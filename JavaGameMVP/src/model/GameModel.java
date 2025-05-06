package model;

import java.util.ArrayList;
import java.util.List;

public class GameModel {
    private final List<GameMap> maps = new ArrayList<>();
    private int currentMapIndex = 0;
    private Ball ball;
    private int score = 0; // Thêm để quản lý điểm

    public GameModel() {
        maps.add(new Map1());
        maps.add(new Map2());
        maps.add(new Map3());
        ball = new Ball(maps.get(currentMapIndex));
    }

    public GameMap getGameMap() {
        return maps.get(currentMapIndex);
    }

    public Ball getBall() {
        return ball;
    }

    public boolean isWin() {
        return getGameMap().isWin(ball);
    }

    public boolean nextMap() {
        if (currentMapIndex + 1 < maps.size()) {
            currentMapIndex++;
            // Giữ ball, chỉ reset vị trí và trạng thái
            ball.setMap(getGameMap());
            ball.setPosition(0, 400);
            ball.clearPassedColumns(); // Reset cột đã qua khi chuyển map
            return true;
        }
        return false;
    }

    public boolean isLastMap() {
        return currentMapIndex == maps.size() - 1;
    }

    public void setCurrentMapIndex(int index) {
        if (index >= 0 && index < maps.size()) {
            currentMapIndex = index;
            ball.setMap(getGameMap());
            ball.setPosition(0, 400);
            ball.clearPassedColumns();
        }
    }

    public int getCurrentMapIndex() {
        return currentMapIndex;
    }

    // Thêm để quản lý điểm
    public void addScore(int points) {
        score += points;
        if (score < 0) score = 0; // Đảm bảo điểm không âm
    }

    public int getScore() {
        return score;
    }
}