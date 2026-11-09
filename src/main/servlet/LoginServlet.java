package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = RegistrationServlet.getUserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String requestedRole = request.getParameter("role");
        boolean authenticated = userService.login(username, password);
        String actualRole = authenticated ? userService.getUserRole(username) : null;
        if (authenticated && requestedRole != null
                && !requestedRole.equalsIgnoreCase(actualRole)) {
            authenticated = false;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (authenticated) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", actualRole);
        }
        response.getWriter().printf(
                "{\"success\":%s,\"message\":\"%s\"}",
                authenticated,
                authenticated ? "Login successful." : "Invalid username or password.");
    }
}
