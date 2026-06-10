package me.yourname.minisync.commands;

import me.yourname.minisync.MiniSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UnsyncCommand implements CommandExecutor {

    private final MiniSync plugin;

    public UnsyncCommand(MiniSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        try {
            PreparedStatement ps = plugin.getMySQL().getConnection()
                .prepareStatement("DELETE FROM minisync WHERE uuid=?");
            ps.setString(1, p.getUniqueId().toString());
            int affected = ps.executeUpdate();

            if (affected > 0) {
                p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.unsync_success"));
            } else {
                p.sendMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.not_synced"));
            }

        } catch (SQLException e) {
            p.sendMessage("§cCó lỗi xảy ra khi kết nối Database!");
            e.printStackTrace();
        }
        return true;
    }
}