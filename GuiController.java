import javax.swing.*;

public class GuiController {
    private final PuzzleEngine logic;
    private final GuiWindow window;
    private int focusRow = -1;
    private int focusCol = -1;

    public GuiController(PuzzleEngine logic, GuiWindow window) {
        this.logic = logic;
        this.window = window;
        bindEvents();
    }

    private void bindEvents() {
        for (int r = 0; r < 9; r++) {
            final int rr = r;
            for (int c = 0; c < 9; c++) {
                final int cc = c;
                window.getCellNode(r, c).addActionListener(e -> {
                    focusRow = rr;
                    focusCol = cc;
                });
            }
        }

        for (int i = 1; i <= 9; i++) {
            final int num = i;
            window.getActionNode("NUM_" + i).addActionListener(e -> injectValue(num));
        }
        window.getActionNode("CLEAR").addActionListener(e -> injectValue(0));

        window.getActionNode("Undo Move").addActionListener(e -> logic.performUndo());
        window.getActionNode("Get Hint").addActionListener(e -> logic.provideHint());
        window.getActionNode("New Puzzle").addActionListener(e -> {
            focusRow = -1; focusCol = -1;
            logic.initializePuzzle();
        });
        window.getActionNode("Restart").addActionListener(e -> logic.restartCurrentGame());

        window.getConfigNode("Live Validation").addActionListener(e ->
                logic.toggleValidation(((JCheckBox)e.getSource()).isSelected()));
        window.getConfigNode("Allow Hints").addActionListener(e ->
                logic.toggleHints(((JCheckBox)e.getSource()).isSelected()));
        window.getConfigNode("Shuffle Puzzles").addActionListener(e ->
                logic.toggleRandomSource(((JCheckBox)e.getSource()).isSelected()));
    }

    private void injectValue(int val) {
        if (focusRow == -1 || focusCol == -1) {
            JOptionPane.showMessageDialog(window, "Select a target square first.");
            return;
        }
        if (val == 0) {
            logic.eraseCell(focusRow, focusCol);
        } else {
            logic.modifyCell(focusRow, focusCol, val);
        }
    }
}