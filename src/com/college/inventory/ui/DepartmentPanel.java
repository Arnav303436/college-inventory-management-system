package com.college.inventory.ui;

import com.college.inventory.model.Department;
import com.college.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DepartmentPanel extends JPanel {
    private final InventoryService inventoryService;
    private JTable tblDepts;
    private DefaultTableModel tableModel;
    private List<Department> departments;

    public DepartmentPanel(InventoryService inventoryService) {
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

        JLabel lblTitle = new JLabel("Academic Departments & Specialized Labs");
        lblTitle.setFont(ModernTheme.FONT_HEADER);
        lblTitle.setForeground(ModernTheme.PRIMARY_DARK);

        JButton btnAdd = ModernTheme.createPrimaryButton("+ Add Department");
        btnAdd.addActionListener(e -> showDeptDialog(null));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Dept Code", "Department Name", "Head of Department (HOD)", "Official Email"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDepts = new JTable(tableModel);
        tblDepts.setRowHeight(30);
        tblDepts.getColumnModel().getColumn(0).setMaxWidth(50);
        tblDepts.getColumnModel().getColumn(1).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(tblDepts);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernTheme.BORDER_COLOR, 1, true));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);

        JButton btnEdit = ModernTheme.createSecondaryButton("Edit Department");
        btnEdit.addActionListener(e -> {
            int row = tblDepts.getSelectedRow();
            if (row >= 0 && row < departments.size()) {
                showDeptDialog(departments.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Select a department to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(btnEdit);
        add(footer, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        departments = inventoryService.getDepartmentDao().getAll();
        for (Department d : departments) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getHeadOfDepartment(),
                d.getContactEmail()
            });
        }
    }

    private void showDeptDialog(Department deptToEdit) {
        JTextField txtCode = new JTextField(deptToEdit != null ? deptToEdit.getCode() : "");
        JTextField txtName = new JTextField(deptToEdit != null ? deptToEdit.getName() : "");
        JTextField txtHod = new JTextField(deptToEdit != null ? deptToEdit.getHeadOfDepartment() : "");
        JTextField txtEmail = new JTextField(deptToEdit != null ? deptToEdit.getContactEmail() : "");

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Department Code (e.g. AI-ML):"));
        p.add(txtCode);
        p.add(new JLabel("Full Name:"));
        p.add(txtName);
        p.add(new JLabel("Head of Dept (HOD):"));
        p.add(txtHod);
        p.add(new JLabel("Official Email:"));
        p.add(txtEmail);

        int res = JOptionPane.showConfirmDialog(
            this, p,
            deptToEdit == null ? "Add Academic Department" : "Edit Department",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Code and Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (deptToEdit == null) {
                Department newD = new Department(code, name, txtHod.getText().trim(), txtEmail.getText().trim());
                inventoryService.getDepartmentDao().add(newD);
            } else {
                deptToEdit.setCode(code);
                deptToEdit.setName(name);
                deptToEdit.setHeadOfDepartment(txtHod.getText().trim());
                deptToEdit.setContactEmail(txtEmail.getText().trim());
                inventoryService.getDepartmentDao().update(deptToEdit);
            }
            refreshTable();
        }
    }
}
