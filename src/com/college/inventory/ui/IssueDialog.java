package com.college.inventory.ui;

import com.college.inventory.model.Item;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class IssueDialog extends JDialog {
    private final InventoryService inventoryService;
    private final String currentUserName;

    private JComboBox<ItemEntry> cmbItems;
    private JLabel lblAvailableStock;
    private JComboBox<String> cmbBorrowerType;
    private JTextField txtBorrowerId;
    private JTextField txtBorrowerName;
    private JTextField txtBorrowerEmail;
    private JTextField txtBorrowerDept;
    private JSpinner spnQuantity;
    private JTextField txtReturnDate;
    private JTextArea txtRemarks;

    private boolean issued = false;

    private static class ItemEntry {
        final Item item;
        ItemEntry(Item item) { this.item = item; }
        @Override
        public String toString() {
            return item.getItemCode() + " - " + item.getName() + " (In Stock: " + item.getQuantity() + ")";
        }
    }

    public IssueDialog(Window parent, InventoryService inventoryService, Item preselectedItem, String currentUserName) {
        super(parent, "Issue Equipment to Student / Faculty", ModalityType.APPLICATION_MODAL);
        this.inventoryService = inventoryService;
        this.currentUserName = currentUserName;

        initComponents(preselectedItem);
        setSize(540, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents(Item preselectedItem) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBackground(ModernTheme.BG_DARK);
        main.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Issue College Lab Equipment / Asset");
        title.setFont(ModernTheme.FONT_HEADER);
        title.setForeground(ModernTheme.PRIMARY_DARK);
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 6, 5, 6);

        int row = 0;

        // Select Item
        c.gridx = 0; c.gridy = row; c.weightx = 0.35;
        form.add(new JLabel("Select Asset/Item:*"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 0.65;
        cmbItems = new JComboBox<>();
        List<Item> items = inventoryService.getItemDao().getAll();
        ItemEntry selectedEntry = null;
        for (Item it : items) {
            ItemEntry entry = new ItemEntry(it);
            cmbItems.addItem(entry);
            if (preselectedItem != null && it.getId() == preselectedItem.getId()) {
                selectedEntry = entry;
            }
        }
        if (selectedEntry != null) {
            cmbItems.setSelectedItem(selectedEntry);
        }
        cmbItems.addActionListener(e -> updateStockLabel());
        form.add(cmbItems, c);
        row++;

        // Available Stock display
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Available Stock:"), c);
        c.gridx = 1; c.gridy = row;
        lblAvailableStock = new JLabel();
        lblAvailableStock.setFont(ModernTheme.FONT_BOLD);
        form.add(lblAvailableStock, c);
        row++;

        // Borrower Type
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Borrower Type:*"), c);
        c.gridx = 1; c.gridy = row;
        cmbBorrowerType = new JComboBox<>(new String[]{"STUDENT", "FACULTY", "STAFF"});
        form.add(cmbBorrowerType, c);
        row++;

        // Borrower ID / Roll No
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Roll No / Employee ID:*"), c);
        c.gridx = 1; c.gridy = row;
        txtBorrowerId = new JTextField();
        form.add(txtBorrowerId, c);
        row++;

        // Borrower Name
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Full Name:*"), c);
        c.gridx = 1; c.gridy = row;
        txtBorrowerName = new JTextField();
        form.add(txtBorrowerName, c);
        row++;

        // Borrower Email
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("College Email:"), c);
        c.gridx = 1; c.gridy = row;
        txtBorrowerEmail = new JTextField();
        form.add(txtBorrowerEmail, c);
        row++;

        // Borrower Department
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Department:"), c);
        c.gridx = 1; c.gridy = row;
        txtBorrowerDept = new JTextField("Computer Science");
        form.add(txtBorrowerDept, c);
        row++;

        // Quantity
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Quantity to Issue:*"), c);
        c.gridx = 1; c.gridy = row;
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        form.add(spnQuantity, c);
        row++;

        // Expected Return Date (default 7 days from now: YYYY-MM-DD)
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Expected Return Date:*"), c);
        c.gridx = 1; c.gridy = row;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtReturnDate = new JTextField(sdf.format(cal.getTime()));
        form.add(txtReturnDate, c);
        row++;

        // Remarks / Purpose
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Purpose / Project:"), c);
        c.gridx = 1; c.gridy = row;
        txtRemarks = new JTextArea(3, 20);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        form.add(new JScrollPane(txtRemarks), c);
        row++;

        updateStockLabel();

        main.add(new JScrollPane(form), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnIssue = ModernTheme.createPrimaryButton("Confirm & Issue");
        btnIssue.addActionListener(e -> handleIssue());

        btnPanel.add(btnCancel);
        btnPanel.add(btnIssue);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void updateStockLabel() {
        ItemEntry entry = (ItemEntry) cmbItems.getSelectedItem();
        if (entry != null) {
            lblAvailableStock.setText(entry.item.getQuantity() + " units in stock");
            if (entry.item.getQuantity() <= 0) {
                lblAvailableStock.setForeground(ModernTheme.ACCENT_RED);
            } else if (entry.item.isLowStock()) {
                lblAvailableStock.setForeground(ModernTheme.ACCENT_AMBER);
            } else {
                lblAvailableStock.setForeground(ModernTheme.ACCENT_GREEN);
            }
        }
    }

    private void handleIssue() {
        ItemEntry entry = (ItemEntry) cmbItems.getSelectedItem();
        if (entry == null) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String borrowerId = txtBorrowerId.getText().trim();
        String borrowerName = txtBorrowerName.getText().trim();
        String borrowerEmail = txtBorrowerEmail.getText().trim();
        String borrowerDept = txtBorrowerDept.getText().trim();
        String borrowerType = (String) cmbBorrowerType.getSelectedItem();
        int qty = (Integer) spnQuantity.getValue();
        String returnDate = txtReturnDate.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (borrowerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Borrower ID / Roll Number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (borrowerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Borrower Full Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (returnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Expected Return Date is required (format: YYYY-MM-DD).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String result = inventoryService.issueItem(
            entry.item.getId(),
            borrowerType,
            borrowerId,
            borrowerName,
            borrowerEmail,
            borrowerDept,
            qty,
            returnDate,
            remarks,
            currentUserName
        );

        if ("SUCCESS".equalsIgnoreCase(result)) {
            issued = true;
            JOptionPane.showMessageDialog(this, "Item successfully issued to " + borrowerName + "!", "Issue Completed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Issue Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isIssued() {
        return issued;
    }
}
