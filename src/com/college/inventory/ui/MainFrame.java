package com.college.inventory.ui;

import com.college.inventory.model.Item;
import com.college.inventory.model.User;
import com.college.inventory.service.AuthService;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final InventoryService inventoryService;
    private final AuthService authService;

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private InventoryPanel inventoryPanel;
    private IssueReturnPanel issueReturnPanel;
    private StockTransactionPanel auditPanel;
    private SupplierPanel supplierPanel;
    private DepartmentPanel departmentPanel;
    private ReportPanel reportPanel;

    public MainFrame(InventoryService inventoryService, AuthService authService) {
        super("CampusAsset Pro - College Inventory & Equipment Management System");
        this.inventoryService = inventoryService;
        this.authService = authService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.BG_DARK);

        // Top Navigation & User Status Bar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER_COLOR),
            new EmptyBorder(12, 20, 12, 20)
        ));

        // Brand / College Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel brandIcon = new JLabel("🎓");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel brandTitle = new JLabel("CampusAsset Pro");
        brandTitle.setFont(ModernTheme.FONT_HEADER);
        brandTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JLabel brandSub = new JLabel("| College Inventory Management System");
        brandSub.setFont(ModernTheme.FONT_REGULAR);
        brandSub.setForeground(ModernTheme.TEXT_MUTED);

        brandPanel.add(brandIcon);
        brandPanel.add(brandTitle);
        brandPanel.add(brandSub);
        navBar.add(brandPanel, BorderLayout.WEST);

        // User Info & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        User currentUser = authService.getCurrentUser();
        String userText = (currentUser != null) ? currentUser.getFullName() + " (" + currentUser.getRole() + ")" : "Guest";
        JLabel lblUser = new JLabel("Logged in as: " + userText);
        lblUser.setFont(ModernTheme.FONT_BOLD);
        lblUser.setForeground(ModernTheme.TEXT_MAIN);

        JButton btnLogout = ModernTheme.createSecondaryButton("Sign Out");
        btnLogout.addActionListener(e -> handleLogout());

        userPanel.add(lblUser);
        userPanel.add(btnLogout);
        navBar.add(userPanel, BorderLayout.EAST);

        root.add(navBar, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);

        dashboardPanel = new DashboardPanel(inventoryService, this);
        inventoryPanel = new InventoryPanel(inventoryService, this);
        issueReturnPanel = new IssueReturnPanel(inventoryService, this);
        auditPanel = new StockTransactionPanel(inventoryService);
        supplierPanel = new SupplierPanel(inventoryService);
        departmentPanel = new DepartmentPanel(inventoryService);
        reportPanel = new ReportPanel(inventoryService);

        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        tabbedPane.addTab("📦 Inventory Catalog", inventoryPanel);
        tabbedPane.addTab("🔄 Issue & Return Desk", issueReturnPanel);
        tabbedPane.addTab("📋 Audit Ledger", auditPanel);
        tabbedPane.addTab("🏢 Suppliers & Vendors", supplierPanel);
        tabbedPane.addTab("🏛️ Departments & Labs", departmentPanel);
        tabbedPane.addTab("📈 Reports & Exports", reportPanel);

        // Tab change listener to auto-refresh
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) dashboardPanel.refreshData();
            else if (selectedIndex == 1) { inventoryPanel.loadFilters(); inventoryPanel.refreshTable(); }
            else if (selectedIndex == 2) issueReturnPanel.refreshTable();
            else if (selectedIndex == 3) auditPanel.refreshTable();
            else if (selectedIndex == 4) supplierPanel.refreshTable();
            else if (selectedIndex == 5) departmentPanel.refreshTable();
            else if (selectedIndex == 6) reportPanel.refreshDeptSummary();
        });

        root.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    public void openAddAssetDialog() {
        ItemDialog dlg = new ItemDialog(this, inventoryService, null, getCurrentUser().getFullName());
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            inventoryPanel.refreshTable();
            dashboardPanel.refreshData();
            tabbedPane.setSelectedComponent(inventoryPanel);
        }
    }

    public void openIssueDialog(Item preselected) {
        IssueDialog dlg = new IssueDialog(this, inventoryService, preselected, getCurrentUser().getFullName());
        dlg.setVisible(true);
        if (dlg.isIssued()) {
            issueReturnPanel.refreshTable();
            inventoryPanel.refreshTable();
            dashboardPanel.refreshData();
            tabbedPane.setSelectedComponent(issueReturnPanel);
        }
    }

    public void refreshDashboard() {
        dashboardPanel.refreshData();
    }

    public void refreshInventoryTable() {
        inventoryPanel.refreshTable();
    }

    public User getCurrentUser() {
        User u = authService.getCurrentUser();
        return u != null ? u : new User("admin", "", "System Admin", "ADMIN", "Central");
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            // Re-open login dialog
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDlg = new LoginDialog(null, authService);
                loginDlg.setVisible(true);
                if (loginDlg.isSucceeded()) {
                    new MainFrame(inventoryService, authService).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }
}
