package com.college.inventory.ui;

import com.college.inventory.model.IssueRecord;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class IssueReturnPanel extends JPanel {
    private final InventoryService inventoryService;
    private final MainFrame mainFrame;

    private JComboBox<String> cmbStatusFilter;
    private JTable tblRecords;
    private DefaultTableModel tableModel;
    private List<IssueRecord> displayedRecords;

    public IssueReturnPanel(InventoryService inventoryService, MainFrame mainFrame) {
        this.inventoryService = inventoryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Filter bar
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBackground(ModernTheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        filterCard.add(new JLabel("Loan Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "ISSUED", "RETURNED"});
        cmbStatusFilter.setSelectedItem("ISSUED");
        cmbStatusFilter.addActionListener(e -> refreshTable());
        filterCard.add(cmbStatusFilter);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshTable());
        filterCard.add(btnRefresh);

        add(filterCard, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"ID", "Item Code", "Item Name", "Borrower Type", "Roll/Emp ID", "Borrower Name", "Department", "Qty", "Issue Date", "Due Date", "Returned Date", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecords = new JTable(tableModel);
        tblRecords.setRowHeight(30);
        tblRecords.getColumnModel().getColumn(0).setMaxWidth(50);
        tblRecords.getColumnModel().getColumn(7).setMaxWidth(60);
        tblRecords.getColumnModel().getColumn(11).setCellRenderer(ModernTheme.getStatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tblRecords);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionBar.setOpaque(false);

        JButton btnNewIssue = ModernTheme.createPrimaryButton("+ Issue Equipment");
        btnNewIssue.addActionListener(e -> {
            mainFrame.openIssueDialog(null);
            refreshTable();
        });

        JButton btnReturn = ModernTheme.createSecondaryButton("Process Return Item");
        btnReturn.addActionListener(e -> handleReturn());

        actionBar.add(btnNewIssue);
        actionBar.add(btnReturn);

        add(actionBar, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String filter = (String) cmbStatusFilter.getSelectedItem();
        displayedRecords = inventoryService.getIssueRecordDao().getAll(filter);

        for (IssueRecord r : displayedRecords) {
            tableModel.addRow(new Object[]{
                r.getId(),
                r.getItemCode(),
                r.getItemName(),
                r.getBorrowerType(),
                r.getBorrowerId(),
                r.getBorrowerName(),
                r.getBorrowerDepartment(),
                r.getQuantity(),
                r.getIssueDate() != null ? r.getIssueDate().substring(0, Math.min(10, r.getIssueDate().length())) : "-",
                r.getExpectedReturnDate(),
                r.getActualReturnDate() != null ? r.getActualReturnDate().substring(0, Math.min(10, r.getActualReturnDate().length())) : "-",
                r.getStatus(),
                r.getRemarks() != null ? r.getRemarks() : ""
            });
        }
    }

    private void handleReturn() {
        int row = tblRecords.getSelectedRow();
        if (row < 0 || row >= displayedRecords.size()) {
            JOptionPane.showMessageDialog(this, "Please select an issued record to mark as returned.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        IssueRecord selected = displayedRecords.get(row);
        if (!"ISSUED".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "This item has already been marked as " + selected.getStatus() + ".", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String remarks = JOptionPane.showInputDialog(
            this,
            "Enter return remarks/condition (e.g., 'Returned in good working condition'):",
            "Return Verification for " + selected.getItemCode(),
            JOptionPane.QUESTION_MESSAGE
        );

        if (remarks == null) {
            return; // cancelled
        }

        String res = inventoryService.returnItem(selected.getId(), remarks, mainFrame.getCurrentUser().getFullName());
        if ("SUCCESS".equalsIgnoreCase(res)) {
            JOptionPane.showMessageDialog(this, "Item returned successfully! Stock quantity has been restored.", "Return Complete", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
            mainFrame.refreshDashboard();
            mainFrame.refreshInventoryTable();
        } else {
            JOptionPane.showMessageDialog(this, res, "Return Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
