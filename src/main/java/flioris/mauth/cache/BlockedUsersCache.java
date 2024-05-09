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

import java.util.HashSet;
import java.util.Set;

public class BlockedUsersCache {
    private static final Set<String> blockedUsers = new HashSet<>();

    public static boolean contains(String playerName) {
        return blockedUsers.contains(playerName);
    }

    public static void add(String playerName) {
        blockedUsers.add(playerName);
    }

    public static void remove(String playerName) {
        blockedUsers.remove(playerName);
    }
}
