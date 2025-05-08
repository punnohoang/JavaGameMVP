package presenter;

import model.*;
import util.DatabaseManager;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

public class GamePresenter {
    private final GameModel model;
    private final String playerName;
    private final GameTimer gameTimer;
    private int scoreId;
    private boolean left, right;
    private int deathCount = 0;
    private boolean wasDead = false;
    private boolean hasWonFinalMap = false;
    private boolean gameOver = false;
    public boolean isPaused = false;
    private int lastPassedColumnsCount = 0;

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.gameTimer = new GameTimer();
        this.scoreId = DatabaseManager.recordPlayTime(playerName, 0, 0);
        gameTimer.start();
    }

    public void update() {
        if (gameOver || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        int currentPassedColumns = ball.getPassedColumnsCount();
        if (currentPassedColumns > lastPassedColumnsCount) {
            int newColumns = currentPassedColumns - lastPassedColumnsCount;
            model.addScore(newColumns);
            lastPassedColumnsCount = currentPassedColumns;
            DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
        }

        if (ball.isDead() && !wasDead && !model.isWin()) {
            wasDead = true;
            gameTimer.pause();
            gameOver = true;
 isPaused = true;
            DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
            return;
        } else if (!ball.isDead()) {
            wasDead = false;
        }

        checkWinAndSwitchMap();
    }

    private void checkWinAndSwitchMap() {
        if (model.getBall().isDead()) return;

        if (model.isWin()) {
            if (!model.isLastMap()) {
                model.nextMap();
                lastPassedColumnsCount = 0;
                DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
            } else if (!hasWonFinalMap) {
                // Thắng map cuối: Điều chỉnh điểm để đạt 100
                int currentScore = model.getScore();
                int pointsToAdd = 100 - currentScore;
                if (pointsToAdd >= 0) {
                    model.addScore(pointsToAdd);
                } else {
                    model.addScore(-currentScore);
                    model.addScore(100);
                }

                hasWonFinalMap = true;
                gameOver = true;
                isPaused = true;
                gameTimer.pause(); // Dừng thời gian khi thắng map cuối

                // Lưu điểm số và thời gian vào cơ sở dữ liệu
                DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));

                // Lấy và hiển thị top 3 người chơi
                List<String> topPlayers = DatabaseManager.getTop3Players();
                if (topPlayers.isEmpty()) {
                    System.out.println("No players in leaderboard yet.");
                } else {
                    for (int i = 0; i < topPlayers.size(); i++) {
                        System.out.println((i + 1) + ". " + topPlayers.get(i));
                    }
                }
            }
        }
    }

    public void togglePause() {
        if (gameOver || isDead()) return;

        isPaused = !isPaused;
        if (isPaused) {
            gameTimer.pause();
        } else {
            gameTimer.resume();
        }
    }

    public void restart() {
        model.restart();
        Ball ball = model.getBall();

        ball.clearPassedColumns();
        model.getGameMap().updateCamera(ball.getX(), 640);
        left = false;
        right = false;
        wasDead = false;
        lastPassedColumnsCount = 0;
        deathCount = 0;
        model.resetScore();
        gameOver = false;
        isPaused = false;
        gameTimer.restart();
        scoreId = DatabaseManager.recordPlayTime(playerName, 0, 0);
        System.out.println("Restarted to map " + (model.getCurrentMapIndex() + 1));
    }

    public boolean revive(boolean useMechanism2) {
        Ball ball = model.getBall();

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
            for (Rectangle column : model.getGameMap().getColumns()) {
                if (column.x <= lastColumnX) {
                    columnsToRestore++;
                }
            }
        }

        boolean revived = model.revive(newX, newY, columnsToRestore);
        if (revived) {
            model.getGameMap().updateCamera(newX, 640);
            left = false;
            right = false;
            wasDead = false;
            lastPassedColumnsCount = columnsToRestore;
            gameOver = false;
            isPaused = false;
            gameTimer.resume();
            DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
            System.out.println("Revived to x=" + newX + ", restored " + columnsToRestore + " columns");
        }
        return revived;
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
            if (keyCode == KeyEvent.VK_R) {
                restart();
            } else if (keyCode == KeyEvent.VK_V) {
                revive(true);
            } else if (keyCode == KeyEvent.VK_Q) {
                System.exit(0);
            }
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
        long totalMillis = gameTimer.getElapsedTime();
        long seconds = totalMillis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getReviveCount() {
        return model.getReviveCount();
    }

    public int getMaxRevives() {
        return model.getMaxRevives();
    }

    public boolean isPaused() {
        return isPaused;
    }
}