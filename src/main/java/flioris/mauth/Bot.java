/**
 * Copyright © 2024 Flioris
 *
 * This file is part of MAuth.
 *
 * MAuth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package flioris.mauth;

import lombok.Getter;
import flioris.mauth.util.confirmMessage.ConfirmMessage;
import flioris.mauth.cache.ConfirmMessageCache;
import flioris.mauth.util.confirmMessage.ConfirmMessageType;
import flioris.mauth.db.Core;
import flioris.mauth.event.SuccessAuthEvent;
import flioris.mauth.listener.DiscordListener;
import flioris.mauth.cache.BlockedUsersCache;
import flioris.mauth.util.ConfigHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.List;

public class Bot {
    @Getter
    private static JDA jda;
    @Getter
    private static Guild mainGuild;
    private static Role verifiedRole;
    private static Emoji emoji;
    private static final Core core = MAuth.getCore();
    private static final Plugin plugin = MAuth.getPlugin();

    public static void startBot(String token) {
        String activity = ConfigHandler.getString("discord.playing");
        jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                .addEventListeners(new DiscordListener())
                .setActivity(activity.isEmpty()? null : Activity.playing(activity))
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("id", "Display your ID.")
                        .setDescriptionLocalization(DiscordLocale.RUSSIAN, "Выводит ваш ID.")
        ).queue();

        try {
            jda.awaitReady();
        } catch (InterruptedException ignored) {
            plugin.getLogger().severe("Failed to start discord bot.");
        }

        mainGuild = jda.getGuildById(ConfigHandler.getString("discord.guild-id"));
        verifiedRole = ConfigHandler.getBoolean("issuing-verified-role") ?
                mainGuild.getRoleById(ConfigHandler.getString("discord.verified-role-id")) : null;
        emoji = Emoji.fromUnicode(ConfigHandler.getString("discord.emoji"));

    }

    public static void processMessageReaction(User user, String messageId) {
        ConfirmMessage confirmationMessage = ConfirmMessageCache.get(messageId);

        if (confirmationMessage == null) {
            return;
        }

        Player player = confirmationMessage.getPlayer();

        switch (confirmationMessage.getConfirmationMessageType()) {
            case JOIN -> confirmJoin(player);
            case LINK -> confirmAccount(player, user);
            case UNLINK -> confirmUnlink(player, user);
        }

        ConfirmMessageCache.remove(messageId);
    }

    public static void confirmJoin(Player player) {
        String playerName = player.getName();

        if (core.getSession(playerName) != null) {
            return;
        }

        core.addSession(playerName, player.getAddress().getHostString());
        if (player.isOnline()) {
            BlockedUsersCache.remove(playerName);
            player.sendMessage(ConfigHandler.improve("messages.success-auth"));
            new BukkitRunnable(){
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new SuccessAuthEvent(player));
                }
            }.runTask(plugin);
        }
    }

    public static void confirmAccount(Player player, User user) {
        String playerName = player.getName();

        if (core.getAccount(playerName) != null) {
            return;
        }

        core.setAccount(playerName, user.getId());
        core.addSession(playerName, player.getAddress().getHostString());
        if (player.isOnline()) {
            BlockedUsersCache.remove(playerName);
            player.sendMessage(ConfigHandler.improve("messages.success-auth"));
            addVerifiedRole(user);
            new BukkitRunnable(){
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new SuccessAuthEvent(player));
                }
            }.runTask(plugin);
        }
    }

    public static void confirmUnlink(Player player, User user) {
        String playerName = player.getName();

        if (core.getAccount(playerName) == null) {
            return;
        }

        core.remSession(playerName);
        core.delAccount(playerName);
        if (player.isOnline()) {
            BlockedUsersCache.remove(playerName);
            removeVerifiedRole(user);
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.kickPlayer(ConfigHandler.improve("messages.success-account-unlinked"));
                }
            }.runTask(plugin);
        }
    }

    public static void sendConfirmJoin(String id, Player player) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(player.getName())
                .setDescription(ConfigHandler.getString("embeds.confirm-join-desc"))
                .setColor(new Color(255, 238, 127))
                .build();

        sendPrivateEmbed(id, embed, player, ConfirmMessageType.JOIN);
    }

    public static void sendConfirmAccount(String id, Player player) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(player.getName())
                .setDescription(ConfigHandler.getString("embeds.confirm-account-desc"))
                .setColor(new Color(174, 255, 127))
                .build();

        sendPrivateEmbed(id, embed, player, ConfirmMessageType.LINK);
    }

    public static void sendConfirmUnlink(String id, Player player) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(player.getName())
                .setDescription(ConfigHandler.getString("embeds.confirm-unlink-desc"))
                .setColor(new Color(255, 127, 127))
                .build();

        sendPrivateEmbed(id, embed, player, ConfirmMessageType.UNLINK);
    }

    public static void sendPrivateEmbed(String id, MessageEmbed embed, Player player, ConfirmMessageType type) {
        try {
            jda.retrieveUserById(id).queue(
                    user -> user.openPrivateChannel().queue(
                            channel -> channel.sendMessageEmbeds(embed).queue(
                                    message -> {
                                        ConfirmMessageCache.put(message.getId(), player, channel, type);
                                        player.sendMessage(ConfigHandler.improve("messages.confirm-message-sent"));
                                        message.addReaction(emoji).queue();},
                                    error -> player.sendMessage(ConfigHandler.improve("messages.pm-is-closed"))),
                            error -> player.sendMessage(ConfigHandler.improve("messages.invalid-account"))),
                    error -> player.sendMessage(ConfigHandler.improve("messages.invalid-account")));
        } catch (NumberFormatException exception) {
            player.sendMessage(ConfigHandler.improve("messages.invalid-account"));
        }
    }

    public static void addVerifiedRole(User user) {
        if (verifiedRole != null) {
            mainGuild.addRoleToMember(user, verifiedRole).queue();
        }
    }

    public static void removeVerifiedRole(User user) {
        if (verifiedRole != null) {
            mainGuild.removeRoleFromMember(user, verifiedRole).queue();
        }
    }

    public static boolean hasWhitelistedRole(String memberID) {
        List<String> list = ConfigHandler.getStringList("discord.whitelisted-role-ids");

        if (list.isEmpty()) {
            return true;
        }

        try {
            Member member = mainGuild.retrieveMemberById(memberID).complete();
            return member != null && member.getRoles().stream().map(ISnowflake::getId).anyMatch(list::contains);
        } catch (Exception ignored) {
            return true;
        }
    }
}
