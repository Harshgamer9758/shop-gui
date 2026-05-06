package com.blockmart.shopgui.managers;

import com.blockmart.shopgui.ShopGUI;
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

    public void loadDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            File databaseFile = new File(plugin.getDataFolder(), "shop.db");
            config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            config.setPoolName("ShopGUIPool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setMaxLifetime(30000);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(30000);

            dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().log(Level.INFO, "Database connection established.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to database! Error: " + e.getMessage(), e);
        }
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS shop_transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "player_name VARCHAR(16) NOT NULL," +
                    "item_id VARCHAR(255) NOT NULL," +
                    "item_material VARCHAR(255) NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "price REAL NOT NULL," +
                    "transaction_type VARCHAR(10) NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
                pstmt.execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create database tables: " + e.getMessage(), e);
            }
        });
    }

    public void recordTransaction(String playerUuid, String playerName, String itemId, String itemMaterial, int quantity, double price, String type) {
        CompletableFuture.runAsync(() -> {
            String insertSQL = "INSERT INTO shop_transactions (player_uuid, player_name, item_id, item_material, quantity, price, transaction_type) VALUES (?, ?, ?, ?, ?, ?, ?);";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, playerUuid);
                pstmt.setString(2, playerName);
                pstmt.setString(3, itemId);
                pstmt.setString(4, itemMaterial);
                pstmt.setInt(5, quantity);
                pstmt.setDouble(6, price);
                pstmt.setString(7, type);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error recording transaction: " + e.getMessage(), e);
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().log(Level.INFO, "Database connection closed.");
        }
    }
}
