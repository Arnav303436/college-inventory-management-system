package com.college.inventory.ui;

import com.college.inventory.model.Category;
import com.college.inventory.model.Department;
import com.college.inventory.model.Item;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;
    private final MainFrame mainFrame;

    private JTextField txtSearch;
    private JComboBox<ComboItem> cmbCategory;
    private JComboBox<ComboItem> cmbDepartment;
    private JCheckBox chkLowStockOnly;

    private JTable tblItems;
    private DefaultTableModel tableModel;
    private List<Item> displayedItems;

    private static class ComboItem {
        final int id;
        final String name;
        ComboItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public InventoryPanel(InventoryService inventoryService, MainFrame mainFrame) {
        this.inventoryService = inventoryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        loadFilters();
        refreshTable();
    }

    private void initComponents() {
        // Top Filter Bar Card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBackground(ModernTheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        filterCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Code, Name, or Lab...");
        txtSearch.addActionListener(e -> refreshTable());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>();
        cmbCategory.addActionListener(e -> refreshTable());
        filterCard.add(cmbCategory);

        filterCard.add(new JLabel("Department:"));
        cmbDepartment = new JComboBox<>();
        cmbDepartment.addActionListener(e -> refreshTable());
        filterCard.add(cmbDepartment);

        chkLowStockOnly = new JCheckBox("Low Stock Only");
        chkLowStockOnly.setFont(ModernTheme.FONT_BOLD);
        chkLowStockOnly.setForeground(ModernTheme.ACCENT_RED);
        chkLowStockOnly.setOpaque(false);
        chkLowStockOnly.addActionListener(e -> refreshTable());
        filterCard.add(chkLowStockOnly);

        JButton btnApply = ModernTheme.createSecondaryButton("Filter");
        btnApply.addActionListener(e -> refreshTable());
        filterCard.add(btnApply);

        JButton btnReset = ModernTheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            if (cmbCategory.getItemCount() > 0) cmbCategory.setSelectedIndex(0);
            if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
            chkLowStockOnly.setSelected(false);
            refreshTable();
        });
        filterCard.add(btnReset);

        add(filterCard, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"ID", "Asset Code", "Name", "Category", "Department", "Qty", "Min", "Unit Price", "Valuation", "Location", "Condition", "Stock Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblItems = new JTable(tableModel);
        tblItems.setRowHeight(30);
        tblItems.getColumnModel().getColumn(0).setMaxWidth(50);
        tblItems.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblItems.getColumnModel().getColumn(11).setCellRenderer(ModernTheme.getStatusCellRenderer());

        // Currency format right align renderer
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblItems.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tblItems.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tblItems);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionBar.setOpaque(false);

        JButton btnAdd = ModernTheme.createPrimaryButton("+ Add Asset");
        btnAdd.addActionListener(e -> {
            mainFrame.openAddAssetDialog();
            refreshTable();
        });

        JButton btnEdit = ModernTheme.createSecondaryButton("Edit Asset");
        btnEdit.addActionListener(e -> handleEdit());

        JButton btnAdjust = ModernTheme.createSecondaryButton("Adjust Stock (+/-)");
        btnAdjust.addActionListener(e -> handleStockAdjust());

        JButton btnIssue = ModernTheme.createSecondaryButton("Issue to Borrower");
        btnIssue.addActionListener(e -> handleIssueSelected());

        JButton btnDelete = ModernTheme.createDangerButton("Delete Asset");
        btnDelete.addActionListener(e -> handleDelete());

        actionBar.add(btnAdd);
        actionBar.add(btnEdit);
        actionBar.add(btnAdjust);
        actionBar.add(btnIssue);
        actionBar.add(btnDelete);

        add(actionBar, BorderLayout.SOUTH);
    }

    public void loadFilters() {
        cmbCategory.removeAllItems();
        cmbCategory.addItem(new ComboItem(0, "All Categories"));
        for (Category cat : inventoryService.getCategoryDao().getAll()) {
            cmbCategory.addItem(new ComboItem(cat.getId(), cat.getName()));
        }

        cmbDepartment.removeAllItems();
        cmbDepartment.addItem(new ComboItem(0, "All Departments"));
        for (Department d : inventoryService.getDepartmentDao().getAll()) {
            cmbDepartment.addItem(new ComboItem(d.getId(), d.getCode()));
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        String keyword = txtSearch.getText().trim();
        ComboItem cat = (ComboItem) cmbCategory.getSelectedItem();
        ComboItem dept = (ComboItem) cmbDepartment.getSelectedItem();
        boolean lowOnly = chkLowStockOnly.isSelected();

        Integer catId = (cat != null && cat.id > 0) ? cat.id : null;
        Integer deptId = (dept != null && dept.id > 0) ? dept.id : null;

        displayedItems = inventoryService.getItemDao().search(keyword, catId, deptId, lowOnly);

        NumberFormat cur = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        for (Item it : displayedItems) {
            String stockStatus = it.isLowStock() ? "LOW STOCK" : "IN STOCK";
            tableModel.addRow(new Object[]{
                it.getId(),
                it.getItemCode(),
                it.getName(),
                it.getCategoryName() != null ? it.getCategoryName() : "-",
                it.getDepartmentName() != null ? it.getDepartmentName() : "-",
                it.getQuantity(),
                it.getMinThreshold(),
                cur.format(it.getUnitPrice()),
                cur.format(it.getTotalValuation()),
                it.getLocation() != null ? it.getLocation() : "-",
                it.getConditionStatus(),
                stockStatus
            });
        }
    }

    private Item getSelectedItem() {
        int row = tblItems.getSelectedRow();
        if (row >= 0 && row < displayedItems.size()) {
            return displayedItems.get(row);
        }
        return null;
    }

    private void handleEdit() {
        Item selected = getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an asset to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ItemDialog dlg = new ItemDialog(SwingUtilities.getWindowAncestor(this), inventoryService, selected, mainFrame.getCurrentUser().getFullName());
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshTable();
            mainFrame.refreshDashboard();
        }
    }

    private void handleStockAdjust() {
        Item selected = getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an asset to adjust stock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StockAdjustDialog dlg = new StockAdjustDialog(SwingUtilities.getWindowAncestor(this), inventoryService, selected, mainFrame.getCurrentUser().getFullName());
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            refreshTable();
            mainFrame.refreshDashboard();
        }
    }

    private void handleIssueSelected() {
        Item selected = getSelectedItem();
        mainFrame.openIssueDialog(selected);
    }

    private void handleDelete() {
        Item selected = getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an asset to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete item [" + selected.getItemCode() + " - " + selected.getName() + "]?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            String res = inventoryService.deleteItem(selected.getId());
            if ("SUCCESS".equalsIgnoreCase(res)) {
                JOptionPane.showMessageDialog(this, "Asset deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, res, "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
