package com.webdevwizards.revsGUI;

import javax.swing.JFrame;
import com.webdevwizards.revsGUI.screens.LoginScreen;
import com.webdevwizards.revsGUI.screens.MainScreen;
import com.webdevwizards.revsGUI.screens.TestScreen;

public class App {
    
    private static JFrame currentScreen;

    public static void main(String[] args) {
        DatabaseManager.initialize();
        switchToLoginScreen();
    }

    public static void switchToLoginScreen() {
        currentScreen = new LoginScreen(App::switchToTestScreen);
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

}
