package com.college.inventory.dao;

import com.college.inventory.config.DatabaseManager;
import com.college.inventory.model.StockTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDao {

    private static final String BASE_SELECT = """
        SELECT t.*, i.item_code, i.name AS item_name 
        FROM stock_transactions t
        JOIN items i ON t.item_id = i.id
    """;

    public List<StockTransaction> getAll() {
        List<StockTransaction> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY t.timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<StockTransaction> getByItemId(int itemId) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE t.item_id = ? ORDER BY t.timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean recordTransaction(StockTransaction tx) {
        String sql = """
            INSERT INTO stock_transactions (item_id, transaction_type, quantity_change, 
                                            resulting_quantity, reference_note, performed_by, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getItemId());
            ps.setString(2, tx.getTransactionType());
            ps.setInt(3, tx.getQuantityChange());
            ps.setInt(4, tx.getResultingQuantity());
            ps.setString(5, tx.getReferenceNote());
            ps.setString(6, tx.getPerformedBy());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        tx.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private StockTransaction mapRow(ResultSet rs) throws SQLException {
        StockTransaction tx = new StockTransaction(
            rs.getInt("id"),
            rs.getInt("item_id"),
            rs.getString("transaction_type"),
            rs.getInt("quantity_change"),
            rs.getInt("resulting_quantity"),
            rs.getString("reference_note"),
            rs.getString("performed_by"),
            rs.getString("timestamp")
        );
        tx.setItemCode(rs.getString("item_code"));
        tx.setItemName(rs.getString("item_name"));
        return tx;
    }
}
