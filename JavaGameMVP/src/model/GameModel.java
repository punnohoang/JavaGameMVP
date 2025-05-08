package model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GameModel {
    private List<Map> maps;
    private int currentMapIndex;
    private Ball ball;
    private int score;
    private int reviveCount; // Theo dõi số lần hồi sinh
    private static final int MAX_REVIVES = 2; // Tối đa 2 lần hồi sinh
    private static final int REVIVE_PENALTY = 25; // Phạt 25 điểm mỗi lần hồi sinh
    private int savedScore; // Lưu điểm khi restart

    public GameModel() {
        maps = new ArrayList<>();
        maps.add(new Map(1));
        maps.add(new Map(2));
        maps.add(new Map(3));
        currentMapIndex = 0;
        ball = new Ball(getGameMap());
        score = 0;
        reviveCount = 0;
        savedScore = 0;
    }

    public Map getGameMap() {
        return maps.get(currentMapIndex);
    }

    public Ball getBall() {
        return ball;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
        if (score < 0) score = 0;
    }

    public boolean isWin() {
        return getGameMap().isWin(ball);
    }

    public boolean isLastMap() {
        return currentMapIndex == maps.size() - 1;
    }

    public void nextMap() {
        if (!isLastMap()) {
            currentMapIndex++;
            ball.setMap(getGameMap());
            ball.setPosition(0, 400);
        }
    }

    public int getCurrentMapIndex() {
        return currentMapIndex;
    }

    // Hàm restart: Cơ chế 1 - Quay về map đầu tiên và lưu điểm
    public void restart() {
        savedScore = score; // Lưu điểm hiện tại
        currentMapIndex = 0; // Quay về map đầu tiên
        ball.setMap(getGameMap());
        ball.setPosition(0, 400); // Đặt lại vị trí bóng
        reviveCount = 0; // Đặt lại số lần hồi sinh
    }

    // Hàm hồi sinh: Đặt lại vị trí bóng nhưng không thay đổi map
    public boolean revive(int newX, int newY, int columnsToRestore) {
        if (reviveCount >= MAX_REVIVES) {
            System.out.println("Cannot revive: Max revives (" + MAX_REVIVES + ") reached.");
            return false; // Không thể hồi sinh nữa
        }
        reviveCount++; // Tăng số lần hồi sinh
        // Trừ 25 điểm, đảm bảo điểm không âm
        if (score < REVIVE_PENALTY) {
            score = 0;
        } else {
            score -= REVIVE_PENALTY;
        }
        System.out.println("Revive penalty applied: -" + REVIVE_PENALTY + ", new score: " + score);

        // Đặt lại vị trí bóng
        ball.setPosition(newX, newY);
        ball.setMap(getGameMap()); // Đảm bảo bóng ở đúng map
        ball.clearPassedColumns();
        int restored = 0;
        for (Rectangle column : getGameMap().getColumns()) {
            if (column.x <= newX) {
                ball.addPassedColumn(column);
                restored++;
                if (restored >= columnsToRestore) break;
            }
        }

        return true; // Hồi sinh thành công
    }

    public int getReviveCount() {
        return reviveCount;
    }

    public int getMaxRevives() {
        return MAX_REVIVES;
    }

    public int getSavedScore() {
        return savedScore;
    }

    public void resetScore() {
        score = savedScore;
        reviveCount = 0;
    }
}