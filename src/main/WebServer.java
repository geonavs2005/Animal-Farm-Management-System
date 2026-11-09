import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import model.User;
import service.FarmDataService;
import service.UserService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {

    private static final UserService userService = new UserService();
    private static final FarmDataService farmDataService = new FarmDataService();
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        StaticFileHandler staticHandler = new StaticFileHandler();
        server.createContext("/", staticHandler);

        // Login API Endpoint
        server.createContext("/login", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/login")) {
                staticHandler.handle(exchange);
                return;
            }
            if (handleCors(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> data = readRequestData(exchange);

                String username = data.getOrDefault("username", "").trim();
                String password = data.getOrDefault("password", "");
                String requestedRole = data.getOrDefault("role", "").trim();

                boolean authenticated = userService.login(username, password);
                String actualRole = authenticated ? userService.getUserRole(username) : null;

                // Validate requested role against actual role if requested
                if (authenticated && !requestedRole.isEmpty() && actualRole != null) {
                    if (!requestedRole.equalsIgnoreCase(actualRole)) {
                        authenticated = false;
                        String roleMismatchMsg = "Account role is " + actualRole + ", but " + requestedRole + " login was selected.";
                        sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"" + escape(roleMismatchMsg) + "\",\"role\":\"\"}");
                        return;
                    }
                }

                if (authenticated) {
                    String json = String.format(
                            "{\"success\":true,\"message\":\"Login successful! Welcome back, %s!\",\"role\":\"%s\",\"username\":\"%s\"}",
                            escape(username), escape(actualRole), escape(username));
                    sendJsonResponse(exchange, 200, json);
                } else {
                    String json = "{\"success\":false,\"message\":\"Invalid username or password. Please try again.\",\"role\":\"\"}";
                    sendJsonResponse(exchange, 200, json);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Registration API Endpoint
        server.createContext("/register", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/register")) {
                staticHandler.handle(exchange);
                return;
            }
            if (handleCors(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> data = readRequestData(exchange);

                String username = data.getOrDefault("username", "").trim();
                String password = data.getOrDefault("password", "");
                String fullName = data.getOrDefault("fullName", username).trim();
                String email = data.getOrDefault("email", "").trim();
                String farmType = data.getOrDefault("farmType", "General Livestock").trim();

                if (username.length() < 3) {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Username must be at least 3 characters.\"}");
                    return;
                }
                if (password.length() < 4) {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Password must contain at least 4 characters.\"}");
                    return;
                }

                User user = new User(username, password, "USER", fullName, email, farmType);
                boolean registered = userService.register(user);

                if (registered) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account registered successfully! You can now log in.\"}");
                } else {
                    sendJsonResponse(exchange, 200, "{\"success\":false,\"message\":\"Username already exists or is reserved.\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Logout Endpoint
        server.createContext("/logout", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/logout")) {
                staticHandler.handle(exchange);
                return;
            }
            exchange.getResponseHeaders().set("Location", "/login.html?logout=true");
            exchange.sendResponseHeaders(302, -1);
        });

        // Combined Farm Data Endpoint (Animals, Feed, Health, Users)
        server.createContext("/api/data", exchange -> {
            if (handleCors(exchange)) return;

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, farmDataService.getAllDataJson(userService));
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Users Endpoint (GET /api/users, DELETE /api/users?username=...)
        server.createContext("/api/users", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, userService.usersAsJson());
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String username = params.get("username");
                if (username == null) {
                    Map<String, String> data = readRequestData(exchange);
                    username = data.get("username");
                }
                boolean deleted = userService.deleteUser(username);
                sendJsonResponse(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Animals Endpoint (GET, POST, DELETE)
        server.createContext("/api/animals", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, farmDataService.getAnimalsJson());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> data = readRequestData(exchange);
                String tag = data.getOrDefault("tag", "").trim().toUpperCase();
                String name = data.getOrDefault("name", "").trim();
                String species = data.getOrDefault("species", "").trim();
                String breed = data.getOrDefault("breed", "").trim();
                String age = data.getOrDefault("age", "").trim();
                String gender = data.getOrDefault("gender", "Female").trim();
                String health = data.getOrDefault("health", "Healthy").trim();

                if (tag.isEmpty() || name.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Tag and Name are required.\"}");
                    return;
                }

                boolean added = farmDataService.addAnimal(tag, name, species, breed, age, gender, health);
                sendJsonResponse(exchange, 200, "{\"success\":" + added + ",\"message\":\"Animal saved successfully.\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String tag = params.get("tag");
                if (tag == null) {
                    Map<String, String> data = readRequestData(exchange);
                    tag = data.get("tag");
                }
                boolean deleted = farmDataService.deleteAnimal(tag);
                sendJsonResponse(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Feed Endpoint (GET, POST, DELETE)
        server.createContext("/api/feed", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, farmDataService.getFeedJson());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> data = readRequestData(exchange);
                String code = data.getOrDefault("code", "").trim();
                String name = data.getOrDefault("name", "").trim();
                double stock = parseDouble(data.get("stock"), 0);
                double consumption = parseDouble(data.get("consumption"), 0);
                String updatedDate = java.time.LocalDate.now().toString();

                if (name.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Feed name is required.\"}");
                    return;
                }

                if (code.isEmpty()) {
                    code = "FD-" + (System.currentTimeMillis() % 1000);
                }

                boolean added = farmDataService.addFeed(code, name, stock, consumption, updatedDate);
                sendJsonResponse(exchange, 200, "{\"success\":" + added + ",\"message\":\"Feed item saved successfully.\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String code = params.get("code");
                if (code == null) {
                    Map<String, String> data = readRequestData(exchange);
                    code = data.get("code");
                }
                boolean deleted = farmDataService.deleteFeed(code);
                sendJsonResponse(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Feed Restock Endpoint (POST /api/feed/restock)
        server.createContext("/api/feed/restock", exchange -> {
            if (handleCors(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> data = readRequestData(exchange);
                String code = data.getOrDefault("code", "").trim();
                double amount = parseDouble(data.get("amount"), 0);

                boolean restocked = farmDataService.restockFeed(code, amount);
                sendJsonResponse(exchange, 200, "{\"success\":" + restocked + "}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // Health Endpoint (GET, POST, DELETE)
        server.createContext("/api/health", exchange -> {
            if (handleCors(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, farmDataService.getHealthJson());
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> data = readRequestData(exchange);
                String id = data.getOrDefault("id", "").trim();
                String animal = data.getOrDefault("animal", "").trim();
                String type = data.getOrDefault("type", "").trim();
                String vet = data.getOrDefault("vet", "").trim();
                String date = data.getOrDefault("date", java.time.LocalDate.now().toString()).trim();
                String status = data.getOrDefault("status", "Completed").trim();
                String notes = data.getOrDefault("notes", "").trim();

                if (id.isEmpty()) {
                    id = "HL-" + (int) (Math.random() * 800 + 200);
                }

                boolean added = farmDataService.addHealth(id, animal, type, vet, date, status, notes);
                sendJsonResponse(exchange, 200, "{\"success\":" + added + ",\"message\":\"Medical record saved.\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String id = params.get("id");
                if (id == null) {
                    Map<String, String> data = readRequestData(exchange);
                    id = data.get("id");
                }
                boolean deleted = farmDataService.deleteHealth(id);
                sendJsonResponse(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.start();

        System.out.println("==============================================");
        System.out.println("      ANIMAL FARM MANAGEMENT WEB SERVER       ");
        System.out.println("==============================================");
        System.out.println(" Web Server running at: http://localhost:" + PORT);
        System.out.println(" Home Portal:           http://localhost:" + PORT + "/index.html");
        System.out.println(" User Login:            http://localhost:" + PORT + "/login.html");
        System.out.println(" Admin Login:           http://localhost:" + PORT + "/admin-login.html");
        System.out.println(" Live Dashboard:        http://localhost:" + PORT + "/user-dashboard.html");
        System.out.println(" Database Directory:    " + Paths.get(System.getProperty("user.home"), "animal-farm-data"));
        System.out.println(" Press Ctrl+C to stop the server.");
        System.out.println("==============================================");
    }

    private static boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> readRequestData(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        Map<String, String> data = new HashMap<>();
        if (body.isEmpty()) return data;

        if (body.startsWith("{") && body.endsWith("}")) {
            // Simple JSON parser for top-level key-values
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9.]+)|(true|false)|null)");
            Matcher matcher = pattern.matcher(body);
            while (matcher.find()) {
                String key = matcher.group(1);
                String val = matcher.group(2) != null ? matcher.group(2) :
                             (matcher.group(3) != null ? matcher.group(3) : matcher.group(4));
                data.put(key, val != null ? val : "");
            }
        } else {
            // Form URL Encoded
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    data.put(key, value);
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    data.put(key, "");
                }
            }
        }
        return data;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            } else if (parts.length == 1) {
                params.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    private static double parseDouble(String str, double def) {
        if (str == null || str.trim().isEmpty()) return def;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Redirect any .jsp requests directly to .html
            if (path.endsWith(".jsp")) {
                path = path.replace(".jsp", ".html");
            }

            // Look in current working directory web/ or parent/child web directory
            Path filePath = Paths.get("web", path.substring(1));
            if (!Files.exists(filePath)) {
                filePath = Paths.get("AnimalFarmManagementSystem", "web", path.substring(1));
            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = getContentType(filePath.toString());
                byte[] bytes = Files.readAllBytes(filePath);

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "404 Not Found";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private String getContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
            return "text/plain; charset=UTF-8";
        }
    }
}
