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

    private long startTime;
    private long elapsedTime;

    private boolean left, right;
    private int deathCount = 0;
    private boolean wasDead = false;
    private boolean hasWonFinalMap = false;
    private boolean gameOver = false;
    private boolean isPaused = false;

    private long totalPlayTime = 0; // Tổng thời gian chơi thực tế (milliseconds)
    private int lastPassedColumnsCount = 0;

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.gameTimer = new GameTimer();
        gameTimer.start(); // Bắt đầu đếm thời gian
        this.startTime = System.currentTimeMillis();
    }

    public void update() {
        if (gameOver || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        // Cộng điểm khi qua cột mới
        int currentPassedColumns = ball.getPassedColumnsCount();
        if (currentPassedColumns > lastPassedColumnsCount) {
            int newColumns = currentPassedColumns - lastPassedColumnsCount;
            model.addScore(newColumns);
            System.out.println("score: " + model.getScore());
            lastPassedColumnsCount = currentPassedColumns;
        }

        if (ball.isDead() && !wasDead && !model.isWin()) {
            deathCount++;
            wasDead = true;

            // Trừ điểm khi chết
            int currentScore = model.getScore();
            if (currentScore >= 10) {
                model.addScore(-10);
            } else {
                model.addScore(-currentScore);
            }
            System.out.println("Lost 10 points! New score: " + model.getScore());

            // Dừng timer và lưu thời gian
            gameTimer.stop();
            totalPlayTime += gameTimer.getElapsedTime();

            // Lưu kết quả
            DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
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
                // Lưu điểm hiện tại khi thắng map
                DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
            }

            if (!model.isLastMap()) {
                model.nextMap();
                System.out.println("Switched to next map!");
                lastPassedColumnsCount = 0;
            } else if (!hasWonFinalMap) {
                hasWonFinalMap = true;
                gameOver = true;
                isPaused = true;

                // Dừng timer và lưu thời gian
                gameTimer.stop();
                totalPlayTime += gameTimer.getElapsedTime();

                DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
                DatabaseManager.recordFinalResult(playerName, deathCount);
                DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());

                System.out.println("🎉 You won the final map! Final score: " + model.getScore() + ", Deaths: " + deathCount);

                // Hiển thị top 3 người chơi
                System.out.println("🏆 Top 3 Players:");
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
        if (gameOver) return;

        isPaused = !isPaused;
        if (isPaused) {
            gameTimer.pause();
        } else {
            gameTimer.resume();
        }
    }

    public void restart() {
        Ball ball = model.getBall();

        // Trở về cột ngay trước vùng chết
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
        return (int) (totalPlayTime / 1000);
    }

    public String getFormattedPlayTime() {
        long totalMillis = gameTimer.getElapsedTime();
        long seconds = totalMillis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isPaused() {
        return isPaused;
    }
}
