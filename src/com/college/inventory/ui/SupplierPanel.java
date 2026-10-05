package com.college.inventory.ui;

import com.college.inventory.model.Supplier;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SupplierPanel extends JPanel {
    private final InventoryService inventoryService;
    private JTable tblSuppliers;
    private DefaultTableModel tableModel;
    private List<Supplier> suppliers;

    public SupplierPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("College Equipment Vendors & Suppliers");
        lblTitle.setFont(ModernTheme.FONT_HEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JButton btnAdd = ModernTheme.createPrimaryButton("+ Add Vendor");
        btnAdd.addActionListener(e -> showSupplierDialog(null));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Company / Vendor Name", "Contact Person", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSuppliers = new JTable(tableModel);
        tblSuppliers.setRowHeight(30);
        tblSuppliers.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(tblSuppliers);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);

        JButton btnEdit = ModernTheme.createSecondaryButton("Edit Vendor");
        btnEdit.addActionListener(e -> {
            int row = tblSuppliers.getSelectedRow();
            if (row >= 0 && row < suppliers.size()) {
                showSupplierDialog(suppliers.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Select a vendor to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDelete = ModernTheme.createDangerButton("Delete Vendor");
        btnDelete.addActionListener(e -> {
            int row = tblSuppliers.getSelectedRow();
            if (row >= 0 && row < suppliers.size()) {
                Supplier s = suppliers.get(row);
                int c = JOptionPane.showConfirmDialog(this, "Delete supplier " + s.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    inventoryService.getSupplierDao().delete(s.getId());
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a vendor to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(btnEdit);
        footer.add(btnDelete);
        add(footer, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        suppliers = inventoryService.getSupplierDao().getAll();
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getName(),
                s.getContactPerson(),
                s.getPhone(),
                s.getEmail(),
                s.getAddress()
            });
        }
    }

    private void showSupplierDialog(Supplier supplierToEdit) {
        JTextField txtName = new JTextField(supplierToEdit != null ? supplierToEdit.getName() : "");
        JTextField txtContact = new JTextField(supplierToEdit != null ? supplierToEdit.getContactPerson() : "");
        JTextField txtPhone = new JTextField(supplierToEdit != null ? supplierToEdit.getPhone() : "");
        JTextField txtEmail = new JTextField(supplierToEdit != null ? supplierToEdit.getEmail() : "");
        JTextField txtAddress = new JTextField(supplierToEdit != null ? supplierToEdit.getAddress() : "");

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.add(new JLabel("Company Name:"));
        p.add(txtName);
        p.add(new JLabel("Contact Person:"));
        p.add(txtContact);
        p.add(new JLabel("Phone:"));
        p.add(txtPhone);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Address:"));
        p.add(txtAddress);

        int res = JOptionPane.showConfirmDialog(
            this, p,
            supplierToEdit == null ? "Add New Supplier" : "Edit Supplier",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Company Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (supplierToEdit == null) {
                Supplier newS = new Supplier(name, txtContact.getText().trim(), txtPhone.getText().trim(), txtEmail.getText().trim(), txtAddress.getText().trim());
                inventoryService.getSupplierDao().add(newS);
            } else {
                supplierToEdit.setName(name);
                supplierToEdit.setContactPerson(txtContact.getText().trim());
                supplierToEdit.setPhone(txtPhone.getText().trim());
                supplierToEdit.setEmail(txtEmail.getText().trim());
                supplierToEdit.setAddress(txtAddress.getText().trim());
                inventoryService.getSupplierDao().update(supplierToEdit);
            }
            refreshTable();
        }
    }
}
