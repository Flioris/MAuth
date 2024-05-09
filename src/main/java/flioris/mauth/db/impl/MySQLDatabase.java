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

package flioris.mauth.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import flioris.mauth.db.IDatabase;

import java.sql.*;

public class MySQLDatabase implements IDatabase {
    private final HikariDataSource src;

    public MySQLDatabase(String host, int port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database);
        config.setUsername(user);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        src = new HikariDataSource(config);
        try (Connection cn = this.connect(); Statement st = cn.createStatement()){
            st.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (player TEXT NOT NULL, did TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (player TEXT NOT NULL, ip TEXT NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS whitelist (ip TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return src.getConnection();
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
            return result.next() ? result.getString(1) : null;
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
            return result.next() ? result.getString(1) : null;
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
            return result.next() ? result.getString(1) : null;
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
            return result.next() ? result.getString(1) : null;
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

    public void close() {
        src.close();
    }
}
