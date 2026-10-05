package com.college.inventory.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Modern styling utilities, color palettes, card generators, and table cell renderers.
 */
public class ModernTheme {
    // Primary Colors
    public static final Color PRIMARY = new Color(24, 119, 242);
    public static final Color PRIMARY_DARK = new Color(13, 82, 173);
    public static final Color ACCENT_GREEN = new Color(34, 197, 94);
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);
    public static final Color ACCENT_RED = new Color(239, 68, 68);
    public static final Color ACCENT_PURPLE = new Color(147, 51, 234);

    // Dark/Neutral tones
    public static final Color BG_DARK = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 26);

    public static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf Light theme
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
            
            // Customize default UI properties
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("TabbedPane.tabHeight", 38);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Table.selectionBackground", new Color(224, 238, 255));
            UIManager.put("Table.selectionForeground", TEXT_MAIN);
        } catch (Throwable t) {
            // Fallback to system look and feel if FlatLaf isn't available
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_STAT_NUM);
        valLbl.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(ACCENT_RED);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static DefaultTableCellRenderer getStatusCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String text = (value != null) ? value.toString() : "";
                l.setFont(FONT_BOLD);

                if (!isSelected) {
                    if ("LOW STOCK".equalsIgnoreCase(text) || "OVERDUE".equalsIgnoreCase(text) || "DAMAGED".equalsIgnoreCase(text)) {
                        l.setForeground(ACCENT_RED);
                        l.setBackground(new Color(254, 242, 242));
                    } else if ("IN STOCK".equalsIgnoreCase(text) || "WORKING".equalsIgnoreCase(text) || "RETURNED".equalsIgnoreCase(text)) {
                        l.setForeground(new Color(22, 101, 52));
                        l.setBackground(new Color(240, 253, 244));
                    } else if ("ISSUED".equalsIgnoreCase(text)) {
                        l.setForeground(PRIMARY_DARK);
                        l.setBackground(new Color(239, 246, 255));
                    } else {
                        l.setForeground(TEXT_MAIN);
                        l.setBackground(Color.WHITE);
                    }
                }
                return l;
            }
        };
    }
}
