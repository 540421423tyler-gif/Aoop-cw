import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GuiWindow extends JFrame implements BoardUpdateListener {
    private final PuzzleEngine engine;
    private final JButton[][] gridButtons = new JButton[9][9];
    private final Map<String, JButton> actionButtons = new HashMap<>();
    private final Map<String, JCheckBox> configBoxes = new HashMap<>();

    public GuiWindow(PuzzleEngine engine) {
        this.engine = engine;
        this.engine.addUpdateListener(this);

        setupFrame();
        buildMainLayout();
        refreshDisplay();
    }

    private void setupFrame() {
        setTitle("Modern Sudoku Solver");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(950, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void buildMainLayout() {
        JPanel gridWrapper = new JPanel(new GridLayout(9, 9));
        gridWrapper.setPreferredSize(new Dimension(650, 650));

        Font bigFont = new Font("Verdana", Font.BOLD, 26);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JButton btn = new JButton();
                btn.setFont(bigFont);
                btn.setFocusPainted(false);

                int top = (r % 3 == 0) ? 3 : 1;
                int left = (c % 3 == 0) ? 3 : 1;
                int bottom = (r == 8) ? 3 : 0;
                int right = (c == 8) ? 3 : 0;
                Border edge = BorderFactory.createMatteBorder(top, left, bottom, right, Color.DARK_GRAY);
                btn.setBorder(edge);

                gridButtons[r][c] = btn;
                gridWrapper.add(btn);
            }
        }
        add(gridWrapper, BorderLayout.CENTER);

        JPanel sideMenu = new JPanel(new GridLayout(8, 1, 0, 15));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] actions = {"New Puzzle", "Restart", "Undo Move", "Get Hint"};
        for (String act : actions) {
            JButton b = new JButton(act);
            b.setFont(new Font("SansSerif", Font.PLAIN, 18));
            actionButtons.put(act, b);
            sideMenu.add(b);
        }

        String[] configs = {"Live Validation", "Allow Hints", "Shuffle Puzzles"};
        boolean[] defaults = {true, true, false};
        for (int i = 0; i < configs.length; i++) {
            JCheckBox cb = new JCheckBox(configs[i], defaults[i]);
            cb.setFont(new Font("SansSerif", Font.ITALIC, 16));
            configBoxes.put(configs[i], cb);
            sideMenu.add(cb);
        }
        add(sideMenu, BorderLayout.EAST);

        JPanel numpad = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        for (int i = 1; i <= 9; i++) {
            JButton nb = new JButton(String.valueOf(i));
            nb.setPreferredSize(new Dimension(65, 65));
            nb.setFont(bigFont);
            actionButtons.put("NUM_" + i, nb);
            numpad.add(nb);
        }
        JButton clearBtn = new JButton("DEL");
        clearBtn.setPreferredSize(new Dimension(85, 65));
        clearBtn.setFont(new Font("Verdana", Font.BOLD, 18));
        actionButtons.put("CLEAR", clearBtn);
        numpad.add(clearBtn);

        add(numpad, BorderLayout.SOUTH);
    }

    @Override
    public void onBoardUpdated() {
        refreshDisplay();
    }

    @Override
    public void onGameWon() {
        JOptionPane.showMessageDialog(this, "Success! Puzzle Completed.", "Victory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshDisplay() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile t = engine.getTile(r, c);
                JButton btn = gridButtons[r][c];

                btn.setText(t.getValue() == 0 ? "" : String.valueOf(t.getValue()));

                if (t.isFixed()) {
                    btn.setForeground(Color.decode("#333333"));
                    btn.setBackground(Color.decode("#E0E0E0"));
                } else {
                    btn.setForeground(Color.decode("#0055AA"));
                    btn.setBackground(Color.WHITE);
                }

                if (engine.isValidationEnabled() && engine.hasConflict(r, c)) {
                    btn.setBackground(Color.decode("#FFCCCC"));
                }
            }
        }
        actionButtons.get("Get Hint").setEnabled(engine.isHintsEnabled());
    }

    public JButton getCellNode(int r, int c) { return gridButtons[r][c]; }
    public JButton getActionNode(String key) { return actionButtons.get(key); }
    public JCheckBox getConfigNode(String key) { return configBoxes.get(key); }
}
