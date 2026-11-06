package me.yourname.minisync.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.yourname.minisync.MiniSync;

import java.sql.Connection;
import java.sql.SQLException;

public final class MySQL {
    private final MiniSync plugin;
    private HikariDataSource dataSource;

    public MySQL(MiniSync plugin) { this.plugin = plugin; }

    public synchronized void connect() {
        if (isConnected()) return;

        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "minisync");
        String username = plugin.getConfig().getString("mysql.username", "user");
        String password = plugin.getConfig().getString("mysql.password", "");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("MiniSync-MySQL");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setValidationTimeout(5000);
        config.setInitializationFailTimeout(10000);

        try {
            dataSource = new HikariDataSource(config);
            try (Connection connection = dataSource.getConnection()) {
                setupTables(connection);
            }
            plugin.getLogger().info("MySQL đã kết nối và kiểm tra schema thành công.");
        } catch (Exception exception) {
            plugin.getLogger().severe("Không thể kết nối/khởi tạo MySQL: " + exception.getMessage());
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    private void setupTables(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS minisync (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "discord_id VARCHAR(32) NOT NULL," +
                "linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY uq_minisync_discord (discord_id)) ENGINE=InnoDB"
            );
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS minisync_codes (" +
                "code VARCHAR(8) NOT NULL PRIMARY KEY," +
                "discord_id VARCHAR(32) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "expires_at DATETIME NULL," +
                "INDEX idx_minisync_code_expiry (expires_at)) ENGINE=InnoDB"
            );
        }

        // Migrate existing 1.x tables without deleting linked accounts.
        try (var statement = connection.createStatement()) {
            try {
                statement.executeUpdate("ALTER TABLE minisync_codes ADD COLUMN expires_at DATETIME NULL");
            } catch (SQLException ignored) {
                // Column already exists.
            }
            statement.executeUpdate("DELETE FROM minisync_codes WHERE expires_at IS NULL OR expires_at <= NOW()");
            try {
                statement.executeUpdate("CREATE UNIQUE INDEX uq_minisync_discord ON minisync(discord_id)");
            } catch (SQLException ignored) {
                // Index exists already, or legacy data contains duplicate Discord IDs.
                // Existing links are retained; new links are still checked by SyncCommand.
            }
        }
    }

    public synchronized boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) throw new SQLException("MySQL chưa kết nối.");
        return dataSource.getConnection();
    }

    public synchronized void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
