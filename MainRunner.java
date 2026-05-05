import javax.swing.SwingUtilities;

public class MainRunner {
    public static void main(String[] args) {
        // GUI version
        PuzzleEngine sharedEngine = new PuzzleEngine();

        SwingUtilities.invokeLater(() -> {
            GuiWindow mainWindow = new GuiWindow(sharedEngine);
            new GuiController(sharedEngine, mainWindow);
            mainWindow.setVisible(true);
        });
    }
}