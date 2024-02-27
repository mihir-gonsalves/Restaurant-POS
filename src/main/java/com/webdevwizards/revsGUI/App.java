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
        currentScreen = new LoginScreen(App::switchToTestGUIScreen);
        currentScreen.setVisible(true);
    }

    public static void switchToMainScreen() {
        currentScreen.dispose();
        currentScreen = new MainScreen();
        currentScreen.setVisible(true);
    }

    public static void switchToTestScreen() {
        currentScreen.dispose();
        currentScreen = new TestScreen();
        currentScreen.setVisible(true);
    }

    public static void switchToTestGUIScreen() {
        currentScreen.dispose();
        currentScreen = new testGUI();
        currentScreen.setVisible(true);
    }

}
