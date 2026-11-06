package me.yourname.minisync.discord;

import me.yourname.minisync.MiniSync;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.TimeUnit;

public final class DiscordManager extends ListenerAdapter {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MiniSync plugin;
    private volatile JDA jda;

    public DiscordManager(MiniSync plugin) { this.plugin = plugin; }

    public void start() {
        String token = plugin.getConfig().getString("discord.token", "");
        if (token.isBlank() || token.equals("YOUR_DISCORD_BOT_TOKEN")) {
            plugin.getLogger().warning("Chưa cấu hình discord.token; Discord Bot sẽ không khởi động.");
            return;
        }

        try {
            JDA client = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("NIGHTMARE TCYT"))
                    .addEventListeners(this)
                    .build();
            client.awaitReady();
            client.updateCommands().addCommands(
                    Commands.slash("sync", "Tạo mã liên kết tài khoản Minecraft")
            ).queue(
                    success -> plugin.getLogger().info("Đã đăng ký Discord slash command /sync."),
                    error -> plugin.getLogger().severe("Không thể đăng ký slash command: " + error.getMessage())
            );
            jda = client;
            plugin.getLogger().info("Discord Bot đã kết nối.");
        } catch (Exception exception) {
            plugin.getLogger().severe("Không thể khởi động Discord Bot: " + exception.getMessage());
        }
    }

    public void stop() {
        JDA client = jda;
        jda = null;
        if (client != null) {
            client.shutdown();
            try {
                if (!client.awaitShutdown(5, TimeUnit.SECONDS)) client.shutdownNow();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                client.shutdownNow();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sync")) return;

        event.deferReply(true).queue();
        String discordId = event.getUser().getId();
        int expiryMinutes = Math.max(1, plugin.getConfig().getInt("sync.code-expiry-minutes", 10));

        plugin.getExecutor().execute(() -> {
            try (Connection connection = plugin.getMySQL().getConnection()) {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM minisync_codes WHERE discord_id=? OR expires_at <= NOW()")) {
                    delete.setString(1, discordId);
                    delete.executeUpdate();
                }

                String code = null;
                for (int attempt = 0; attempt < 10; attempt++) {
                    String candidate = generateCode();
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO minisync_codes(code, discord_id, expires_at) " +
                            "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))")) {
                        insert.setString(1, candidate);
                        insert.setString(2, discordId);
                        insert.setInt(3, expiryMinutes);
                        insert.executeUpdate();
                        code = candidate;
                        break;
                    } catch (SQLIntegrityConstraintViolationException duplicate) {
                        // Retry if this randomly generated code already exists.
                    }
                }

                if (code == null) {
                    event.getHook().editOriginal("❌ Không thể tạo mã lúc này. Vui lòng thử lại.").queue();
                    return;
                }
                event.getHook().editOriginal(
                        "🔗 **Mã liên kết Minecraft của bạn:** `" + code + "`\n"
                        + "Vào server và nhập `/sync " + code + "`.\n"
                        + "Mã có hiệu lực trong **" + expiryMinutes + " phút**."
                ).queue();
            } catch (Exception exception) {
                plugin.getLogger().warning("Không thể tạo mã liên kết: " + exception.getMessage());
                event.getHook().editOriginal("❌ Database đang gặp sự cố. Vui lòng thử lại sau.").queue();
            }
        });
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        return code.toString();
    }
}
