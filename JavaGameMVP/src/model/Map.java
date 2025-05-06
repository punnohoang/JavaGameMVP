package model;

import java.awt.*;
import java.util.List;
import util.MapDataLoader;

public class Map extends AbstractMap {
    private final Rectangle arrowRect;
    private final Foothold foothold;

    public Map(int mapNumber) {
        // Thiết lập màu nền dựa trên mapNumber
        Color backgroundColor = mapNumber == 1 ? new Color(200, 255, 255) : new Color(255, 200, 200);
        background = new Background(backgroundColor);

        // Đọc cột từ file Map<mapNumber>.txt
        String fileName = "Map" + mapNumber + ".txt";
        //System.out.println("Loading map file: " + fileName);
        List<int[]> columnPositions = MapDataLoader.readColumn(fileName);
        if (columnPositions.isEmpty()) {
            System.err.println("Warning: No columns loaded for " + fileName);
        }
        initColumns(columnPositions, 40, 430);

        // Thiết lập đích đến (arrowRect)
        if (mapNumber == 1) {
            arrowRect = new Rectangle(2975, 370, 40, 500);
        } else {
            arrowRect = new Rectangle(2950, 370, 40, 60);
        }

        // Khởi tạo foothold
        foothold = new Foothold();
    }

    @Override
    public boolean isWin(Ball ball) {
        return arrowRect.intersects(ball.getBounds());
    }

    @Override
    public Foothold getFoothold() {
        return foothold;
    }
}