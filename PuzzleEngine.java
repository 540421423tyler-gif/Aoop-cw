import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PuzzleEngine {
    public static final int DIMENSION = 9;
    private final Tile[][] matrix = new Tile[DIMENSION][DIMENSION];
    private final List<BoardUpdateListener> listeners = new ArrayList<>();
    private final Deque<Move> historyStack = new ArrayDeque<>();

    private boolean enableValidation = true;
    private boolean enableHints = true;
    private boolean randomizeSource = false;

    public PuzzleEngine() {
        initializePuzzle();
    }

    public void addUpdateListener(BoardUpdateListener listener) {
        this.listeners.add(listener);
    }

    private void fireUpdate() {
        for (BoardUpdateListener listener : listeners) listener.onBoardUpdated();
        if (checkVictoryCondition()) {
            for (BoardUpdateListener listener : listeners) listener.onGameWon();
        }
    }

    public void initializePuzzle() {
        try {
            List<String> rawLines = Files.readAllLines(Paths.get("puzzles.txt"));
            if (rawLines.isEmpty()) return;

            String targetLine = randomizeSource ?
                    rawLines.get(new Random().nextInt(rawLines.size())) : rawLines.get(0);

            int[][] tempSolverBoard = new int[DIMENSION][DIMENSION];
            for (int i = 0; i < 81; i++) {
                tempSolverBoard[i / 9][i % 9] = targetLine.charAt(i) - '0';
            }

            // 计算解答
            executeBacktracking(tempSolverBoard);

            for (int r = 0; r < DIMENSION; r++) {
                for (int c = 0; c < DIMENSION; c++) {
                    int initialVal = targetLine.charAt(r * 9 + c) - '0';
                    matrix[r][c] = new Tile(initialVal, tempSolverBoard[r][c], initialVal != 0);
                }
            }
            historyStack.clear();
            fireUpdate();
        } catch (IOException ex) {
            System.err.println("Fatal: Cannot locate puzzles.txt in the project directory.");
        }
    }

    public void modifyCell(int r, int c, int newValue) {
        if (isOutOfBounds(r, c) || matrix[r][c].isFixed()) return;
        historyStack.push(new Move(r, c, matrix[r][c].getValue()));
        matrix[r][c].setValue(newValue);
        fireUpdate();
    }

    public void eraseCell(int r, int c) {
        modifyCell(r, c, 0);
    }

    public void performUndo() {
        if (!historyStack.isEmpty()) {
            Move lastMove = historyStack.pop();
            matrix[lastMove.row()][lastMove.col()].setValue(lastMove.previousValue());
            fireUpdate();
        }
    }

    public void provideHint() {
        if (!enableHints) return;
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                if (matrix[r][c].getValue() == 0) {
                    modifyCell(r, c, matrix[r][c].getSolutionValue());
                    return;
                }
            }
        }
    }

    public void restartCurrentGame() {
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                if (!matrix[r][c].isFixed()) matrix[r][c].setValue(0);
            }
        }
        historyStack.clear();
        fireUpdate();
    }

    private boolean executeBacktracking(int[][] grid) {
        for (int row = 0; row < DIMENSION; row++) {
            for (int col = 0; col < DIMENSION; col++) {
                if (grid[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (checkSafe(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (executeBacktracking(grid)) return true;
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkSafe(int[][] b, int row, int col, int num) {
        for (int d = 0; d < DIMENSION; d++) {
            if (b[row][d] == num || b[d][col] == num) return false;
        }
        int startRow = row - row % 3, startCol = col - col % 3;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (b[startRow + r][startCol + c] == num) return false;
            }
        }
        return true;
    }

    public boolean hasConflict(int r, int c) {
        int currentVal = matrix[r][c].getValue();
        if (currentVal == 0) return false;

        int[][] temp = new int[DIMENSION][DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                temp[i][j] = matrix[i][j].getValue();
            }
        }
        temp[r][c] = 0;
        return !checkSafe(temp, r, c, currentVal);
    }

    private boolean checkVictoryCondition() {
        for (int r = 0; r < DIMENSION; r++) {
            for (int c = 0; c < DIMENSION; c++) {
                if (matrix[r][c].getValue() == 0 || hasConflict(r, c)) return false;
            }
        }
        return true;
    }

    private boolean isOutOfBounds(int r, int c) {
        return r < 0 || r >= DIMENSION || c < 0 || c >= DIMENSION;
    }

    public Tile getTile(int r, int c) { return matrix[r][c]; }
    public boolean isValidationEnabled() { return enableValidation; }
    public void toggleValidation(boolean val) { this.enableValidation = val; fireUpdate(); }
    public boolean isHintsEnabled() { return enableHints; }
    public void toggleHints(boolean val) { this.enableHints = val; fireUpdate(); }
    public void toggleRandomSource(boolean val) { this.randomizeSource = val; }
}
