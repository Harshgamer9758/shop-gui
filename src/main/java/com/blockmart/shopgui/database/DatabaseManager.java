package com.blockmart.shopgui.database;

import com.blockmart.shopgui.ShopGUI;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final ShopGUI plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ShopGUI plugin) {
        this.plugin = plugin;
        setupDatabase();
    }

    private void setupDatabase() {
        // SQLite setup
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File databaseFile = new File(dataFolder, "shop.db");
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create database file: " + e.getMessage());
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        // You can add more HikariCP properties if needed
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("ShopGUIPool");

        try {
            this.dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().info("Successfully connected to the database.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            String createShopItemsTable = "CREATE TABLE IF NOT EXISTS shop_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "item_serialized TEXT NOT NULL," +
                    "buy_price REAL NOT NULL," +
                    "sell_price REAL NOT NULL," +
                    "stock INTEGER NOT NULL DEFAULT -1," +
                    "is_buyable BOOLEAN NOT NULL DEFAULT TRUE," +
                    "is_sellable BOOLEAN NOT NULL DEFAULT TRUE"
                    + ");";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(createShopItemsTable)) {
                stmt.execute();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create shop_items table: " + e.getMessage());
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed.");
        }
    }

    // Example of async DB operation
    public CompletableFuture<Void> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Async update failed: " + e.getMessage());
            }
        });
    }
}