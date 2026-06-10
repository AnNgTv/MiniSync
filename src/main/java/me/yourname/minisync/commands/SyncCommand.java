package me.yourname.minisync.commands;

import me.yourname.minisync.MiniSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SyncCommand implements CommandExecutor {

    private final MiniSync plugin;

    public SyncCommand(MiniSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Chỉ người chơi mới dùng được lệnh này!");
            return true;
        }

        Player p = (Player) sender;
        if (args.length != 1) {
            p.sendMessage(plugin.getConfig().getString("messages.prefix") + "§c/sync <code>");
            return true;
        }

        String code = args[0];

        try {
            // Kiểm tra mã
            PreparedStatement ps = plugin.getMySQL().getConnection()
                .prepareStatement("SELECT discord_id FROM minisync_codes WHERE code=?");
            ps.setString(1, code);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.invalid_code"));
                return true;
            }

            String discordId = rs.getString("discord_id");

            // Lưu liên kết
            PreparedStatement insert = plugin.getMySQL().getConnection()
                .prepareStatement("REPLACE INTO minisync(uuid, discord_id) VALUES (?,?)");
            insert.setString(1, p.getUniqueId().toString());
            insert.setString(2, discordId);
            insert.executeUpdate();

            // Xóa mã đã dùng
            PreparedStatement deleteCode = plugin.getMySQL().getConnection()
                .prepareStatement("DELETE FROM minisync_codes WHERE code=?");
            deleteCode.setString(1, code);
            deleteCode.executeUpdate();

            String msg = plugin.getConfig().getString("messages.sync_success").replace("%discord_id%", discordId);
            p.sendMessage(plugin.getConfig().getString("messages.prefix") + msg);

        } catch (SQLException e) {
            p.sendMessage("§cCó lỗi xảy ra khi kết nối Database!");
            e.printStackTrace();
        }
        return true;
    }
}