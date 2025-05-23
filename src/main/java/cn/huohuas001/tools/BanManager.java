package cn.huohuas001.tools;

import java.sql.*;

public class BanManager {
    private static final String DB_URL = "jdbc:sqlite:data/bans.db";
    private static Connection connection;

    // 初始化数据库
    public static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS bans (" +
                        "server_id TEXT PRIMARY KEY," +
                        "reason TEXT," +
                        "banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 检查是否被封禁
    public static boolean isBanned(String serverId) {
        String sql = "SELECT server_id FROM bans WHERE server_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 添加封禁记录
    public static boolean banServer(String serverId, String reason) {
        String sql = "INSERT OR REPLACE INTO bans(server_id, reason) VALUES(?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverId);
            pstmt.setString(2, reason);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 解除封禁
    public static boolean unbanServer(String serverId) {
        String sql = "DELETE FROM bans WHERE server_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
