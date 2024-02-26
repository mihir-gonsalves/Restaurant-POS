package com.webdevwizards.revsGUI;

import javax.swing.JFrame;
import com.webdevwizards.revsGUI.screens.LoginScreen;
import com.webdevwizards.revsGUI.screens.MainScreen;

public class App {
    
    private static JFrame currentScreen;

    public static void main(String[] args) {
        switchToLoginScreen();
    }

    public static void switchToLoginScreen() {
        currentScreen = new LoginScreen(App::switchToMainScreen);
        currentScreen.setVisible(true);
    }

    public static void switchToMainScreen() {
        currentScreen.dispose();
        currentScreen = new MainScreen();
        currentScreen.setVisible(true);
    }

}
