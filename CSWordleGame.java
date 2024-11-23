// By: Hoang Huynh, Van Thiang, and Vincent Dang

import javax.swing.*;       // swing - GUI
import java.awt.*;          // abstract windows toolkit - graphics
import java.awt.event.*;    // event handling
import java.io.*;           // reading from github and writing to history.txt
import java.net.URL;        // for github URL
import java.util.*;
import java.util.List;      // cause theres also awt.List

public class CSWordleGame extends JFrame {

    private JPanel startPanel;          // main 3 panels
    private JPanel gamePanel;           // colors from https://www.color-hex.com/color-palette/1012607
    private JPanel historyPanel;
    private JLabel[][] grid;            // grid label in gamePanel

    private static final Color CORRECT = new Color(108, 169, 101);
    private static final Color WRONG_POSITION = new Color(200, 182, 83);
    private static final Color WRONG = new Color(120, 124, 127);
    private static final int WORD_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 6;

    private String targetWord;
    private int currentRow = 0;         // for tracking current letter
    private int currentCol = 0;
    private boolean isCSMode = false;

    private List<String> regularWords;  // word lists
    private List<String> CSWords = Arrays.asList(
            "array", "class", "debug", "error", "float", "index", "logic", "queue", "stack", "parse",
            "value", "while", "input", "valid", "write", "break", "token", "shift", "coder", "tests",
            "throw", "false", "heaps", "codes", "final", "chars", "loops", "super", "child", "catch");

    public CSWordleGame() {                                 // constructor
        regularWords = loadWords();

        setTitle("Wordly");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createStartScreen();                    // makes the window basically
        pack();
        setSize(550, 770);
        setLocationRelativeTo(null);
        addKeyListener(new KeyAdapter() {       // for letter input
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private List<String> loadWords() {                      // reads words from raw github file, load to words
        List<String> words = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/charlesreid1/five-letter-words/master/sgb-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 5) {
                    words.add(line.trim().toLowerCase());
                }
            }
            reader.close();
        } catch (Exception e) { // if unable to read, the only word will be 'error'
            words.addAll(Arrays.asList("error"));
        }
        return words;
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

        add(startPanel);
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

    private void startGame(boolean csMode) {                // starts game, picks target word
        isCSMode = csMode;
        startPanel.setVisible(false);
        remove(startPanel);

        List<String> wordList = csMode ? CSWords : regularWords;
        targetWord = wordList.get(new Random().nextInt(wordList.size())).toUpperCase();

        createGamePanel();
        add(gamePanel);
        revalidate();
        repaint();
        requestFocus();
    }

    private JLabel usedLetters;                             // so it can be accessed by updateKeyboard

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout());

        // top title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Wordly: " + (isCSMode ? "CSE Mode" : "Normal Mode"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // grid panel
        JPanel gridPanel = new JPanel(new GridLayout(MAX_ATTEMPTS, WORD_LENGTH, 5, 5));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setBackground(Color.BLACK);

        // bottom panel, you can use html :O
        JPanel letterPanel = new JPanel();
        usedLetters = new JLabel("<html>A E I O U<br>B C D F G H J K L M N<br>P Q R S T V W X Y Z</html>");
        usedLetters.setFont(new Font("Arial", Font.BOLD, 32));
        letterPanel.add(usedLetters);

