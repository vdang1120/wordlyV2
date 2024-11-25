import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class UIManager {
    private final CSWordleGame gameLogic;
    private JPanel startPanel;          // main 3 panels
    private JPanel gamePanel;           
    private JPanel historyPanel;
    private JLabel[][] grid;            
    private JLabel usedLetters;         // so it can be accessed by updateKeyboard
    private JFrame frame;

    // Constructor
    public UIManager(CSWordleGame gameLogic) {
        this.gameLogic = gameLogic;
        this.grid = new JLabel[CSWordleGame.MAX_ATTEMPTS][CSWordleGame.WORD_LENGTH];
        initializeFrame();
    }

    private void initializeFrame() {
        frame = new JFrame("Wordly");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 770);
        frame.setLocationRelativeTo(null);
        
        createStartScreen();                        // makes window basically
        frame.addKeyListener(new KeyAdapter() {     // for letter input
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        gameLogic.handleKeyPress(e, grid);
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    private void createStartScreen() {
        startPanel = new JPanel();
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Wordly");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playButton = createStyledButton("Play");
        JButton csButton = createStyledButton("Play CSE Mode");
        JButton histButton = createStyledButton("Game History");

        playButton.addActionListener(e -> startGame(false));
        csButton.addActionListener(e -> startGame(true));
        histButton.addActionListener(e -> showHistory());

        startPanel.add(titleLabel);
        startPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        startPanel.add(playButton);
        startPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        startPanel.add(csButton);
        startPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        startPanel.add(histButton);

        frame.add(startPanel);
    }

    private void startGame(boolean csMode) {
        startPanel.setVisible(false);
        frame.remove(startPanel);
        
        gameLogic.startNewGame(csMode);
        createGamePanel();
        frame.add(gamePanel);
        frame.revalidate();
        frame.repaint();
        frame.requestFocus();
    }

    public void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());

        // top title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Wordly: " + (gameLogic.isCSMode() ? "CSE Mode" : "Normal Mode"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // grid panel
        JPanel gridPanel = new JPanel(new GridLayout(6, 5, 5, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gridPanel.setBackground(Color.BLACK);

        // bottom panel, you can use html :O
        JPanel letterPanel = new JPanel();
        usedLetters = new JLabel("<html>A E I O U<br>B C D F G H J K L M N<br>P Q R S T V W X Y Z</html>");
        usedLetters.setFont(new Font("Arial", Font.BOLD, 32));
        letterPanel.add(usedLetters);

        // grid setup
        for (int i = 0; i < CSWordleGame.MAX_ATTEMPTS; i++) {
            for (int j = 0; j < CSWordleGame.WORD_LENGTH; j++) {
                grid[i][j] = new JLabel();
                grid[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                grid[i][j].setFont(new Font("Arial", Font.BOLD, 40));
                grid[i][j].setOpaque(true);
                grid[i][j].setBackground(Color.WHITE);
                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                gridPanel.add(grid[i][j]);
            }
        }

        gamePanel.add(titlePanel, BorderLayout.NORTH);
        gamePanel.add(gridPanel, BorderLayout.CENTER);
        gamePanel.add(letterPanel, BorderLayout.SOUTH);
    }

    public void updateKeyboard(String guess) {              // deletes used letter from letterPanel
        String currentText = usedLetters.getText();
        for (char c : guess.toCharArray()) {
            currentText = currentText.replace(" " + c + " ", "   ");
            currentText = currentText.replace(c + " ", "  ");
            currentText = currentText.replace(" " + c, "  ");
            currentText = currentText.replace(String.valueOf(c), " ");
        }
        usedLetters.setText(currentText);
    }

    private JButton createStyledButton(String text) {       // button style
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 50));
        button.setPreferredSize(new Dimension(250, 50));
        button.setBackground(new Color(106, 170, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    public void returnToMainMenu() {
        frame.getContentPane().removeAll();
        frame.setJMenuBar(null);
        createStartScreen();
        frame.add(startPanel);
        gameLogic.resetGame();
        frame.revalidate();
        frame.repaint();
    }

    private void showHistory() {
        startPanel.setVisible(false);
        frame.remove(startPanel);

        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Game History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel historyContent = new JPanel();
        historyContent.setLayout(new BoxLayout(historyContent, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(historyContent);       // histContent into scrollPane
        scrollPane.setPreferredSize(new Dimension(400, 400));           // adds scrollbar if too many lines
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(13);

        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JLabel historyEntry = new JLabel(line);
                historyEntry.setFont(new Font("Arial", Font.BOLD, 24));
                historyEntry.setAlignmentX(Component.CENTER_ALIGNMENT);
                historyContent.add(historyEntry);
                historyContent.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (IOException e) {
            JLabel historyEntry = new JLabel("Error Getting History :/");
            historyEntry.setFont(new Font("Arial", Font.BOLD, 24));
            historyEntry.setAlignmentX(Component.CENTER_ALIGNMENT);
            historyContent.add(historyEntry);
        }

        JButton backButton = createStyledButton("Back to Menu");
        backButton.addActionListener(e -> returnToMainMenu());

        
        historyPanel.add(titleLabel);                                   // assemble historyPanel
        historyPanel.add(Box.createRigidArea(new Dimension(0, 32)));
        historyPanel.add(scrollPane);
        historyPanel.add(Box.createRigidArea(new Dimension(0, 32)));
        historyPanel.add(backButton);

        frame.add(historyPanel);
        frame.revalidate();
        frame.repaint();
    }

    public JLabel[][] getGrid() {
        return grid;
    }

    public void showEndGameDialog(boolean isWin, String targetWord) {
        String title = isWin ? "You Win!" : "Game Over";
        String message = isWin ? "Congratulations! You win!" : "Game Over! The word was: " + targetWord;

        int option = JOptionPane.showOptionDialog(
                frame,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Play Again", "Exit" },
                "GG");

        if (option == 0) {
            returnToMainMenu();
        } else {
            System.exit(0);
        }
    }
}