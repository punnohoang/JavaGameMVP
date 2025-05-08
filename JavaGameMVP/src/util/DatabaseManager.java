package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = "jdbc:sqlserver://DESKTOP-9CKDHNV:1433;databaseName=Score;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Gọi khi người chơi nhập tên để bắt đầu chơi
    public static void insertNewPlayer(String playerName) {
        String sql = "INSERT INTO HighScores (playerName, score, playTime) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setInt(2, 0);
            stmt.setInt(3, 0);;
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cập nhật điểm
    public static void updateScore(String playerName, int additionalScore) {
        String sql = "UPDATE HighScores SET score = score + ? WHERE playerName = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, additionalScore);
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lưu điểm, số lần chết
    public static void savePlayerResult(String playerName, int score) {
        String checkSql = "SELECT COUNT(*) FROM HighScores WHERE playerName = ?";
        String updateSql = "UPDATE HighScores SET score = ?, deathCount = ? WHERE playerName = ?";
        String insertSql = "INSERT INTO HighScores (playerName, score, playTime) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Kiểm tra người chơi tồn tại
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerName);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                boolean exists = rs.getInt(1) > 0;
                if (exists) {
                    // Cập nhật
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, score);
                        updateStmt.setString(3, playerName);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Thêm mới
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, playerName);
                        insertStmt.setInt(2, score);
                        insertStmt.setInt(4, 0);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy top 3 người chơi có điểm cao nhất
    public static List<String> getTop3Players() {
        List<String> topPlayers = new ArrayList<>();
        String sql = "SELECT playerName, score, playTime FROM HighScores ORDER BY score DESC, playTime ASC OFFSET 0 ROWS FETCH NEXT 3 ROWS ONLY";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String player = rs.getString("playerName");
                int score = rs.getInt("score");
                int time = rs.getInt("playTime");
                topPlayers.add(player + " - " + score + " điểm - " + time + "s");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }


    // Cập nhật thời gian chơi
    public static void recordPlayTime(String playerName, int playTime) {
        String sql = "UPDATE HighScores SET playTime = ? WHERE playerName = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playTime);
            stmt.setString(2, playerName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ Không thể cập nhật playTime. Kiểm tra lại cột playTime trong bảng HighScores.");
            e.printStackTrace();
        }
    }
}