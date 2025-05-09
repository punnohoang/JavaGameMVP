package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
	private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = "jdbc:sqlserver://PUNNO:1433;databaseName=Score;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Thêm bản ghi mới và trả về scoreId
    public static int recordPlayTime(String playerName, int playTime, int score) {
        String sql = "INSERT INTO HighScores (playerName, playTime, score) VALUES (?, ?, ?); SELECT SCOPE_IDENTITY() AS id;";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setInt(2, playTime);
            stmt.setInt(3, score);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Không thể lưu lượt chơi. Kiểm tra lại bảng HighScores.");
            e.printStackTrace();
        }
        return -1;
    }

    // Reset playTime và score cho bản ghi có scoreId
    public static void resetPlayerRecord(int scoreId) {
        String sql = "UPDATE HighScores SET playTime = 0, score = 0 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scoreId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ Không thể reset bản ghi. Kiểm tra lại bảng HighScores.");
            e.printStackTrace();
        }
    }

    // Cập nhật điểm và thời gian cho lượt chơi có scoreId
    public static void updateScore(int scoreId, int score, int playTime) {
        String sql = "UPDATE HighScores SET score = ?, playTime = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setInt(2, playTime);
            stmt.setInt(3, scoreId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠️ Không thể cập nhật điểm. Kiểm tra lại bảng HighScores.");
            e.printStackTrace();
        }
    }

    // Lấy top 3 lượt chơi có điểm cao nhất
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
            System.err.println("⚠️ Không thể lấy top 3 người chơi. Kiểm tra lại bảng HighScores.");
            e.printStackTrace();
        }
        return topPlayers;
    }

    public static void insertNewPlayer(String playerName) {
    }
}