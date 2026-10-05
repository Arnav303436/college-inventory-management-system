package com.college.inventory.ui;

import com.college.inventory.model.StockTransaction;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StockTransactionPanel extends JPanel {
    private final InventoryService inventoryService;
    private JTable tblAudit;
    private DefaultTableModel tableModel;

    public StockTransactionPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Header
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);

        JLabel lblTitle = new JLabel("Inventory Movement Audit Ledger");
        lblTitle.setFont(ModernTheme.FONT_HEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Log");
        btnRefresh.addActionListener(e -> refreshTable());

        topSection.add(lblTitle, BorderLayout.WEST);
        topSection.add(btnRefresh, BorderLayout.EAST);
        add(topSection, BorderLayout.NORTH);

        // Table
        String[] cols = {"Log ID", "Date & Time", "Asset Code", "Item Name", "Action Type", "Qty Delta", "Resulting Stock", "Reference / Reason", "Staff User"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAudit = new JTable(tableModel);
        tblAudit.setRowHeight(28);
        tblAudit.getColumnModel().getColumn(0).setMaxWidth(60);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblAudit.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer qtyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("+")) {
                        setForeground(new Color(22, 101, 52));
                    } else if (str.startsWith("-")) {
                        setForeground(ModernTheme.ACCENT_RED);
                    }
                }
                return c;
            }
        };
        tblAudit.getColumnModel().getColumn(5).setCellRenderer(qtyRenderer);

        JScrollPane scrollPane = new JScrollPane(tblAudit);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<StockTransaction> list = inventoryService.getTransactionDao().getAll();
        for (StockTransaction t : list) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getTimestamp(),
                t.getItemCode(),
                t.getItemName(),
                t.getTransactionType(),
                (t.getQuantityChange() > 0 ? "+" : "") + t.getQuantityChange(),
                t.getResultingQuantity(),
                t.getReferenceNote(),
                t.getPerformedBy()
            });
        }
    }
}
