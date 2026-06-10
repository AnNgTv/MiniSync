package me.yourname.minisync.database;

import me.yourname.minisync.MiniSync;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

    private Connection connection;
    private final MiniSync plugin;

    public MySQL(MiniSync plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");

        try {
            if (connection != null && !connection.isClosed()) return;

            synchronized (this) {
                if (connection != null && !connection.isClosed()) return;
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    username, password
                );
                setupTables();
            }
            plugin.getLogger().info("MySQL connected successfully!");
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to MySQL! " + e.getMessage());
        }
    }

    private void setupTables() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS minisync (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "discord_id VARCHAR(32), " +
                "linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS minisync_codes (" +
                "code VARCHAR(8) PRIMARY KEY, " +
                "discord_id VARCHAR(32), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")) {
            ps.executeUpdate();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }
}