import java.util.EventListener;

public interface BoardUpdateListener extends EventListener {
    void onBoardUpdated();
    void onGameWon();
}
