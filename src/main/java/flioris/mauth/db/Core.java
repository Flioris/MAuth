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

package flioris.mauth.db;

import flioris.mauth.MAuth;
import flioris.mauth.db.impl.MySQLDatabase;
import flioris.mauth.db.impl.SQLiteDatabase;
import flioris.mauth.util.ConfigHandler;

import java.io.File;

public class Core {
    private final IDatabase db;

    public Core() {
        db = ConfigHandler.getBoolean("MySQL.enabled")?
                new MySQLDatabase(
                        ConfigHandler.getString("MySQL.host"),
                        ConfigHandler.getInt("MySQL.port"),
                        ConfigHandler.getString("MySQL.database"),
                        ConfigHandler.getString("MySQL.user"),
                        ConfigHandler.getString("MySQL.password")
                ) :
                new SQLiteDatabase(new File(MAuth.getPlugin().getDataFolder().getPath() + "/mauth.db").getAbsolutePath());
    }

    public String getAccount(String playerName) {
        return db.getAccount(playerName);
    }

    public String getAccountByID(String id) {
        return db.getAccountByID(id);
    }

    public String getSession(String playerName) {
        return db.getSession(playerName);
    }

    public String getSessionByIP(String ip) {
        return db.getSessionByIP(ip);
    }

    public void addSession(String playerName, String ip) {
        db.addSession(playerName, ip);
    }

    public void remSession(String playerName) {
        db.remSession(playerName);
    }

    public void setAccount(String playerName, String id) {
        db.setAccount(playerName, id);
    }

    public void delAccount(String playerName) {
        db.delAccount(playerName);
    }

    public boolean isWhiteListed(String ip) {
        return db.isWhiteListed(ip);
    }

    public void addInWhiteList(String ip) {
        db.addInWhiteList(ip);
    }

    public void remFromWhiteList(String ip) {
        db.remFromWhiteList(ip);
    }

    public void close() {
        if (db != null) db.close();
    }
}
