package com.college.inventory.ui;

import com.college.inventory.model.Item;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StockAdjustDialog extends JDialog {
    private final InventoryService inventoryService;
    private final Item item;
    private final String currentUserName;

    private JComboBox<String> cmbAction;
    private JSpinner spnDelta;
    private JTextField txtReason;
    private JLabel lblResult;

    private boolean completed = false;

    public StockAdjustDialog(Window parent, InventoryService inventoryService, Item item, String currentUserName) {
        super(parent, "Adjust Stock: " + item.getItemCode(), ModalityType.APPLICATION_MODAL);
        this.inventoryService = inventoryService;
        this.item = item;
        this.currentUserName = currentUserName;

        initComponents();
        setSize(460, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(ModernTheme.BG_DARK);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Adjust Stock for: " + item.getName());
        title.setFont(ModernTheme.FONT_SUBHEADER);
        title.setForeground(ModernTheme.PRIMARY_DARK);
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        c.gridx = 0; c.gridy = row; c.weightx = 0.4;
        form.add(new JLabel("Current Stock Level:"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 0.6;
        JLabel lblCurrent = new JLabel(item.getQuantity() + " units");
        lblCurrent.setFont(ModernTheme.FONT_BOLD);
        form.add(lblCurrent, c);
        row++;

        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Adjustment Action:"), c);
        c.gridx = 1; c.gridy = row;
        cmbAction = new JComboBox<>(new String[]{"Add Stock (Restock / Received)", "Deduct Stock (Damaged / Scrapped)", "Manual Correction"});
        cmbAction.addActionListener(e -> updateExpected());
        form.add(cmbAction, c);
        row++;

        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Units to Change:"), c);
        c.gridx = 1; c.gridy = row;
        spnDelta = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        spnDelta.addChangeListener(e -> updateExpected());
        form.add(spnDelta, c);
        row++;

        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Resulting Stock:"), c);
        c.gridx = 1; c.gridy = row;
        lblResult = new JLabel();
        lblResult.setFont(ModernTheme.FONT_BOLD);
        form.add(lblResult, c);
        row++;

        c.gridx = 0; c.gridy = row;
        form.add(new JLabel("Reason / PO Number:*"), c);
        c.gridx = 1; c.gridy = row;
        txtReason = new JTextField();
        form.add(txtReason, c);
        row++;

        updateExpected();
        content.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnConfirm = ModernTheme.createPrimaryButton("Confirm Adjustment");
        btnConfirm.addActionListener(e -> handleConfirm());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void updateExpected() {
        int delta = (Integer) spnDelta.getValue();
        int actionIdx = cmbAction.getSelectedIndex();
        int resulting = item.getQuantity();

        if (actionIdx == 0) { // Add
            resulting += delta;
        } else { // Deduct or Correction
            resulting -= delta;
        }

        if (resulting < 0) {
            lblResult.setText(resulting + " units (INVALID: Negative stock!)");
            lblResult.setForeground(ModernTheme.ACCENT_RED);
        } else {
            lblResult.setText(resulting + " units");
            lblResult.setForeground(ModernTheme.TEXT_MAIN);
        }
    }

    private void handleConfirm() {
        int delta = (Integer) spnDelta.getValue();
        int actionIdx = cmbAction.getSelectedIndex();
        String reason = txtReason.getText().trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a reason or reference note for this stock adjustment.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantityChange;
        String txType;
        if (actionIdx == 0) {
            quantityChange = delta;
            txType = "STOCK_IN";
        } else if (actionIdx == 1) {
            quantityChange = -delta;
            txType = "SCRAP";
        } else {
            quantityChange = -delta;
            txType = "ADJUSTMENT";
        }

        String res = inventoryService.adjustStock(item.getId(), quantityChange, txType, reason, currentUserName);
        if ("SUCCESS".equalsIgnoreCase(res)) {
            completed = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, res, "Adjustment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
