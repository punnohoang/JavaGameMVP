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
    private boolean wasDead = false;
    private boolean hasWonFinalMap = false;
    private boolean gameOver = false;
    private boolean isPaused = false;
    private int lastPassedColumnsCount = 0;

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        DatabaseManager.insertNewPlayer(playerName);
    }

    public void update() {
        if (gameOver || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        int currentPassedColumns = ball.getPassedColumnsCount();
        if (currentPassedColumns > lastPassedColumnsCount) {
            int newColumns = currentPassedColumns - lastPassedColumnsCount;
            model.addScore(1); // Cộng 1 điểm khi qua cột
            lastPassedColumnsCount = currentPassedColumns;
        }

        if (ball.isDead() && !wasDead && !model.isWin()) {
            wasDead = true;
            model.addScore(-1); // Trừ 1 điểm khi thua
            DatabaseManager.savePlayerResult(playerName, model.getScore());
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
                DatabaseManager.savePlayerResult(playerName, model.getScore());
            }

            if (!model.isLastMap()) {
                model.nextMap();
                lastPassedColumnsCount = 0;
            } else if (!hasWonFinalMap) {
                hasWonFinalMap = true;
                gameOver = true;
                isPaused = true;
                DatabaseManager.savePlayerResult(playerName, model.getScore());
                DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());
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

    public void jump() {
        model.getBall().jump();
    }

    public void restart() {
        model.restart();
        Ball ball = model.getBall();
        ball.clearPassedColumns();
        model.getGameMap().updateCamera(ball.getX(), 640);
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        left = false;
        right = false;
        wasDead = false;
        lastPassedColumnsCount = 0;
        gameOver = false;
        isPaused = false;
        DatabaseManager.savePlayerResult(playerName, model.getScore());
        DatabaseManager.recordPlayTime(playerName, getPlayTimeInSeconds());
        System.out.println("Restarted to map " + (model.getCurrentMapIndex() + 1));
    }

    public boolean revive(boolean useMechanism2) {
        Ball ball = model.getBall();
        int newX = 0;
        int newY = 400;
        int columnsToRestore = 0;

        if (ball.getPassedColumnsCount() > 0) {
            int ballX = ball.getX();
            Rectangle nearestColumn = null;
            int minDistance = Integer.MAX_VALUE;

            for (Rectangle column : model.getGameMap().getColumns()) {
                if (ball.getPassedColumns().contains(column)) {
                    int distance = Math.abs(ballX - column.x);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestColumn = column;
                    }
                }
            }

            if (nearestColumn != null) {
                newX = nearestColumn.x;
                newY = nearestColumn.y - ball.height;
                for (Rectangle column : model.getGameMap().getColumns()) {
                    if (column.x <= newX) {
                        columnsToRestore++;
                    }
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
            DatabaseManager.savePlayerResult(playerName, model.getScore());
            System.out.println("Revived to x=" + newX + ", restored " + columnsToRestore + " columns");
        }
        return revived;
    }

    public void handleKeyPressed(int keyCode) {
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

    public int getReviveCount() {
        return model.getReviveCount();
    }

    public int getMaxRevives() {
        return model.getMaxRevives();
    }
}