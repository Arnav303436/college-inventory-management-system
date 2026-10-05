package com.college.inventory.ui;

import com.college.inventory.model.Category;
import com.college.inventory.model.Department;
import com.college.inventory.model.Item;
import com.college.inventory.model.Supplier;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ItemDialog extends JDialog {
    private final InventoryService inventoryService;
    private final Item existingItem;
    private final String currentUserName;

    private JTextField txtCode;
    private JTextField txtName;
    private JComboBox<CategoryItem> cmbCategory;
    private JComboBox<DepartmentItem> cmbDepartment;
    private JComboBox<SupplierItem> cmbSupplier;
    private JSpinner spnQuantity;
    private JSpinner spnMinThreshold;
    private JTextField txtUnitPrice;
    private JTextField txtLocation;
    private JComboBox<String> cmbCondition;
    private JTextArea txtDescription;

    private boolean saved = false;

    // Helper item wrappers for JComboBox
    private static class CategoryItem {
        final int id;
        final String name;
        CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class DepartmentItem {
        final int id;
        final String name;
        DepartmentItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class SupplierItem {
        final int id;
        final String name;
        SupplierItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public ItemDialog(Window parent, InventoryService inventoryService, Item itemToEdit, String currentUserName) {
        super(parent, itemToEdit == null ? "Add New Inventory Asset" : "Edit Asset: " + itemToEdit.getItemCode(), ModalityType.APPLICATION_MODAL);
        this.inventoryService = inventoryService;
        this.existingItem = itemToEdit;
        this.currentUserName = currentUserName;

        initComponents();
        setSize(560, 640);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(ModernTheme.BG_DARK);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header Title
        JLabel lblHeading = new JLabel(existingItem == null ? "Register New College Equipment" : "Modify Equipment Record");
        lblHeading.setFont(ModernTheme.FONT_HEADER);
        lblHeading.setForeground(ModernTheme.PRIMARY_DARK);
        main.add(lblHeading, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 6, 5, 6);

        int row = 0;

        // Item Code
        c.gridx = 0; c.gridy = row; c.weightx = 0.3;
        form.add(new JLabel("Asset / Item Code:*"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 0.7;
        txtCode = new JTextField(existingItem != null ? existingItem.getItemCode() : "");
        form.add(txtCode, c);
        row++;

        // Name
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Equipment / Item Name:*"), c);
        c.gridx = 1; c.gridy = row;
        txtName = new JTextField(existingItem != null ? existingItem.getName() : "");
        form.add(txtName, c);
        row++;

        // Category
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Category:"), c);
        c.gridx = 1; c.gridy = row;
        cmbCategory = new JComboBox<>();
        cmbCategory.addItem(new CategoryItem(0, "-- Select Category --"));
        List<Category> categories = inventoryService.getCategoryDao().getAll();
        CategoryItem selectedCat = null;
        for (Category cat : categories) {
            CategoryItem ci = new CategoryItem(cat.getId(), cat.getName());
            cmbCategory.addItem(ci);
            if (existingItem != null && existingItem.getCategoryId() == cat.getId()) {
                selectedCat = ci;
            }
        }
        if (selectedCat != null) cmbCategory.setSelectedItem(selectedCat);
        form.add(cmbCategory, c);
        row++;

        // Department
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Department / Lab:"), c);
        c.gridx = 1; c.gridy = row;
        cmbDepartment = new JComboBox<>();
        cmbDepartment.addItem(new DepartmentItem(0, "-- Select Department --"));
        List<Department> departments = inventoryService.getDepartmentDao().getAll();
        DepartmentItem selectedDept = null;
        for (Department dept : departments) {
            DepartmentItem di = new DepartmentItem(dept.getId(), dept.getCode() + " - " + dept.getName());
            cmbDepartment.addItem(di);
            if (existingItem != null && existingItem.getDepartmentId() == dept.getId()) {
                selectedDept = di;
            }
        }
        if (selectedDept != null) cmbDepartment.setSelectedItem(selectedDept);
        form.add(cmbDepartment, c);
        row++;

        // Supplier
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Supplier / Vendor:"), c);
        c.gridx = 1; c.gridy = row;
        cmbSupplier = new JComboBox<>();
        cmbSupplier.addItem(new SupplierItem(0, "-- Select Supplier --"));
        List<Supplier> suppliers = inventoryService.getSupplierDao().getAll();
        SupplierItem selectedSup = null;
        for (Supplier sup : suppliers) {
            SupplierItem si = new SupplierItem(sup.getId(), sup.getName());
            cmbSupplier.addItem(si);
            if (existingItem != null && existingItem.getSupplierId() == sup.getId()) {
                selectedSup = si;
            }
        }
        if (selectedSup != null) cmbSupplier.setSelectedItem(selectedSup);
        form.add(cmbSupplier, c);
        row++;

        // Quantity & Min Threshold side-by-side
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Initial Stock Units:*"), c);
        c.gridx = 1; c.gridy = row;
        spnQuantity = new JSpinner(new SpinnerNumberModel(existingItem != null ? existingItem.getQuantity() : 1, 0, 99999, 1));
        form.add(spnQuantity, c);
        row++;

        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Low Stock Threshold:*"), c);
        c.gridx = 1; c.gridy = row;
        spnMinThreshold = new JSpinner(new SpinnerNumberModel(existingItem != null ? existingItem.getMinThreshold() : 5, 0, 9999, 1));
        form.add(spnMinThreshold, c);
        row++;

        // Unit Price
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Unit Price (INR ₹):"), c);
        c.gridx = 1; c.gridy = row;
        txtUnitPrice = new JTextField(existingItem != null ? String.valueOf(existingItem.getUnitPrice()) : "0.0");
        form.add(txtUnitPrice, c);
        row++;

        // Physical Location
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Lab / Room / Rack:"), c);
        c.gridx = 1; c.gridy = row;
        txtLocation = new JTextField(existingItem != null ? existingItem.getLocation() : "");
        form.add(txtLocation, c);
        row++;

        // Condition
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Condition:"), c);
        c.gridx = 1; c.gridy = row;
        cmbCondition = new JComboBox<>(new String[]{"Working", "Under Maintenance", "Damaged", "Scrapped"});
        if (existingItem != null && existingItem.getConditionStatus() != null) {
            cmbCondition.setSelectedItem(existingItem.getConditionStatus());
        }
        form.add(cmbCondition, c);
        row++;

        // Description / Specs
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Description / Specs:"), c);
        c.gridx = 1; c.gridy = row;
        txtDescription = new JTextArea(existingItem != null ? existingItem.getDescription() : "", 3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescription), c);
        row++;

        main.add(new JScrollPane(form), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ModernTheme.createPrimaryButton("Save Asset");
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void handleSave() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Code is mandatory.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item Name is mandatory.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double unitPrice = 0.0;
        try {
            unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());
            if (unitPrice < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for Unit Price.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (Integer) spnQuantity.getValue();
        int minThreshold = (Integer) spnMinThreshold.getValue();
        String location = txtLocation.getText().trim();
        String condition = (String) cmbCondition.getSelectedItem();
        String desc = txtDescription.getText().trim();

        CategoryItem catItem = (CategoryItem) cmbCategory.getSelectedItem();
        int catId = (catItem != null && catItem.id > 0) ? catItem.id : 0;

        DepartmentItem deptItem = (DepartmentItem) cmbDepartment.getSelectedItem();
        int deptId = (deptItem != null && deptItem.id > 0) ? deptItem.id : 0;

        SupplierItem supItem = (SupplierItem) cmbSupplier.getSelectedItem();
        int supId = (supItem != null && supItem.id > 0) ? supItem.id : 0;

        String result;
        if (existingItem == null) {
            Item newItem = new Item(code, name, catId, deptId, quantity, minThreshold, unitPrice, location, condition, supId, desc);
            result = inventoryService.addItem(newItem, currentUserName);
        } else {
            existingItem.setItemCode(code);
            existingItem.setName(name);
            existingItem.setCategoryId(catId);
            existingItem.setDepartmentId(deptId);
            existingItem.setQuantity(quantity);
            existingItem.setMinThreshold(minThreshold);
            existingItem.setUnitPrice(unitPrice);
            existingItem.setLocation(location);
            existingItem.setConditionStatus(condition);
            existingItem.setSupplierId(supId);
            existingItem.setDescription(desc);
            result = inventoryService.updateItem(existingItem, currentUserName);
        }

        if ("SUCCESS".equalsIgnoreCase(result)) {
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
