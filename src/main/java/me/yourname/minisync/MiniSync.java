package me.yourname.minisync;

import me.yourname.minisync.commands.SyncCommand;
import me.yourname.minisync.commands.UnsyncCommand;
import me.yourname.minisync.database.MySQL;
import me.yourname.minisync.discord.DiscordManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniSync extends JavaPlugin {

    private static MiniSync instance;
    private MySQL mysql;
    private DiscordManager discordManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load Config
        saveDefaultConfig();

        // Setup Database
        mysql = new MySQL(this);
        mysql.connect();

        // Setup Discord Bot
        discordManager = new DiscordManager(this);
        discordManager.start();

        // Register Commands
        getCommand("sync").setExecutor(new SyncCommand(this));
        getCommand("unsync").setExecutor(new UnsyncCommand(this));

        getLogger().info("MiniSync đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        if (mysql != null) mysql.disconnect();
        if (discordManager != null) discordManager.stop();
        getLogger().info("MiniSync đã bị vô hiệu hóa!");
    }

    public static MiniSync getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mysql;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}