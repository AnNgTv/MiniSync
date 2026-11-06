package me.yourname.minisync.commands;

import me.yourname.minisync.MiniSync;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class UnsyncCommand implements CommandExecutor {
    private final MiniSync plugin;

    public UnsyncCommand(MiniSync plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi.");
            return true;
        }
        if (!player.hasPermission("minisync.use")) {
            player.sendMessage(prefix() + plugin.getConfig().getString("messages.no_permission", "§cBạn không có quyền."));
            return true;
        }

        String uuid = player.getUniqueId().toString();
        plugin.getExecutor().execute(() -> {
            String key;
            try (Connection connection = plugin.getMySQL().getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM minisync WHERE uuid=?")) {
                statement.setString(1, uuid);
                key = statement.executeUpdate() > 0 ? "messages.unsync_success" : "messages.not_synced";
            } catch (Exception exception) {
                plugin.getLogger().warning("Lỗi hủy liên kết: " + exception.getMessage());
                key = "messages.database_error";
            }

            String messageKey = key;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) player.sendMessage(prefix()
                        + plugin.getConfig().getString(messageKey, "§cĐã xảy ra lỗi."));
            });
        });
        return true;
    }

    private String prefix() { return plugin.getConfig().getString("messages.prefix", "§b[MiniSync] "); }
}
