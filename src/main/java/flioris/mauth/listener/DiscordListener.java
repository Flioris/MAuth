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

package flioris.mauth.listener;

import flioris.mauth.Bot;
import flioris.mauth.MAuth;
import flioris.mauth.db.Core;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
    private final Core core = MAuth.getCore();

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        User user = event.getUser();

        if (user != null && event.isFromType(ChannelType.PRIVATE) && !user.isBot()) {
            Bot.processMessageReaction(user, event.getMessageId());
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();

        if (event.getGuild().getId().equals(Bot.getMainGuild().getId()) && core.getAccountByID(user.getId()) != null) {
            Bot.addVerifiedRole(user);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("id")) {
            event.reply("ID: " + event.getUser().getId()).setEphemeral(true).queue();
        }
    }
}
