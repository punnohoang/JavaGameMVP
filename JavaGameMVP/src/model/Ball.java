package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Ball {
    public int x = 0, y = 400;
    public final int width = 30, height = 30;
    public int velocityX = 0, velocityY = 0;
    public boolean onGround = true;
    public final int gravity = 1;
    public final int groundY = 430;
    private boolean isDead = false;
    private GameMap gameMap;
    private final int screenWidth = 640;
    private List<Rectangle> passedColumns = new ArrayList<>();
    private int lastSafeColumnX = 0;
    private int passedColumnsCount = 0; // Thêm biến để theo dõi số cột vượt qua

    public Ball(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void update(boolean left, boolean right) {
        if (isDead) {
            return;
        }

        if (left) velocityX = -5;
        else if (right) velocityX = 5;
        else velocityX = 0;

        boolean collided = false;
        for (Rectangle col : gameMap.getColumns()) {
            int top = col.y;
            if (x + width > col.x && x < col.x + col.width && y + height <= top && y + height + velocityY >= top) {
                y = top - height;
                velocityY = 0;
                onGround = true;
                collided = true;
                lastSafeColumnX = col.x;
                if (!passedColumns.contains(col)) {
                    passedColumns.add(col);
                    passedColumnsCount++;
                }
                break;
            }
        }

        // Chỉ thêm cột khi vượt qua hoàn toàn (x + width > col.x + col.width)
        for (Rectangle col : gameMap.getColumns()) {
            if (!passedColumns.contains(col) && x + width > col.x + col.width) {
                passedColumns.add(col);
                passedColumnsCount++;
            }
        }

        if (!collided) {
            if (y + velocityY >= groundY) {
                y = groundY;
                velocityY = 0;
                onGround = true;
            } else {
                velocityY += gravity;
                if (velocityY > 20) velocityY = 20;
                onGround = false;
            }
        }

        int nextX = x + velocityX;
        Rectangle nextRect = new Rectangle(nextX, y, width, height);
        boolean blocked = false;
        for (Rectangle col : gameMap.getColumns()) {
            if (nextRect.intersects(col)) {
                blocked = true;
                break;
            }
        }

        for (Rectangle dz : gameMap.getDeathZoneBounds()) {
            if (nextRect.intersects(dz) || new Rectangle(x, y + velocityY, width, height).intersects(dz)) {
                isDead = true;
                break;
            }
        }

        if (!blocked) x = nextX;
        y += velocityY;

        gameMap.updateCamera(x, screenWidth);

        if (x < 0) x = 0;
        if (x > gameMap.getWidth() - width) x = gameMap.getWidth() - width;
    }

    public void jump() {
        if (onGround && !isDead) {
            velocityY = -15;
            onGround = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void setMap(GameMap newMap) {
        this.gameMap = newMap;
        clearPassedColumns(); // Reset passedColumns khi đổi map
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isDead = false;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        this.isDead = dead;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPassedColumnsCount() {
        return passedColumnsCount; // Trả về số cột đã vượt qua
    }

    public List<Rectangle> getPassedColumns() {
        return passedColumns;
    }

    public int getLastSafeColumnX() {
        return lastSafeColumnX;
    }

    public void clearPassedColumns() {
        passedColumns.clear();
        passedColumnsCount = 0; // Reset cả passedColumnsCount
    }

    public void addPassedColumn(Rectangle column) {
        if (!passedColumns.contains(column)) {
            passedColumns.add(column);
            passedColumnsCount++;
        }
    }
}