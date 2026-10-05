package com.college.inventory.ui;

import com.college.inventory.model.Department;
import com.college.inventory.model.Item;
import com.college.inventory.service.InventoryService;
import com.college.inventory.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportPanel extends JPanel {
    private final InventoryService inventoryService;
    private final ReportService reportService;

    private JTable tblDeptValuation;
    private DefaultTableModel deptModel;

    public ReportPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.reportService = new ReportService(inventoryService);

        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        refreshDeptSummary();
    }

    private void initComponents() {
        // Top Section: Export Cards
        JPanel topSection = new JPanel(new GridLayout(1, 3, 16, 16));
        topSection.setOpaque(false);

        topSection.add(createExportCard(
            "Export Full Inventory",
            "Generate a complete spreadsheet (CSV) of all campus assets, quantities, locations, and valuations.",
            "Download Inventory CSV",
            this::exportInventory
        ));

        topSection.add(createExportCard(
            "Export Equipment Loans",
            "Generate a spreadsheet (CSV) of all equipment issued to students, faculty, and return statuses.",
            "Download Loans CSV",
            this::exportLoans
        ));

        topSection.add(createExportCard(
            "Export Audit Logs",
            "Export full stock transaction history, stock adjustments, write-offs, and user accountability trail.",
            "Download Audit CSV",
            this::exportAudit
        ));

        add(topSection, BorderLayout.NORTH);

        // Center Section: Department Asset Valuation Summary Table
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBackground(ModernTheme.CARD_BG);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Department & Lab Asset Valuation Breakdown");
        lblTableTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblTableTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JButton btnRefreshSummary = ModernTheme.createSecondaryButton("Recalculate Summary");
        btnRefreshSummary.addActionListener(e -> refreshDeptSummary());

        tableHeader.add(lblTableTitle, BorderLayout.WEST);
        tableHeader.add(btnRefreshSummary, BorderLayout.EAST);
        centerPanel.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Department Code", "Department Name", "Total Assets Count", "Total Stock Units", "Total Valuation (INR ₹)"};
        deptModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDeptValuation = new JTable(deptModel);
        tblDeptValuation.setRowHeight(30);

        centerPanel.add(new JScrollPane(tblDeptValuation), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createExportCard(String title, String desc, String btnLabel, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        card.setBackground(ModernTheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ModernTheme.FONT_SUBHEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JLabel lblDesc = new JLabel("<html><font color='#64748b'>" + desc + "</font></html>");
        lblDesc.setFont(ModernTheme.FONT_REGULAR);

        JButton btn = ModernTheme.createPrimaryButton(btnLabel);
        btn.addActionListener(e -> action.run());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }

    public void refreshDeptSummary() {
        deptModel.setRowCount(0);
        List<Department> depts = inventoryService.getDepartmentDao().getAll();
        List<Item> items = inventoryService.getItemDao().getAll();

        Map<Integer, Integer> deptUniqueCount = new HashMap<>();
        Map<Integer, Integer> deptUnitsCount = new HashMap<>();
        Map<Integer, Double> deptValuation = new HashMap<>();

        for (Item it : items) {
            int deptId = it.getDepartmentId();
            deptUniqueCount.put(deptId, deptUniqueCount.getOrDefault(deptId, 0) + 1);
            deptUnitsCount.put(deptId, deptUnitsCount.getOrDefault(deptId, 0) + it.getQuantity());
            deptValuation.put(deptId, deptValuation.getOrDefault(deptId, 0.0) + it.getTotalValuation());
        }

        NumberFormat cur = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        for (Department d : depts) {
            int uCount = deptUniqueCount.getOrDefault(d.getId(), 0);
            int units = deptUnitsCount.getOrDefault(d.getId(), 0);
            double val = deptValuation.getOrDefault(d.getId(), 0.0);

            deptModel.addRow(new Object[]{
                d.getCode(),
                d.getName(),
                uCount + " items",
                units + " units",
                cur.format(val)
            });
        }
    }

    private void exportInventory() {
        File file = promptSaveFile("inventory_report_" + getTimestamp() + ".csv");
        if (file != null) {
            if (reportService.exportInventoryToCsv(file)) {
                JOptionPane.showMessageDialog(this, "Inventory report saved to:\n" + file.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportLoans() {
        File file = promptSaveFile("equipment_loans_" + getTimestamp() + ".csv");
        if (file != null) {
            if (reportService.exportIssuesToCsv(file, "ALL")) {
                JOptionPane.showMessageDialog(this, "Equipment loans report saved to:\n" + file.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportAudit() {
        File file = promptSaveFile("audit_log_" + getTimestamp() + ".csv");
        if (file != null) {
            if (reportService.exportAuditLogToCsv(file)) {
                JOptionPane.showMessageDialog(this, "Audit log report saved to:\n" + file.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private File promptSaveFile(String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
