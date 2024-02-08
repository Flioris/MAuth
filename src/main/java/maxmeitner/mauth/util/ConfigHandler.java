/**
 * Copyright © 2023 Flioris
 *
 * This file is part of Protogenchik.
 *
 * Protogenchik is free software: you can redistribute it and/or modify
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

package maxmeitner.mauth.util;

import maxmeitner.mauth.MAuth;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class ConfigHandler {
    private static final Plugin plugin = MAuth.getPlugin();

    public static String improve(String key) {
        String value = getString(key);
        return ChatColor.translateAlternateColorCodes('&', value == null? "Key value '" + key + "' not found." : value
                .replaceAll("&#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])",
                        "&x&$1&$2&$3&$4&$5&$6"));
    }

    public static String getString(String key) {
        return plugin.getConfig().getString(key);
    }
}
