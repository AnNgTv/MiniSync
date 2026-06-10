package me.yourname.minisync.discord;

import me.yourname.minisync.MiniSync;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class DiscordManager extends ListenerAdapter {

    private JDA jda;
    private final MiniSync plugin;

    public DiscordManager(MiniSync plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String token = plugin.getConfig().getString("discord.token");
        if (token == null || token.equals("YOUR_DISCORD_BOT_TOKEN")) {
            plugin.getLogger().warning("Discord token chưa được thiết lập trong config.yml!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(token)
                .addEventListeners(this)
                .build();
            
            jda.awaitReady();
            
            // Đăng ký lệnh Slash
            jda.updateCommands().addCommands(
                Commands.slash("sync", "Nhận mã liên kết tài khoản Minecraft")
            ).queue();

            plugin.getLogger().info("Discord Bot đã khởi động thành công!");
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi khi khởi động Discord Bot: " + e.getMessage());
        }
    }

    public void stop() {
        if (jda != null) jda.shutdown();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("sync")) return;

        String discordId = event.getUser().getId();
        String code = generateCode();

        try {
            PreparedStatement ps = plugin.getMySQL().getConnection()
                .prepareStatement("REPLACE INTO minisync_codes(code, discord_id) VALUES (?,?)");
            ps.setString(1, code);
            ps.setString(2, discordId);
            ps.executeUpdate();

            event.reply("🔗 Mã liên kết của bạn là: **" + code + "**\nHãy vào Minecraft và gõ: `/sync " + code + "`")
                .setEphemeral(true).queue();
        } catch (SQLException e) {
            event.reply("❌ Đã có lỗi xảy ra khi tạo mã!").setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < 6) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}