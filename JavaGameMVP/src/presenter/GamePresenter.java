package presenter;

import model.Ball;
import model.GameModel;
import util.DatabaseManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

public class GamePresenter {
    private final GameModel model;
    private final String playerName;
    private long startTime;
    private long elapsedTime;
    private boolean left, right;
    private int deathCount = 0;
    private boolean wasDead = false;
    private boolean hasWonFinalMap = false;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private int lastPassedColumnsCount = 0; // Thêm để hỗ trợ cơ chế 2

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
    }

    public void update() {
        if (gameOver || model.getBall().isDead() || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        if (ball.isDead() && !wasDead && !model.isWin()) {
            deathCount++;
            wasDead = true;
            System.out.println("Ball died! Deaths: " + deathCount);

            // Ghi nhận kết quả khi chết
            DatabaseManager.recordFinalResult(playerName, deathCount);
            DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());

            gameOver = true;
            isPaused = true;
            return;
        } else if (!ball.isDead()) {
            wasDead = false;
        }

        checkWinAndSwitchMap();
    }

    private void checkWinAndSwitchMap() {
        if (model.getBall().isDead()) return;

        if (model.isWin()) {
            int mapIndex = model.getCurrentMapIndex();

            if (!hasWonFinalMap) {
                if (mapIndex == 0 || mapIndex == 1) {
                    DatabaseManager.updateScore(playerName, 30);
                } else if (mapIndex == 2) {
                    DatabaseManager.updateScore(playerName, 40);
                }
            }

            if (!model.isLastMap()) {
                model.nextMap();
                System.out.println("Switched to next map!");
            } else if (!hasWonFinalMap) {
                hasWonFinalMap = true;
                gameOver = true;
                System.out.println("🎉 You won the final map!");
                DatabaseManager.recordFinalResult(playerName, deathCount);
                DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());
                isPaused = true;
            }
        }
    }

    public void jump() {
        model.getBall().jump();
    }

    public void restart() {
        Ball ball = model.getBall();

        /*
        // Cơ chế 1: Trở về vị trí ban đầu của map
        ball.setPosition(0, 400);
        model.getGameMap().updateCamera(ball.x, 640);
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        left = false;
        right = false;
        wasDead = false;
        gameOver = false;
        isPaused = false;
        */

        // Cơ chế 2: Trở về cột ngay trước vùng chết
        int newX = 0;
        int newY = 400;
        int columnsToRestore = 0;

        if (ball.getPassedColumnsCount() > 0) {
            int lastColumnX = ball.getLastSafeColumnX();
            for (Rectangle column : model.getGameMap().getColumns()) {
                if (column.x == lastColumnX) {
                    newX = column.x;
                    newY = column.y - ball.height;
                    break;
                }
            }
            ball.clearPassedColumns();
            for (Rectangle column : model.getGameMap().getColumns()) {
                if (column.x <= lastColumnX) {
                    ball.addPassedColumn(column);
                    columnsToRestore++;
                }
            }
        }

        ball.setPosition(newX, newY);
        model.getGameMap().updateCamera(newX, 640);
        left = false;
        right = false;
        wasDead = false;
        lastPassedColumnsCount = columnsToRestore;
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        gameOver = false;
        isPaused = false;
    }

    public void handleKeyPressed(int keyCode) {
        if (isDead()) {
            if (keyCode == KeyEvent.VK_R) restart();
            else if (keyCode == KeyEvent.VK_Q) System.exit(0);
            return;
        }

        if (gameOver) return;

        switch (keyCode) {
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
            case KeyEvent.VK_UP -> jump();
        }
    }

    public void handleKeyReleased(int keyCode) {
        if (gameOver) return;

        switch (keyCode) {
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
        }
    }

    public GameModel getModel() {
        return model;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public boolean isDead() {
        return model.getBall().isDead();
    }

    public boolean hasWonFinalMap() {
        return hasWonFinalMap;
    }

    public List<String> getTop3Players() {
        return DatabaseManager.getTop3Players();
    }

    public int getPlayTimeInSeconds() {
        if (isPaused) return (int) (elapsedTime / 1000);
        return (int) ((System.currentTimeMillis() - startTime + elapsedTime) / 1000);
    }

    public String getFormattedPlayTime() {
        int seconds = getPlayTimeInSeconds();
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}