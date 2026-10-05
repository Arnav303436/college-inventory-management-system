package com.college.inventory.dao;

import com.college.inventory.config.DatabaseManager;
import com.college.inventory.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    public List<Department> getAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY code ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Department(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getString("hod"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Department getById(int id) {
        String sql = "SELECT * FROM departments WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Department(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("hod"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean add(Department dept) {
        String sql = "INSERT INTO departments (code, name, hod, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getCode());
            ps.setString(2, dept.getName());
            ps.setString(3, dept.getHeadOfDepartment());
            ps.setString(4, dept.getContactEmail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Department dept) {
        String sql = "UPDATE departments SET code = ?, name = ?, hod = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getCode());
            ps.setString(2, dept.getName());
            ps.setString(3, dept.getHeadOfDepartment());
            ps.setString(4, dept.getContactEmail());
            ps.setInt(5, dept.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
