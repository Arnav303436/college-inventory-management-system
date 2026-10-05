package com.college.inventory.ui;

import com.college.inventory.model.Item;
import com.college.inventory.model.StockTransaction;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final InventoryService inventoryService;
    private final MainFrame mainFrame;

    private JPanel statsGrid;
    private JTable tblLowStock;
    private DefaultTableModel lowStockModel;
    private JTable tblRecentTx;
    private DefaultTableModel recentTxModel;

    public DashboardPanel(InventoryService inventoryService, MainFrame mainFrame) {
        this.inventoryService = inventoryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Header
        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setOpaque(false);

        JLabel lblWelcome = new JLabel("College Inventory & Asset Dashboard");
        lblWelcome.setFont(ModernTheme.FONT_TITLE);
        lblWelcome.setForeground(ModernTheme.PRIMARY_DARK);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);

        JButton btnQuickAdd = ModernTheme.createPrimaryButton("+ Add Asset");
        btnQuickAdd.addActionListener(e -> mainFrame.openAddAssetDialog());

        JButton btnQuickIssue = ModernTheme.createSecondaryButton("Issue Asset");
        btnQuickIssue.addActionListener(e -> mainFrame.openIssueDialog(null));

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        quickActions.add(btnQuickAdd);
        quickActions.add(btnQuickIssue);
        quickActions.add(btnRefresh);

        topSection.add(lblWelcome, BorderLayout.WEST);
        topSection.add(quickActions, BorderLayout.EAST);

        // Stats Cards Grid
        statsGrid = new JPanel(new GridLayout(1, 5, 12, 12));
        statsGrid.setOpaque(false);
        topSection.add(statsGrid, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);

        // Center Content Split: Low Stock Alerts (Left) + Recent Transactions (Right)
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 14, 14));
        tablesPanel.setOpaque(false);

        // 1. Low Stock Card
        JPanel lowStockCard = new JPanel(new BorderLayout(8, 8));
        lowStockCard.setBackground(ModernTheme.CARD_BG);
        lowStockCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblLowStockTitle = new JLabel("Low Stock Reorder Alerts");
        lblLowStockTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblLowStockTitle.setForeground(ModernTheme.ACCENT_RED);
        lowStockCard.add(lblLowStockTitle, BorderLayout.NORTH);

        lowStockModel = new DefaultTableModel(new String[]{"Code", "Item Name", "Current Qty", "Min Threshold"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLowStock = new JTable(lowStockModel);
        tblLowStock.setRowHeight(28);
        lowStockCard.add(new JScrollPane(tblLowStock), BorderLayout.CENTER);

        // 2. Recent Stock Transactions Card
        JPanel recentTxCard = new JPanel(new BorderLayout(8, 8));
        recentTxCard.setBackground(ModernTheme.CARD_BG);
        recentTxCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTxTitle = new JLabel("Recent Inventory Movements");
        lblTxTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblTxTitle.setForeground(ModernTheme.PRIMARY_DARK);
        recentTxCard.add(lblTxTitle, BorderLayout.NORTH);

        recentTxModel = new DefaultTableModel(new String[]{"Timestamp", "Item Code", "Type", "Change", "By"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecentTx = new JTable(recentTxModel);
        tblRecentTx.setRowHeight(28);
        recentTxCard.add(new JScrollPane(tblRecentTx), BorderLayout.CENTER);

        tablesPanel.add(lowStockCard);
        tablesPanel.add(recentTxCard);

        add(tablesPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        Map<String, Object> stats = inventoryService.getDashboardStats();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        statsGrid.removeAll();
        statsGrid.add(ModernTheme.createStatCard("Total Stock Units", String.valueOf(stats.get("totalUnits")), "In campus inventory", ModernTheme.PRIMARY));
        statsGrid.add(ModernTheme.createStatCard("Catalog Items", String.valueOf(stats.get("uniqueItems")), "Unique SKU categories", ModernTheme.ACCENT_PURPLE));
        statsGrid.add(ModernTheme.createStatCard("Active Loans", String.valueOf(stats.get("activeLoans")), "With Students/Faculty", new Color(14, 165, 233)));
        statsGrid.add(ModernTheme.createStatCard("Low Stock Alerts", String.valueOf(stats.get("lowStockCount")), "Need replenishment", ModernTheme.ACCENT_RED));
        statsGrid.add(ModernTheme.createStatCard("Asset Valuation", currencyFormat.format((Double) stats.get("totalValuation")), "Total purchase value", ModernTheme.ACCENT_GREEN));

        statsGrid.revalidate();
        statsGrid.repaint();

        // Refresh Low Stock table
        lowStockModel.setRowCount(0);
        List<Item> allItems = inventoryService.getItemDao().getAll();
        for (Item it : allItems) {
            if (it.isLowStock()) {
                lowStockModel.addRow(new Object[]{
                    it.getItemCode(),
                    it.getName(),
                    it.getQuantity(),
                    it.getMinThreshold()
                });
            }
        }

        // Refresh Recent Tx table
        recentTxModel.setRowCount(0);
        List<StockTransaction> txs = inventoryService.getTransactionDao().getAll();
        int count = 0;
        for (StockTransaction tx : txs) {
            if (count++ >= 8) break;
            recentTxModel.addRow(new Object[]{
                tx.getTimestamp() != null ? tx.getTimestamp().substring(0, Math.min(16, tx.getTimestamp().length())) : "",
                tx.getItemCode(),
                tx.getTransactionType(),
                (tx.getQuantityChange() > 0 ? "+" : "") + tx.getQuantityChange(),
                tx.getPerformedBy()
            });
        }
    }
}
