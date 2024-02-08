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

package maxmeitner.mauth.db.impl;

import maxmeitner.mauth.db.IDatabase;

import java.sql.*;

public class SQLiteDatabase implements IDatabase {
    private final String url;

    public SQLiteDatabase(String path) {
        url = "jdbc:sqlite:" + path;
        try (Connection cn = connect(); Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (id INTEGER PRIMARY KEY NOT NULL, player TEXT NOT NULL, did TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (id INTEGER PRIMARY KEY NOT NULL, player TEXT NOT NULL, ip TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS whitelist (id INTEGER PRIMARY KEY NOT NULL, ip TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void setAccount(String playerName, String accountId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO accounts (player, did) VALUES (?, ?)");
            ps.setString(1, playerName);
            ps.setString(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delAccount(String playerName) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE player = ?");
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAccount(String playerName) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT did FROM accounts WHERE player = ?");
            ps.setString(1, playerName);
            ResultSet result = ps.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAccountByID(String accountId) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT player FROM accounts WHERE did = ?");
            ps.setString(1, accountId);
            ResultSet result = ps.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSession(String playerName) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT ip FROM sessions WHERE player = ?");
            ps.setString(1, playerName);
            ResultSet result = ps.executeQuery();
            return result.next()? result.getString(1) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addSession(String playerName, String playerIp) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO sessions (player, ip) VALUES (?, ?)");
            ps.setString(1, playerName);
            ps.setString(2, playerIp);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remSession(String playerName) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM sessions WHERE player = ?");
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSessionByIP(String ip) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT player FROM sessions WHERE ip = ?");
            ps.setString(1, ip);
            ResultSet result = ps.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWhiteListed (String ip) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM whitelist WHERE ip = ?");
            ps.setString(1, ip);
            ResultSet result = ps.executeQuery();
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addInWhiteList(String ip) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO whitelist (ip) VALUES (?)");
            ps.setString(1, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remFromWhiteList(String ip) {
        try (Connection c = connect()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM whitelist WHERE ip = ?");
            ps.setString(1, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {}
}
