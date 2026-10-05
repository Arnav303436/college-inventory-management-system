package com.college.inventory.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connection lifecycle, schema initialization, and sample data seeding.
 */
public class DatabaseManager {
    private static final String DB_FILE = "college_inventory.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Creates all required tables if they don't already exist.
     */
    public static void initializeDatabase() {
        String createDepartments = """
            CREATE TABLE IF NOT EXISTS departments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                hod TEXT,
                email TEXT
            );
        """;

        String createCategories = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT
            );
        """;

        String createSuppliers = """
            CREATE TABLE IF NOT EXISTS suppliers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                contact_person TEXT,
                phone TEXT,
                email TEXT,
                address TEXT
            );
        """;

        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL,
                department TEXT
            );
        """;

        String createItems = """
            CREATE TABLE IF NOT EXISTS items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_code TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
                department_id INTEGER REFERENCES departments(id) ON DELETE SET NULL,
                quantity INTEGER NOT NULL DEFAULT 0,
                min_threshold INTEGER NOT NULL DEFAULT 5,
                unit_price REAL NOT NULL DEFAULT 0.0,
                location TEXT,
                condition_status TEXT NOT NULL DEFAULT 'Working',
                supplier_id INTEGER REFERENCES suppliers(id) ON DELETE SET NULL,
                description TEXT,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createIssueRecords = """
            CREATE TABLE IF NOT EXISTS issue_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
                borrower_type TEXT NOT NULL,
                borrower_id TEXT NOT NULL,
                borrower_name TEXT NOT NULL,
                borrower_email TEXT,
                borrower_department TEXT,
                quantity INTEGER NOT NULL DEFAULT 1,
                issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expected_return_date TEXT NOT NULL,
                actual_return_date TIMESTAMP,
                status TEXT NOT NULL DEFAULT 'ISSUED',
                remarks TEXT
            );
        """;

        String createStockTransactions = """
            CREATE TABLE IF NOT EXISTS stock_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
                transaction_type TEXT NOT NULL,
                quantity_change INTEGER NOT NULL,
                resulting_quantity INTEGER NOT NULL,
                reference_note TEXT,
                performed_by TEXT,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createDepartments);
            stmt.execute(createCategories);
            stmt.execute(createSuppliers);
            stmt.execute(createUsers);
            stmt.execute(createItems);
            stmt.execute(createIssueRecords);
            stmt.execute(createStockTransactions);

            seedInitialData(conn);
            System.out.println("[DB] Database initialized successfully: " + DB_FILE);
        } catch (SQLException e) {
            System.err.println("[DB Error] Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds initial realistic college sample data if tables are empty.
     */
    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if users exist
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Database is already populated
                return;
            }
        }

        System.out.println("[DB] Seeding realistic college sample data...");

        // 1. Seed Users
        String insertUser = "INSERT INTO users (username, password, full_name, role, department) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "System Administrator");
            ps.setString(4, "ADMIN");
            ps.setString(5, "IT Support & Central Admin");
            ps.executeUpdate();

            ps.setString(1, "cs_incharge");
            ps.setString(2, "lab123");
            ps.setString(3, "Prof. Rajesh Sharma");
            ps.setString(4, "LAB_ASSISTANT");
            ps.setString(5, "Computer Science");
            ps.executeUpdate();

            ps.setString(1, "chem_incharge");
            ps.setString(2, "chem123");
            ps.setString(3, "Dr. Sunita Rao");
            ps.setString(4, "LAB_ASSISTANT");
            ps.setString(5, "Chemistry Dept");
            ps.executeUpdate();
        }

        // 2. Seed Departments
        String insertDept = "INSERT INTO departments (code, name, hod, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertDept)) {
            Object[][] depts = {
                {"CSE", "Computer Science & Engineering", "Dr. A. Verma", "hod.cse@college.edu"},
                {"ECE", "Electronics & Communication Engg", "Dr. K. Patel", "hod.ece@college.edu"},
                {"MECH", "Mechanical Engineering", "Dr. S. Nair", "hod.mech@college.edu"},
                {"CHEM", "Department of Chemistry", "Dr. Sunita Rao", "chem@college.edu"},
                {"PHYS", "Department of Physics", "Dr. M. Bose", "physics@college.edu"},
                {"LIB", "Central College Library", "Mrs. L. Das", "library@college.edu"},
                {"SPORTS", "Physical Education & Sports", "Coach Vikram", "sports@college.edu"}
            };
            for (Object[] d : depts) {
                ps.setString(1, (String) d[0]);
                ps.setString(2, (String) d[1]);
                ps.setString(3, (String) d[2]);
                ps.setString(4, (String) d[3]);
                ps.executeUpdate();
            }
        }

        // 3. Seed Categories
        String insertCat = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertCat)) {
            Object[][] cats = {
                {"Computers & Laptops", "Desktops, Workstations, High-Performance Compute, Laptops"},
                {"Lab Instrumentation", "Oscilloscopes, Function Generators, Multimeters, Sensors"},
                {"Chemicals & Glassware", "Beakers, Reagents, Flasks, Test Tubes, Safety equipment"},
                {"Audio-Visual Equipment", "Projectors, Digital Podium, PA Systems, Smart Boards"},
                {"Workshop Machinery", "Lathe machines, 3D Printers, CNC drills, Welder units"},
                {"Sports Gear", "Balls, Bats, Rackets, Athletic equipment, Gym weights"}
            };
            for (Object[] c : cats) {
                ps.setString(1, (String) c[0]);
                ps.setString(2, (String) c[1]);
                ps.executeUpdate();
            }
        }

        // 4. Seed Suppliers
        String insertSupplier = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSupplier)) {
            Object[][] suppliers = {
                {"Dell Technologies Enterprise", "Rohit Malhotra", "+91 9876543210", "sales@dellpartner.com", "Tech Park, Bengaluru"},
                {"Borosil Scientific Glass", "Meera Kulkarni", "+91 9811223344", "support@borosilsci.com", "Industrial Estate, Mumbai"},
                {"Tektronix Instruments India", "Anand Sen", "+91 9822334455", "contact@tektronix-in.com", "Okhla Phase III, New Delhi"},
                {"BenQ Educational Displays", "Pooja Desai", "+91 9833445566", "edu@benqindia.com", "Cyber City, Gurugram"}
            };
            for (Object[] s : suppliers) {
                ps.setString(1, (String) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setString(5, (String) s[4]);
                ps.executeUpdate();
            }
        }

        // 5. Seed Items
        String insertItem = "INSERT INTO items (item_code, name, category_id, department_id, quantity, min_threshold, unit_price, location, condition_status, supplier_id, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
            Object[][] items = {
                {"CSE-WS-001", "Dell Precision 3660 Workstation i7 32GB", 1, 1, 45, 10, 85000.0, "Lab 4, Software Lab", "Working", 1, "High end computing lab PC"},
                {"CSE-PRJ-002", "BenQ 4K Laser Projector 4000 Lumens", 4, 1, 6, 2, 72000.0, "Seminar Hall 1 & 2", "Working", 4, "High lumen projector for presentations"},
                {"ECE-DSO-010", "Tektronix 100MHz Digital Oscilloscope", 2, 2, 14, 5, 48000.0, "ECE Hardware Lab Rack B", "Working", 3, "2-channel digital storage oscilloscope"},
                {"ECE-ARD-015", "Arduino Mega 2560 R3 Starter Kit", 2, 2, 28, 8, 2500.0, "Embedded Systems Cupboard 3", "Working", 3, "Microcontroller learning kit"},
                {"CHEM-BEAK-01", "Borosil 500ml Pyrex Glass Beakers (Set of 10)", 3, 4, 8, 15, 1200.0, "Chemistry Store Rack 1", "Working", 2, "Low stock - reorder required"},
                {"CHEM-BUN-02", "Stainless Steel Bunsen Burner with Valve", 3, 4, 30, 10, 850.0, "Chemistry Lab Workbenches", "Working", 2, "Gas burner for titration"},
                {"MECH-3DP-001", "Creality Ender 3 V3 Plus 3D Printer", 5, 3, 3, 2, 38000.0, "Robotics & Prototyping Cell", "Working", 1, "Fused Deposition Modeling Printer"},
                {"SPORTS-BB-01", "Spalding NBA Official Leather Basketball", 6, 7, 4, 6, 3200.0, "Sports Locker 2", "Working", 4, "Basketballs for college varsity team"}
            };
            for (Object[] it : items) {
                ps.setString(1, (String) it[0]);
                ps.setString(2, (String) it[1]);
                ps.setInt(3, (Integer) it[2]);
                ps.setInt(4, (Integer) it[3]);
                ps.setInt(5, (Integer) it[4]);
                ps.setInt(6, (Integer) it[5]);
                ps.setDouble(7, (Double) it[6]);
                ps.setString(8, (String) it[7]);
                ps.setString(9, (String) it[8]);
                ps.setInt(10, (Integer) it[9]);
                ps.setString(11, (String) it[10]);
                ps.executeUpdate();
            }
        }

        // 6. Seed Initial Stock Transactions (Audit Log)
        String insertTx = "INSERT INTO stock_transactions (item_id, transaction_type, quantity_change, resulting_quantity, reference_note, performed_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTx)) {
            ps.setInt(1, 1);
            ps.setString(2, "STOCK_IN");
            ps.setInt(3, 45);
            ps.setInt(4, 45);
            ps.setString(5, "Initial procurement PO #COL-2026-091");
            ps.setString(6, "admin");
            ps.executeUpdate();

            ps.setInt(1, 3);
            ps.setString(2, "STOCK_IN");
            ps.setInt(3, 14);
            ps.setInt(4, 14);
            ps.setString(5, "Grant procurement batch 1");
            ps.setString(6, "admin");
            ps.executeUpdate();
        }

        // 7. Seed Sample Active Loan / Issue
        String insertIssue = "INSERT INTO issue_records (item_id, borrower_type, borrower_id, borrower_name, borrower_email, borrower_department, quantity, expected_return_date, status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertIssue)) {
            ps.setInt(1, 4); // Arduino kit
            ps.setString(2, "STUDENT");
            ps.setString(3, "23CS104");
            ps.setString(4, "Aryan Saxena");
            ps.setString(5, "aryan.23cs104@college.edu");
            ps.setString(6, "Computer Science");
            ps.setInt(7, 2);
            ps.setString(8, "2026-10-15");
            ps.setString(9, "ISSUED");
            ps.setString(10, "Capstone IoT project prototype testing");
            ps.executeUpdate();

            ps.setInt(1, 2); // Projector
            ps.setString(2, "FACULTY");
            ps.setString(3, "EMP-CS-022");
            ps.setString(4, "Dr. Ramesh Gupta");
            ps.setString(5, "ramesh.gupta@college.edu");
            ps.setString(6, "Computer Science");
            ps.setInt(7, 1);
            ps.setString(8, "2026-10-06");
            ps.setString(9, "ISSUED");
            ps.setString(10, "National AI Conference presentation");
            ps.executeUpdate();
        }

        System.out.println("[DB] Seed data generated successfully!");
    }
}
