package com.college.inventory;

import com.college.inventory.cli.ConsoleApp;
import com.college.inventory.config.DatabaseManager;
import com.college.inventory.service.AuthService;
import com.college.inventory.service.InventoryService;
import com.college.inventory.ui.LoginDialog;
import com.college.inventory.ui.MainFrame;
import com.college.inventory.ui.ModernTheme;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Starting CampusAsset Pro College Inventory...  ");
        System.out.println("=================================================");

        // 1. Initialize SQLite Database & Tables & Seed Sample Data
        DatabaseManager.initializeDatabase();

        // 2. Initialize Shared Services
        InventoryService inventoryService = new InventoryService();
        AuthService authService = new AuthService();

        // Check if CLI mode was requested or if environment is headless
        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode || GraphicsEnvironment.isHeadless()) {
            System.out.println("[Mode] Launching Terminal / Console CLI Interface...");
            ConsoleApp app = new ConsoleApp(inventoryService, authService);
            app.start();
        } else {
            System.out.println("[Mode] Launching Modern Desktop GUI...");
            ModernTheme.setupLookAndFeel();

            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null, authService);
                loginDialog.setVisible(true);

                if (loginDialog.isSucceeded()) {
                    MainFrame mainFrame = new MainFrame(inventoryService, authService);
                    mainFrame.setVisible(true);
                } else {
                    System.out.println("[System] Login cancelled. Exiting.");
                    System.exit(0);
                }
            });
        }
    }
}
