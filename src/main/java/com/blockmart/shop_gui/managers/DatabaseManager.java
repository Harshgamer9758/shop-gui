package com.blockmart.shop_gui.managers;

import com.blockmart.shop_gui.ShopGUI;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final ShopGUI plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ShopGUI plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + new File(dataFolder, "shopgui.db").getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setPoolName("ShopGUIPool");

        try {
            dataSource = new HikariDataSource(config);
            plugin.getLogger().log(Level.INFO, "Database connection established.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().log(Level.INFO, "Database connection closed.");
        }
    }

    public void createTables() {
        CompletableFuture.runAsync(() -> {
            String createShopItemsTable = "CREATE TABLE IF NOT EXISTS shop_items (" +
                    "id TEXT PRIMARY KEY," +
                    "item_json TEXT NOT NULL," +
                    "buy_price DOUBLE NOT NULL," +
                    "sell_price DOUBLE NOT NULL);";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(createShopItemsTable)) {
                pstmt.execute();
                plugin.getLogger().log(Level.INFO, "shop_items table checked/created.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create shop_items table!", e);
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public CompletableFuture<Void> executeUpdate(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database update failed: " + sql, e);
            }
        });
    }
}
