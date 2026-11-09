package model;

public class User {

    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String farmType;

    public User(String username, String password, String role) {
        this(username, password, role, username, null, null);
    }

    public User(String username, String password, String role, String fullName, String email, String farmType) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.farmType = farmType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getFarmType() {
        return farmType;
    }

    public boolean isValid() {

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        if (password.length() < 4) {
            return false;
        }

        if (role == null || role.trim().isEmpty()) {
            return false;
        }

        if (!role.equalsIgnoreCase("USER")
                && !role.equalsIgnoreCase("ADMIN")) {
            return false;
        }

        return true;
    }
}