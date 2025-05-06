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
    private int lastPassedColumnsCount = 0;

    public GamePresenter(GameModel model, String playerName) {
        this.model = model;
        this.playerName = playerName;
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        DatabaseManager.insertNewPlayer(playerName);
    }

    public void update() {
        if (gameOver || model.getBall().isDead() || isPaused) return;

        Ball ball = model.getBall();
        ball.update(left, right);

        int currentPassedColumns = ball.getPassedColumnsCount();
        if (currentPassedColumns > lastPassedColumnsCount) {
            int newColumns = currentPassedColumns - lastPassedColumnsCount;
            model.addScore(newColumns);
            lastPassedColumnsCount = currentPassedColumns;
        }

        if (ball.isDead() && !wasDead && !model.isWin()) {
            deathCount++;
            wasDead = true;
            int currentScore = model.getScore();
            if (currentScore >= 10) {
                model.addScore(-10);
            } else {
                model.addScore(-currentScore);
            }
            DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
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
                DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
            }

            if (!model.isLastMap()) {
                model.nextMap();
                lastPassedColumnsCount = 0;
            } else if (!hasWonFinalMap) {
                hasWonFinalMap = true;
                gameOver = true;
                isPaused = true;
                DatabaseManager.savePlayerResult(playerName, model.getScore(), deathCount);
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
        /*
        // Cơ chế 2: Trở về cột an toàn cuối (lastSafeColumnX)
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
		*/
        
        // Cơ chế 3: Trở về cột gần nhất trong passedColumns
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
                ball.clearPassedColumns();
                for (Rectangle column : model.getGameMap().getColumns()) {
                    if (column.x <= newX) {
                        ball.addPassedColumn(column);
                        columnsToRestore++;
                    }
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
        System.out.println("Restarted to x=" + newX + ", restored " + columnsToRestore + " columns");
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