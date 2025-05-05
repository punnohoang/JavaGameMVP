package presenter;

import model.Ball;
import model.GameModel;
import model.GameTimer;
import util.DatabaseManager;

import java.awt.event.KeyEvent;
import java.util.List;

public class GamePresenter {
    private final GameModel model;
    private final String playerName;
    private final GameTimer gameTimer;

    private boolean left, right;
    private int deathCount = 0;
    private boolean wasDead = false;
    private boolean hasWonFinalMap = false;
    private boolean gameOver = false;
    private boolean isPaused = false;

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.gameTimer = new GameTimer();
    }

    public void update() {
        if (gameOver || model.getBall().isDead() || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        if (ball.isDead() && !wasDead && !model.isWin()) {
            deathCount++;
            wasDead = true;
            System.out.println("Ball died! Deaths: " + deathCount);

            DatabaseManager.recordFinalResult(playerName, deathCount);
            DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());

            gameOver = true;
            isPaused = true;
            gameTimer.stop();
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
                gameTimer.stop();
            }
        }
    }

    public void togglePause() {
        if (gameOver) return;

        isPaused = !isPaused;
        if (isPaused) {
            gameTimer.stop();
        } else {
            gameTimer.start();
        }
    }

    public void restart() {
        Ball ball = model.getBall();
        ball.setPosition(0, 400);
        model.getGameMap().updateCamera(ball.x, 640);
        gameTimer.reset();
        left = right = wasDead = false;
        gameOver = false;
        isPaused = false;
    }

    public void jump() {
        model.getBall().jump();
    }

    public void handleKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_P) {
            togglePause();
            return;
        }

        if (isDead()) {
            if (keyCode == KeyEvent.VK_R) restart();
            else if (keyCode == KeyEvent.VK_Q) System.exit(0);
            return;
        }

        if (gameOver || isPaused) return;

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
        return (int) (gameTimer.getElapsedTime() / 1000);
    }

    public String getFormattedPlayTime() {
        return gameTimer.getFormattedTime();
    }

    public boolean isPaused() {
        return isPaused;
    }
}