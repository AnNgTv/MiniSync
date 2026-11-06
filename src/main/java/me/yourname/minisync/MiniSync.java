package me.yourname.minisync;

import me.yourname.minisync.commands.SyncCommand;
import me.yourname.minisync.commands.UnsyncCommand;
import me.yourname.minisync.database.MySQL;
import me.yourname.minisync.discord.DiscordManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class MiniSync extends JavaPlugin {
    private static MiniSync instance;
    private MySQL mysql;
    private DiscordManager discordManager;
    private ExecutorService executor;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "MiniSync-Worker");
            thread.setDaemon(true);
            return thread;
        };
        executor = Executors.newFixedThreadPool(3, factory);
        mysql = new MySQL(this);
        discordManager = new DiscordManager(this);

        if (getCommand("sync") != null) getCommand("sync").setExecutor(new SyncCommand(this));
        if (getCommand("unsync") != null) getCommand("unsync").setExecutor(new UnsyncCommand(this));

        // Never block the Minecraft main thread while connecting to MySQL or Discord.
        executor.execute(() -> {
            mysql.connect();
            if (mysql.isConnected()) discordManager.start();
            else getLogger().severe("Discord Bot không khởi động vì MySQL chưa kết nối.");
        });
        getLogger().info("MiniSync 2.0 đang khởi động...");
    }

    @Override
    public void onDisable() {
        if (discordManager != null) discordManager.stop();
        if (mysql != null) mysql.disconnect();
        if (executor != null) executor.shutdownNow();
        instance = null;
        getLogger().info("MiniSync đã tắt.");
    }

    public static MiniSync getInstance() { return instance; }
    public MySQL getMySQL() { return mysql; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public ExecutorService getExecutor() { return executor; }
}
