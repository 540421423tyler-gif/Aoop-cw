import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleTerminal implements BoardUpdateListener {
    private final PuzzleEngine engine;
    private boolean activeStatus = true;
    private boolean randomize = false;

    public ConsoleTerminal(PuzzleEngine engine) {
        this.engine = engine;
        this.engine.addUpdateListener(this);
    }

    public void launch() {
        System.out.println(">>> SUDOKU TERMINAL ENVIRONMENT <<<");
        renderGrid();
        displayWarnings();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (activeStatus) {
                System.out.print("\n[cmd: set r c v | clear r c | undo | hint | new | val | random | exit]\n➜ ");
                String input = reader.readLine();
                if (input == null) break;
                String[] tokens = input.trim().toLowerCase().split("\\s+");
                if (tokens.length == 0 || tokens[0].isEmpty()) continue;
                processCommand(tokens);
            }
        } catch (Exception e) {
            System.err.println("Fatal terminal error.");
        }
    }

    private void processCommand(String[] args) {
        try {
            switch (args[0]) {
                case "set" -> engine.modifyCell(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                case "clear" -> engine.eraseCell(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                case "undo" -> engine.performUndo();
                case "hint" -> engine.provideHint();
                case "new" -> {
                    engine.initializePuzzle();
                    System.out.println("[*] Fresh puzzle generated.");
                }
                case "val" -> {
                    engine.toggleValidation(!engine.isValidationEnabled());
                    System.out.println("[*] Validation is " + (engine.isValidationEnabled() ? "ACTIVE" : "INACTIVE"));
                }
                case "random" -> {
                    randomize = !randomize;
                    engine.toggleRandomSource(randomize);
                    System.out.println("[*] Randomization: " + randomize);
                }
                case "exit" -> {
                    activeStatus = false;
                    System.out.println("Terminating...");
                }
                default -> System.out.println("[!] Unrecognized input.");
            }
        } catch (Exception ex) {
            System.out.println("[!] Syntax error or invalid coordinates.");
        }
    }

    @Override
    public void onBoardUpdated() {
        System.out.println("\n--- Grid Synchronized ---");
        renderGrid();
        displayWarnings();
    }

    @Override
    public void onGameWon() {
        System.out.println("★★★ PUZZLE SOLVED! EXCELLENT WORK! ★★★");
    }

    private void displayWarnings() {
        if (!engine.isValidationEnabled()) return;
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (engine.hasConflict(r, c)) {
                    sb.append(String.format("(%d,%d) ", r, c));
                }
            }
        }
        if (!sb.isEmpty()) {
            System.out.println("[!] RULE VIOLATION AT: " + sb);
        }
    }

    private void renderGrid() {
        for (int r = 0; r < 9; r++) {
            if (r % 3 == 0) System.out.println(" -----------------------");
            for (int c = 0; c < 9; c++) {
                if (c % 3 == 0) System.out.print("| ");
                int v = engine.getTile(r, c).getValue();
                System.out.print((v == 0 ? "*" : v) + " ");
            }
            System.out.println("|");
        }
        System.out.println(" -----------------------");
    }

    // ==========================================
    // SudokuCLI
    // ==========================================
    public static void main(String[] args) {
        PuzzleEngine sharedEngine = new PuzzleEngine();
        ConsoleTerminal terminal = new ConsoleTerminal(sharedEngine);
        terminal.launch();
    }
}