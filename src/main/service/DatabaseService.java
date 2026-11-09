package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String URL = "jdbc:mysql://localhost:3306/animal_farm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return false;
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Ensure tables exist
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL DEFAULT 'USER', "
                    + "full_name VARCHAR(100) DEFAULT '', "
                    + "email VARCHAR(100) DEFAULT '', "
                    + "farm_type VARCHAR(100) DEFAULT 'General Livestock', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS animals ("
                    + "animal_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "tag VARCHAR(50) UNIQUE NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "species VARCHAR(50) NOT NULL, "
                    + "breed VARCHAR(50) DEFAULT '', "
                    + "age VARCHAR(50) DEFAULT '', "
                    + "gender VARCHAR(20) DEFAULT 'Female', "
                    + "health VARCHAR(50) DEFAULT 'Healthy', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS feed ("
                    + "feed_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "code VARCHAR(50) UNIQUE NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "stock DECIMAL(10,2) NOT NULL DEFAULT 0.0, "
                    + "consumption DECIMAL(10,2) NOT NULL DEFAULT 0.0, "
                    + "last_updated DATE NOT NULL"
                    + ") ENGINE=InnoDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS health_records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "record_id VARCHAR(50) UNIQUE NOT NULL, "
                    + "animal VARCHAR(100) NOT NULL, "
                    + "treatment_type VARCHAR(100) NOT NULL, "
                    + "veterinarian VARCHAR(100) DEFAULT '', "
                    + "record_date DATE NOT NULL, "
                    + "status VARCHAR(50) DEFAULT 'Completed', "
                    + "notes TEXT"
                    + ") ENGINE=InnoDB");

            // Ensure default admin exists
            stmt.executeUpdate("INSERT IGNORE INTO users (username, password, role, full_name, email, farm_type) "
                    + "VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@farm.local', 'Headquarters')");

            System.out.println(">> MySQL Database 'animal_farm_db' connected and verified successfully.");
        } catch (SQLException e) {
            System.err.println("Could not initialize database tables: " + e.getMessage());
        }
    }
}

