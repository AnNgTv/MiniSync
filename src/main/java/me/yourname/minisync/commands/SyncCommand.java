package me.yourname.minisync.commands;

import me.yourname.minisync.MiniSync;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public final class SyncCommand implements CommandExecutor {
    private final MiniSync plugin;

    public SyncCommand(MiniSync plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi.");
            return true;
        }
        if (!player.hasPermission("minisync.use")) {
            send(player, "messages.no_permission");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(prefix() + "§c/sync <code>");
            return true;
        }

        UUID uuid = player.getUniqueId();
        String code = args[0].toUpperCase(java.util.Locale.ROOT);
        player.sendMessage(prefix() + plugin.getConfig().getString("messages.processing", "§eĐang xác minh mã..."));

        plugin.getExecutor().execute(() -> {
            String resultKey;
            try (Connection connection = plugin.getMySQL().getConnection()) {
                connection.setAutoCommit(false);
                try {
                    String discordId;
                    try (PreparedStatement select = connection.prepareStatement(
                            "SELECT discord_id FROM minisync_codes WHERE code=? AND expires_at > NOW() FOR UPDATE")) {
                        select.setString(1, code);
                        try (ResultSet rs = select.executeQuery()) {
                            if (!rs.next()) {
                                connection.rollback();
                                send(player, "messages.invalid_code");
                                return;
                            }
                            discordId = rs.getString("discord_id");
                        }
                    }

                    try (PreparedStatement existing = connection.prepareStatement(
                            "SELECT uuid FROM minisync WHERE discord_id=? AND uuid<>?")) {
                        existing.setString(1, discordId);
                        existing.setString(2, uuid.toString());
                        try (ResultSet rs = existing.executeQuery()) {
                            if (rs.next()) {
                                connection.rollback();
                                send(player, "messages.discord_already_linked");
                                return;
                            }
                        }
                    }

                    try (PreparedStatement upsert = connection.prepareStatement(
                            "INSERT INTO minisync(uuid, discord_id) VALUES (?,?) " +
                            "ON DUPLICATE KEY UPDATE discord_id=VALUES(discord_id), linked_at=CURRENT_TIMESTAMP")) {
                        upsert.setString(1, uuid.toString());
                        upsert.setString(2, discordId);
                        upsert.executeUpdate();
                    }
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM minisync_codes WHERE code=?")) {
                        delete.setString(1, code);
                        delete.executeUpdate();
                    }
                    connection.commit();
                    resultKey = "messages.sync_success";
                } catch (Exception exception) {
                    connection.rollback();
                    throw exception;
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (Exception exception) {
                plugin.getLogger().warning("Lỗi xác minh mã: " + exception.getMessage());
                resultKey = "messages.database_error";
            }
            send(player, resultKey);
        });
        return true;
    }

    private String prefix() { return plugin.getConfig().getString("messages.prefix", "§b[MiniSync] "); }

    private void send(Player player, String path) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) player.sendMessage(prefix()
                    + plugin.getConfig().getString(path, "§cĐã xảy ra lỗi."));
        });
    }
}
