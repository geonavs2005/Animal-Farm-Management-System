package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.UserService;

import java.io.IOException;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    private static final UserService USER_SERVICE = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/register.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = new User(
                username,
                password,
                "USER",
                request.getParameter("fullName"),
                request.getParameter("email"),
                request.getParameter("farmType"));

        boolean registered = USER_SERVICE.register(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf(
                "{\"success\":%s,\"message\":\"%s\"}",
                registered,
                registered ? "Registration successful." : "Registration failed. Check the details or choose another username.");
    }

    public static UserService getUserService() {
        return USER_SERVICE;
    }
}
