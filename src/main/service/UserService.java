package service;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";

    public UserService() {
        DatabaseService.initializeDatabase();
    }

    // Register user in MySQL
    public synchronized boolean register(User user) {
        if (user == null || !user.isValid()) {
            return false;
        }

        if (user.getRole().equalsIgnoreCase("ADMIN")) {
            return false;
        }

        String sql = "INSERT INTO users (username, password, role, full_name, email, farm_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, "USER");
            ps.setString(4, user.getFullName() != null ? user.getFullName() : user.getUsername());
            ps.setString(5, user.getEmail() != null ? user.getEmail() : "");
            ps.setString(6, user.getFarmType() != null ? user.getFarmType() : "General Livestock");

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Registration failed in database: " + e.getMessage());
            return false;
        }
    }

    // Login authentication against MySQL
    public synchronized boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        // Hardcoded safety fallback for admin
        if (username.equalsIgnoreCase(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            return true;
        }

        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Login query error: " + e.getMessage());
            return false;
        }
    }

    // Get user role from MySQL
    public synchronized String getUserRole(String username) {
        if (username == null) return null;

        if (username.equalsIgnoreCase(ADMIN_USERNAME)) {
            return "ADMIN";
        }

        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.err.println("Role check error: " + e.getMessage());
        }
        return null;
    }

    // Delete a user from MySQL
    public synchronized boolean deleteUser(String username) {
        if (username == null || username.equalsIgnoreCase(ADMIN_USERNAME)) {
            return false;
        }

        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Delete user error: " + e.getMessage());
            return false;
        }
    }

    // Return users as JSON string from MySQL
    public synchronized String usersAsJson() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT username, role, full_name, email, farm_type FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append("{\"username\":\"").append(escape(rs.getString("username")))
                        .append("\",\"role\":\"").append(escape(rs.getString("role")))
                        .append("\",\"fullName\":\"").append(escape(rs.getString("full_name")))
                        .append("\",\"email\":\"").append(escape(rs.getString("email")))
                        .append("\",\"farmType\":\"").append(escape(rs.getString("farm_type"))).append("\"}");
            }
        } catch (SQLException e) {
            System.err.println("Fetch users error: " + e.getMessage());
            return "[{\"username\":\"admin\",\"role\":\"ADMIN\",\"fullName\":\"System Administrator\",\"email\":\"admin@farm.local\",\"farmType\":\"Headquarters\"}]";
        }
        json.append("]");
        return json.toString();
    }

    public List<User> getUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT username, password, role, full_name, email, farm_type FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("farm_type")
                ));
            }
        } catch (SQLException e) {
            System.err.println("getUsers error: " + e.getMessage());
        }
        return list;
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
