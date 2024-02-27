package com.webdevwizards.revsGUI;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.webdevwizards.revsGUI.screens.*;

public class App {
    
    private static JFrame currentScreen;

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        switchToLoginScreen();
    }

    public static void switchToLoginScreen() {
        currentScreen = new LoginScreen(App::switchToMainScreen, App::switchToManagerScreen);
        currentScreen.setVisible(true);
    }

    public static void switchToCashierScreen() {
        currentScreen.dispose();
        currentScreen = new CashierScreen();
        currentScreen.setVisible(true);
    }

    public static void switchToManagerScreen() {
        currentScreen.dispose();
        currentScreen = new ManagerScreen();
        currentScreen.setVisible(true);
    }

    public static void switchToTestScreen() {
        currentScreen.dispose();
        currentScreen = new TestScreen();
        currentScreen.setVisible(true);
    }
}
