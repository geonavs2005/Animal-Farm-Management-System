package service;

import java.sql.*;
import java.time.LocalDate;

public class FarmDataService {

    public FarmDataService() {
        DatabaseService.initializeDatabase();
    }

    // ================= ANIMALS TABLE =================

    public synchronized String getAnimalsJson() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT tag, name, species, breed, age, gender, health FROM animals ORDER BY animal_id DESC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append("{\"tag\":\"").append(escape(rs.getString("tag")))
                        .append("\",\"name\":\"").append(escape(rs.getString("name")))
                        .append("\",\"species\":\"").append(escape(rs.getString("species")))
                        .append("\",\"breed\":\"").append(escape(rs.getString("breed")))
                        .append("\",\"age\":\"").append(escape(rs.getString("age")))
                        .append("\",\"gender\":\"").append(escape(rs.getString("gender")))
                        .append("\",\"health\":\"").append(escape(rs.getString("health"))).append("\"}");
            }
        } catch (SQLException e) {
            System.err.println("Error reading animals from database: " + e.getMessage());
        }
        json.append("]");
        return json.toString();
    }

    public synchronized boolean addAnimal(String tag, String name, String species, String breed, String age, String gender, String health) {
        String sql = "INSERT INTO animals (tag, name, species, breed, age, gender, health) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag);
            ps.setString(2, name);
            ps.setString(3, species);
            ps.setString(4, breed != null ? breed : "");
            ps.setString(5, age != null ? age : "");
            ps.setString(6, gender != null ? gender : "Female");
            ps.setString(7, health != null ? health : "Healthy");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting animal: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean deleteAnimal(String tag) {
        if (tag == null) return false;
        String sql = "DELETE FROM animals WHERE tag = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tag);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting animal: " + e.getMessage());
            return false;
        }
    }

    // ================= FEED INVENTORY TABLE =================

    public synchronized String getFeedJson() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT code, name, stock, consumption, last_updated FROM feed ORDER BY feed_id ASC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append("{\"code\":\"").append(escape(rs.getString("code")))
                        .append("\",\"name\":\"").append(escape(rs.getString("name")))
                        .append("\",\"stock\":").append(rs.getDouble("stock"))
                        .append(",\"consumption\":").append(rs.getDouble("consumption"))
                        .append(",\"updated\":\"").append(escape(rs.getString("last_updated"))).append("\"}");
            }
        } catch (SQLException e) {
            System.err.println("Error reading feed from database: " + e.getMessage());
        }
        json.append("]");
        return json.toString();
    }

    public synchronized boolean addFeed(String code, String name, double stock, double consumption, String updatedDate) {
        String sql = "INSERT INTO feed (code, name, stock, consumption, last_updated) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setDouble(3, stock);
            ps.setDouble(4, consumption);
            Date dateVal = (updatedDate != null && !updatedDate.isEmpty())
                    ? Date.valueOf(updatedDate)
                    : Date.valueOf(LocalDate.now());
            ps.setDate(5, dateVal);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting feed: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean restockFeed(String code, double amount) {
        if (code == null || amount <= 0) return false;
        String sql = "UPDATE feed SET stock = stock + ?, last_updated = CURRENT_DATE() WHERE code = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error restocking feed: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean deleteFeed(String code) {
        if (code == null) return false;
        String sql = "DELETE FROM feed WHERE code = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting feed: " + e.getMessage());
            return false;
        }
    }

    // ================= HEALTH RECORDS TABLE =================

    public synchronized String getHealthJson() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT record_id, animal, treatment_type, veterinarian, record_date, status, notes FROM health_records ORDER BY id DESC";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                json.append("{\"id\":\"").append(escape(rs.getString("record_id")))
                        .append("\",\"animal\":\"").append(escape(rs.getString("animal")))
                        .append("\",\"type\":\"").append(escape(rs.getString("treatment_type")))
                        .append("\",\"vet\":\"").append(escape(rs.getString("veterinarian")))
                        .append("\",\"date\":\"").append(escape(rs.getString("record_date")))
                        .append("\",\"status\":\"").append(escape(rs.getString("status")))
                        .append("\",\"notes\":\"").append(escape(rs.getString("notes"))).append("\"}");
            }
        } catch (SQLException e) {
            System.err.println("Error reading health records from database: " + e.getMessage());
        }
        json.append("]");
        return json.toString();
    }

    public synchronized boolean addHealth(String id, String animal, String type, String vet, String date, String status, String notes) {
        String sql = "INSERT INTO health_records (record_id, animal, treatment_type, veterinarian, record_date, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, animal);
            ps.setString(3, type);
            ps.setString(4, vet != null ? vet : "");
            Date dateVal = (date != null && !date.isEmpty()) ? Date.valueOf(date) : Date.valueOf(LocalDate.now());
            ps.setDate(5, dateVal);
            ps.setString(6, status != null ? status : "Completed");
            ps.setString(7, notes != null ? notes : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting health record: " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean deleteHealth(String id) {
        if (id == null) return false;
        String sql = "DELETE FROM health_records WHERE record_id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting health record: " + e.getMessage());
            return false;
        }
    }

    // ================= ALL DATA =================

    public synchronized String getAllDataJson(UserService userService) {
        return "{"
                + "\"animals\":" + getAnimalsJson() + ","
                + "\"feed\":" + getFeedJson() + ","
                + "\"health\":" + getHealthJson() + ","
                + "\"users\":" + userService.usersAsJson()
                + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
