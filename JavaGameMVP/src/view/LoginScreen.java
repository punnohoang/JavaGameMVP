package view;

import javax.swing.*;
import util.DatabaseManager;
import root.GameFrame;

public class LoginScreen extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField nameField;
    private JButton startButton;

    public LoginScreen() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        nameField = new JTextField(15);
        startButton = new JButton("Start");

        // Nhấn Enter trong nameField hoặc click nút Start đều bắt đầu game
        nameField.addActionListener(e -> startGame());
        startButton.addActionListener(e -> startGame());

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter your name:"));
        panel.add(nameField);
        panel.add(startButton);

        add(panel);
        setVisible(true);
    }

    private void startGame() {
        String playerName = nameField.getText();
        if (!playerName.isEmpty()) {
            DatabaseManager.insertNewPlayer(playerName);
            dispose();
            new GameFrame(playerName);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a name to start the game.");
        }
    }
}
