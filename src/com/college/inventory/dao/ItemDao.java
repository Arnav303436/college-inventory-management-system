package com.college.inventory.dao;

import com.college.inventory.config.DatabaseManager;
import com.college.inventory.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {

    private static final String BASE_SELECT = """
        SELECT i.*, 
               c.name AS category_name, 
               d.name AS department_name, 
               s.name AS supplier_name 
        FROM items i
        LEFT JOIN categories c ON i.category_id = c.id
        LEFT JOIN departments d ON i.department_id = d.id
        LEFT JOIN suppliers s ON i.supplier_id = s.id
    """;

    public List<Item> getAll() {
        return search(null, null, null, false);
    }

    public List<Item> search(String keyword, Integer categoryId, Integer departmentId, boolean lowStockOnly) {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(i.item_code) LIKE ? OR LOWER(i.name) LIKE ? OR LOWER(i.location) LIKE ? OR LOWER(i.description) LIKE ?) ");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append(" AND i.category_id = ? ");
            params.add(categoryId);
        }

        if (departmentId != null && departmentId > 0) {
            sql.append(" AND i.department_id = ? ");
            params.add(departmentId);
        }

        if (lowStockOnly) {
            sql.append(" AND i.quantity <= i.min_threshold ");
        }

        sql.append(" ORDER BY i.item_code ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public Item getById(int id) {
        String sql = BASE_SELECT + " WHERE i.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Item getByCode(String code) {
        String sql = BASE_SELECT + " WHERE LOWER(i.item_code) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean add(Item item) {
        String sql = """
            INSERT INTO items (item_code, name, category_id, department_id, quantity, 
                               min_threshold, unit_price, location, condition_status, 
                               supplier_id, description, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getName());
            if (item.getCategoryId() > 0) ps.setInt(3, item.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            if (item.getDepartmentId() > 0) ps.setInt(4, item.getDepartmentId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, item.getQuantity());
            ps.setInt(6, item.getMinThreshold());
            ps.setDouble(7, item.getUnitPrice());
            ps.setString(8, item.getLocation());
            ps.setString(9, item.getConditionStatus() != null ? item.getConditionStatus() : "Working");
            if (item.getSupplierId() > 0) ps.setInt(10, item.getSupplierId()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, item.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Item item) {
        String sql = """
            UPDATE items 
            SET item_code = ?, name = ?, category_id = ?, department_id = ?, 
                quantity = ?, min_threshold = ?, unit_price = ?, location = ?, 
                condition_status = ?, supplier_id = ?, description = ?, 
                last_updated = CURRENT_TIMESTAMP
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getName());
            if (item.getCategoryId() > 0) ps.setInt(3, item.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            if (item.getDepartmentId() > 0) ps.setInt(4, item.getDepartmentId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, item.getQuantity());
            ps.setInt(6, item.getMinThreshold());
            ps.setDouble(7, item.getUnitPrice());
            ps.setString(8, item.getLocation());
            ps.setString(9, item.getConditionStatus());
            if (item.getSupplierId() > 0) ps.setInt(10, item.getSupplierId()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, item.getDescription());
            ps.setInt(12, item.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuantity(int itemId, int newQuantity) {
        String sql = "UPDATE items SET quantity = ?, last_updated = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM items WHERE quantity <= min_threshold";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalUnitsCount() {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM items";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalValuation() {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0.0) FROM items";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item(
            rs.getInt("id"),
            rs.getString("item_code"),
            rs.getString("name"),
            rs.getInt("category_id"),
            rs.getInt("department_id"),
            rs.getInt("quantity"),
            rs.getInt("min_threshold"),
            rs.getDouble("unit_price"),
            rs.getString("location"),
            rs.getString("condition_status"),
            rs.getInt("supplier_id"),
            rs.getString("description"),
            rs.getString("last_updated")
        );
        item.setCategoryName(rs.getString("category_name"));
        item.setDepartmentName(rs.getString("department_name"));
        item.setSupplierName(rs.getString("supplier_name"));
        return item;
    }
}
