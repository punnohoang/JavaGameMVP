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
    private int scoreId; // Lưu scoreId từ database

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
        this.scoreId = DatabaseManager.recordPlayTime(playerName, 0, 0); // Lấy scoreId khi bắt đầu
        gameTimer.start(); // Bắt đầu đếm thời gian
    }

    public void update() {
        if (gameOver || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        int currentPassedColumns = ball.getPassedColumnsCount();
        if (currentPassedColumns > lastPassedColumnsCount) {
            int newColumns = currentPassedColumns - lastPassedColumnsCount;
            model.addScore(1); // Cộng 1 điểm khi qua cột
            System.out.println(model.getScore());
            lastPassedColumnsCount = currentPassedColumns;
            DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
        }

        if (ball.isDead() && !wasDead && !model.isWin()) {
        	wasDead = true;
            gameTimer.pause(); // Tạm dừng thời gian khi chết
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
            if (!hasWonFinalMap) {
                DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
            }

            if (!model.isLastMap()) {
                model.nextMap();
                lastPassedColumnsCount = 0;
            } else if (!hasWonFinalMap) {
                hasWonFinalMap = true;
                gameOver = true;
                isPaused = true;

                DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
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
        if (gameOver || isDead()) return; // Ngăn pause khi chết hoặc game over

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
        model.resetScore(); // Reset điểm về 0
        gameOver = false;
        isPaused = false;
        gameTimer.restart(); // Reset thời gian về 0
        // Tạo bản ghi mới trong database, thay vì reset bản ghi cũ
        //scoreId = DatabaseManager.recordPlayTime(playerName, 0, 0); // Tạo scoreId mới
        //System.out.println("Restarted to map " + (model.getCurrentMapIndex() + 1));
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
            gameTimer.resume(); // Tiếp tục thời gian từ lúc dừng
            DatabaseManager.updateScore(scoreId, model.getScore(), (int) (gameTimer.getElapsedTime() / 1000));
            //System.out.println("Revived to x=" + newX + ", restored " + columnsToRestore + " columns");
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