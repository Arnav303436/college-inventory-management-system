package com.college.inventory.dao;

import com.college.inventory.config.DatabaseManager;
import com.college.inventory.model.IssueRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueRecordDao {

    private static final String BASE_SELECT = """
        SELECT r.*, i.item_code, i.name AS item_name 
        FROM issue_records r
        JOIN items i ON r.item_id = i.id
    """;

    public List<IssueRecord> getAll(String statusFilter) {
        List<IssueRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append(" WHERE UPPER(r.status) = ? ");
        }
        sql.append(" ORDER BY r.issue_date DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
                ps.setString(1, statusFilter.trim().toUpperCase());
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

    public IssueRecord getById(int id) {
        String sql = BASE_SELECT + " WHERE r.id = ?";
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

    public boolean add(IssueRecord record) {
        String sql = """
            INSERT INTO issue_records (item_id, borrower_type, borrower_id, borrower_name, 
                                       borrower_email, borrower_department, quantity, 
                                       expected_return_date, status, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ISSUED', ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getItemId());
            ps.setString(2, record.getBorrowerType());
            ps.setString(3, record.getBorrowerId());
            ps.setString(4, record.getBorrowerName());
            ps.setString(5, record.getBorrowerEmail());
            ps.setString(6, record.getBorrowerDepartment());
            ps.setInt(7, record.getQuantity());
            ps.setString(8, record.getExpectedReturnDate());
            ps.setString(9, record.getRemarks());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        record.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markReturned(int recordId, String returnTimestamp, String returnRemarks) {
        String sql = """
            UPDATE issue_records 
            SET status = 'RETURNED', 
                actual_return_date = COALESCE(?, CURRENT_TIMESTAMP), 
                remarks = CASE WHEN remarks IS NULL OR remarks = '' THEN ? ELSE remarks || ' | ' || ? END
            WHERE id = ? AND status = 'ISSUED'
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, returnTimestamp);
            ps.setString(2, returnRemarks != null ? returnRemarks : "Returned in good order");
            ps.setString(3, returnRemarks != null ? returnRemarks : "Returned in good order");
            ps.setInt(4, recordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getActiveIssuedCount() {
        String sql = "SELECT COUNT(*) FROM issue_records WHERE status = 'ISSUED'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private IssueRecord mapRow(ResultSet rs) throws SQLException {
        IssueRecord rec = new IssueRecord(
            rs.getInt("id"),
            rs.getInt("item_id"),
            rs.getString("borrower_type"),
            rs.getString("borrower_id"),
            rs.getString("borrower_name"),
            rs.getString("borrower_email"),
            rs.getString("borrower_department"),
            rs.getInt("quantity"),
            rs.getString("issue_date"),
            rs.getString("expected_return_date"),
            rs.getString("actual_return_date"),
            rs.getString("status"),
            rs.getString("remarks")
        );
        rec.setItemCode(rs.getString("item_code"));
        rec.setItemName(rs.getString("item_name"));
        return rec;
    }
}
