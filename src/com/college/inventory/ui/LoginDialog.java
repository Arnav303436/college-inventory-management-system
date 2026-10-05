package com.college.inventory.ui;

import com.college.inventory.model.User;
import com.college.inventory.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final AuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private boolean succeeded = false;

    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "College Inventory System - Login", true);
        this.authService = authService;

        initComponents();
        setSize(420, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(ModernTheme.BG_DARK);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setOpaque(false);
        JLabel title = new JLabel("College Inventory System", SwingConstants.CENTER);
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Sign in with staff or administrator credentials", SwingConstants.CENTER);
        subtitle.setFont(ModernTheme.FONT_REGULAR);
        subtitle.setForeground(ModernTheme.TEXT_MUTED);

        header.add(title);
        header.add(subtitle);
        content.add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(ModernTheme.FONT_BOLD);
        form.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        txtUsername = new JTextField("admin");
        txtUsername.setFont(ModernTheme.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(200, 32));
        form.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(ModernTheme.FONT_BOLD);
        form.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(ModernTheme.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(200, 32));
        form.add(txtPassword, gbc);

        // Hint note
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel hint = new JLabel("<html><center><font color='#64748b'>Default Logins: <b>admin / admin123</b> (Full Admin)<br>or <b>cs_incharge / lab123</b> (Lab Staff)</font></center></html>", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        form.add(hint, gbc);

        content.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnExit = ModernTheme.createSecondaryButton("Exit");
        btnExit.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JButton btnLogin = ModernTheme.createPrimaryButton("Sign In");
        btnLogin.addActionListener(e -> attemptLogin());

        // Enter key triggers login
        getRootPane().setDefaultButton(btnLogin);

        btnPanel.add(btnExit);
        btnPanel.add(btnLogin);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authService.login(username, password)) {
            succeeded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
