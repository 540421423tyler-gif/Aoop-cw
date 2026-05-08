import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

public class PuzzleEngineTest {

    private PuzzleEngine engine;

    // Define file paths for the main file and the backup file
    private static final Path ORIGINAL_FILE = Paths.get("puzzles.txt");
    private static final Path BACKUP_FILE = Paths.get("puzzles_backup.txt");

    // Prepare a fixed test case to ensure the test environment is independent and stable
    private static final String TEST_PUZZLE =
            "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

    @BeforeAll
    static void setupTestEnvironment() throws IOException {
        // 1. Backup the existing real puzzle file to prevent data loss
        if (Files.exists(ORIGINAL_FILE)) {
            Files.copy(ORIGINAL_FILE, BACKUP_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
        // 2. Overwrite the main file with the single test puzzle for the engine to load
        Files.writeString(ORIGINAL_FILE, TEST_PUZZLE);
    }

    @AfterAll
    static void restoreTestEnvironment() throws IOException {
        // 3. Restore the original puzzle file after all tests are finished
        if (Files.exists(BACKUP_FILE)) {
            Files.copy(BACKUP_FILE, ORIGINAL_FILE, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(BACKUP_FILE); // Clean up the backup file
        }
    }

    @BeforeEach
    void setUp() {
        // Initialize a new engine instance before each test method runs
        engine = new PuzzleEngine();
    }

    @Test
    void testEngineInitialization() {
        // Verify if the file is loaded correctly
        Tile emptyTile = engine.getTile(0, 0);
        assertEquals(0, emptyTile.getValue(), "The initial empty cell value should be 0");
        assertFalse(emptyTile.isFixed(), "Empty cells should not be marked as fixed");

        Tile prefilledTile = engine.getTile(0, 2);
        assertEquals(3, prefilledTile.getValue(), "According to TEST_PUZZLE, (0,2) should be 3");
        assertTrue(prefilledTile.isFixed(), "Initial prefilled cells should be marked as fixed");
    }

    @Test
    void testModifyAndEraseCell() {
        // Test filling in a number
        engine.modifyCell(0, 0, 5);
        assertEquals(5, engine.getTile(0, 0).getValue(), "The value of cell (0,0) should be modified to 5");

        // Test clearing a number
        engine.eraseCell(0, 0);
        assertEquals(0, engine.getTile(0, 0).getValue(), "Cell (0,0) should be 0 after being cleared");
    }

    @Test
    void testCannotModifyFixedCell() {
        // Attempt to modify a fixed puzzle number
        int initialValue = engine.getTile(0, 2).getValue();
        engine.modifyCell(0, 2, 9);

        assertEquals(initialValue, engine.getTile(0, 2).getValue(), "Fixed cells should not be modified");
    }

    @Test
    void testUndoFunctionality() {
        // Perform two consecutive operations
        engine.modifyCell(0, 0, 5);
        engine.modifyCell(0, 1, 7);

        // First undo: (0,1) should be reverted to 0
        engine.performUndo();
        assertEquals(0, engine.getTile(0, 1).getValue(), "After undo, (0,1) should be reverted to empty (0)");
        assertEquals(5, engine.getTile(0, 0).getValue(), "The first undo should not affect (0,0)");

        // Second undo: (0,0) should be reverted to 0
        engine.performUndo();
        assertEquals(0, engine.getTile(0, 0).getValue(), "After the second undo, (0,0) should be reverted to empty (0)");
    }

    @Test
    void testConflictDetection() {
        // Create a row conflict: (0,2) is already 3, and we fill 3 in (0,0)
        engine.modifyCell(0, 0, 3);
        assertTrue(engine.hasConflict(0, 0), "A conflict should be detected when filling in a duplicate number");

        // Fill in a safe number, there should be no conflict (assuming 4 is safe at this position)
        engine.modifyCell(0, 0, 4);
        assertFalse(engine.hasConflict(0, 0), "There should be no conflict when filling in a valid number");
    }

    @Test
    void testHintSystem() {
        engine.toggleHints(true);
        engine.provideHint();

        // The hint system should find the first empty slot (0,0) and fill in the correct answer
        int hintedValue = engine.getTile(0, 0).getValue();
        assertTrue(hintedValue > 0 && hintedValue <= 9, "The hint system should fill in a number between 1 and 9");
        assertEquals(engine.getTile(0, 0).getSolutionValue(), hintedValue, "The hinted number must be the correct answer");
    }

    @Test
    void testRestartGame() {
        engine.modifyCell(0, 0, 5);
        engine.modifyCell(8, 8, 9);

        engine.restartCurrentGame();

        assertEquals(0, engine.getTile(0, 0).getValue(), "After restarting the game, player-filled numbers should be cleared");
        assertEquals(0, engine.getTile(8, 8).getValue(), "After restarting the game, player-filled numbers should be cleared");
        assertEquals(3, engine.getTile(0, 2).getValue(), "After restarting the game, fixed puzzle numbers should be preserved");
    }

    @Test
    void testEventDispatcher() {
        // Test if the listener can correctly receive update signals
        final boolean[] isUpdated = {false};

        engine.addUpdateListener(new BoardUpdateListener() {
            @Override
            public void onBoardUpdated() { isUpdated[0] = true; }
            @Override
            public void onGameWon() {}
        });

        engine.modifyCell(0, 0, 5);
        assertTrue(isUpdated[0], "The onBoardUpdated event must be triggered when the data model changes");
    }
}