        // grid setup
        grid = new JLabel[MAX_ATTEMPTS][WORD_LENGTH];
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                grid[i][j] = new JLabel();
                grid[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                grid[i][j].setFont(new Font("Arial", Font.BOLD, 40));
                grid[i][j].setOpaque(true);
                grid[i][j].setBackground(Color.WHITE);
                grid[i][j].setBorder(BorderFactory.createLineBorder(Color.GRAY));
                gridPanel.add(grid[i][j]);
            }
        }

        gamePanel.add(titlePanel, BorderLayout.NORTH); // assembles gamePanel
        gamePanel.add(gridPanel, BorderLayout.CENTER);
        gamePanel.add(letterPanel, BorderLayout.SOUTH);
    }

    private void handleKeyPress(KeyEvent e) {
        if (currentRow >= MAX_ATTEMPTS)
            return;

        int keyCode = e.getKeyCode();
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z && currentCol < WORD_LENGTH) {
            grid[currentRow][currentCol].setText(String.valueOf((char) keyCode));
            currentCol++;
        } else if (keyCode == KeyEvent.VK_BACK_SPACE && currentCol > 0) {
            currentCol--;
            grid[currentRow][currentCol].setText("");
        } else if (keyCode == KeyEvent.VK_ENTER && currentCol == WORD_LENGTH) {
            submitGuess();
        }
    }

    private void submitGuess() {
        StringBuilder guess = new StringBuilder();      // guess to stringbuilder
        for (int i = 0; i < WORD_LENGTH; i++) {
            guess.append(grid[currentRow][i].getText());
        }

        String guessStr = guess.toString();             // stringbuilder to string & validate
        if (!regularWords.contains(guessStr.toLowerCase())) {
            return;
        }
        updateKeyboard(guessStr);

        // hashmap for letter freqs
        Map<Character, Integer> targetLetterCount = new HashMap<>();
        for (char c : targetWord.toCharArray()) {
            targetLetterCount.put(c, targetLetterCount.getOrDefault(c, 0) + 1);
        }

        // first pass: mark correct positions
        Map<Character, Integer> usedLetters = new HashMap<>();
        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);
            if (guessChar == targetWord.charAt(i)) {
                grid[currentRow][i].setBackground(CORRECT);
                grid[currentRow][i].setForeground(Color.WHITE);
                usedLetters.put(guessChar, usedLetters.getOrDefault(guessChar, 0) + 1);
            }
        }

        // second pass: mark wrong positions
        for (int i = 0; i < WORD_LENGTH; i++) {
            char guessChar = guess.charAt(i);
            if (guessChar != targetWord.charAt(i)) {
                if (targetWord.indexOf(guessChar) != -1 &&
                        usedLetters.getOrDefault(guessChar, 0) < targetLetterCount.getOrDefault(guessChar, 0)) {
                    grid[currentRow][i].setBackground(WRONG_POSITION);
                    usedLetters.put(guessChar, usedLetters.getOrDefault(guessChar, 0) + 1);
                } else {
                    grid[currentRow][i].setBackground(WRONG);
                }
                grid[currentRow][i].setForeground(Color.WHITE);
            }
        }

        // check for win
        if (guess.toString().equals(targetWord)) {
            storeGame(targetWord, currentRow);
            int option = JOptionPane.showOptionDialog(
                    null,
                    "Congratulations! You win!",
                    "You Win!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[] { "Play Again", "Exit" },
                    "Play Again");
            if (option == 0) {
                returnToMainMenu();
            } else {
                System.exit(0);
            }
            return;
        }

        currentRow++;
        currentCol = 0;

        // loss
        if (currentRow == MAX_ATTEMPTS) {
            storeGame(targetWord);
            int option = JOptionPane.showOptionDialog(
                    this,
                    "Game Over! The word was: " + targetWord,
                    "Game Over",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[] { "Play Again", "Exit" },
                    "Play Again");
            if (option == 0) {
                returnToMainMenu();
            } else {
                System.exit(0);
            }
        }
    }

    private void updateKeyboard(String guess) {             // deletes used letter from letterPanel
        String currentText = usedLetters.getText();
        for (char c : guess.toCharArray()) {
            currentText = currentText.replace(" " + c + " ", "   ");
            currentText = currentText.replace(c + " ", "  ");
            currentText = currentText.replace(" " + c, "  ");
            currentText = currentText.replace(String.valueOf(c), " ");
        }
        usedLetters.setText(currentText);
    }

    private void storeGame(String targetWord, int tries) {  // store game info to history.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("history.txt", true))) {
            writer.write(targetWord + " - " + (tries + 1) + " Tries");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeGame(String targetWord) {             // method overloading, polymorphism :D
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("history.txt", true))) {
            writer.write(targetWord + " - Failed");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnToMainMenu() {
        getContentPane().removeAll();
        setJMenuBar(null);
        createStartScreen();
        add(startPanel);
        currentRow = 0;
        currentCol = 0;
        revalidate();
        repaint();
    }

    private void showHistory() {
        startPanel.setVisible(false);
        remove(startPanel);

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

        // Read and display history from file
        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JLabel historyEntry = new JLabel(line);
                historyEntry.setFont(new Font("Arial", Font.BOLD, 24));
                historyEntry.setAlignmentX(Component.CENTER_ALIGNMENT); // align entry
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

        add(historyPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) { // main method runs game
        SwingUtilities.invokeLater(() -> {
            new CSWordleGame().setVisible(true);
        });
    }
}