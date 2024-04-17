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

package flioris.mauth.cache;

import flioris.mauth.util.confirmMessage.ConfirmMessage;
import flioris.mauth.util.confirmMessage.ConfirmMessageType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ConfirmMessageCache {
    private static final Map<String, ConfirmMessage> confirmationMessages = new HashMap<>();

    public static void put(String id, Player player, PrivateChannel channel, ConfirmMessageType type) {
        confirmationMessages.put(id, new ConfirmMessage(player, channel, type));
    }

    public static ConfirmMessage get(String id) {
        return confirmationMessages.get(id);
    }

    public static void remove(String id) {
        confirmationMessages.remove(id);
    }
}
