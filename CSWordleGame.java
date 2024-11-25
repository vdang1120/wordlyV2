// By: Hoang Huynh, Van Thiang, and Vincent Dang

import javax.swing.*;           // swing - GUI
import java.awt.*;              // abstract windows toolkit - graphics
import java.awt.event.*;        // event handling
import java.io.*;               // reading from github and writing to history.txt
import java.net.URL;            // for github URL
import java.util.*;
import java.util.List;          // cause theres also awt.List

public class CSWordleGame {
    // colors from https://www.color-hex.com/color-palette/1012607
    public static final Color CORRECT = new Color(108, 169, 101);
    public static final Color WRONG_POSITION = new Color(200, 182, 83);
    public static final Color WRONG = new Color(120, 124, 127);
    public static final int WORD_LENGTH = 5;
    public static final int MAX_ATTEMPTS = 6;

    private String targetWord;
    private int currentRow = 0;         // for tracking current letter
    private int currentCol = 0;
    private boolean isCSMode = false;

    private List<String> regularWords;  // word lists
    private List<String> CSWords = Arrays.asList(
            "array", "class", "debug", "error", "float", "index", "logic", "queue", "stack", "parse",
            "value", "while", "input", "valid", "write", "break", "token", "shift", "coder", "tests",
            "throw", "false", "heaps", "codes", "final", "chars", "loops", "super", "child", "catch");

    private UIManager uiManager;

    public CSWordleGame() {             // constructor
        regularWords = loadWords();
        uiManager = new UIManager(this);
    }

    public void start() {
        uiManager.showFrame();
    }// reads words from raw github file, load to words

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
        } catch (Exception e) {                         // if unable to read, the only word will be 'error'
            words.addAll(Arrays.asList("error"));
        }
        return words;
    }

    public void startNewGame(boolean csMode) {
        isCSMode = csMode;
        List<String> wordList = csMode ? CSWords : regularWords;
        targetWord = wordList.get(new Random().nextInt(wordList.size())).toUpperCase();
        currentRow = 0;
        currentCol = 0;
    }

    public void handleKeyPress(KeyEvent e, JLabel[][] grid) {
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
            submitGuess(grid);
        }
    }

    private void submitGuess(JLabel[][] grid) {
        StringBuilder guess = new StringBuilder();      // guess to stringbuilder
        for (int i = 0; i < WORD_LENGTH; i++) {
            guess.append(grid[currentRow][i].getText());
        }

        String guessStr = guess.toString();             // stringbuilder to string & validate
        if (!regularWords.contains(guessStr.toLowerCase())) {
            return;
        }
        uiManager.updateKeyboard(guessStr);

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

        checkGameStatus(guessStr);
    }

    private void checkGameStatus(String guess) {
        if (guess.equals(targetWord)) {             // win case
            storeGame(targetWord, currentRow);
            uiManager.showEndGameDialog(true, targetWord);
            return;
        }

        currentRow++;
        currentCol = 0;

        if (currentRow == MAX_ATTEMPTS) {           // loss case
            storeGame(targetWord);
            uiManager.showEndGameDialog(false, targetWord);
        }
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

    public void resetGame() {
        currentRow = 0;
        currentCol = 0;
        targetWord = null;
    }

    public boolean isCSMode() {
        return isCSMode;
    }

    public static void main(String[] args) {                // main method runs game
        SwingUtilities.invokeLater(() -> {
            CSWordleGame game = new CSWordleGame();
            game.start();
        });
    }
}