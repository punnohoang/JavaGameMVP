package model;

import java.awt.*;
import java.util.List;
import util.MapDataLoader;

public class Map extends AbstractMap {
    private final Rectangle arrowRect;
    private final Foothold foothold;

    public Map(int mapNumber) {
        // Thiết lập màu nền dựa trên mapNumber
    	Color backgroundColor;
    	switch (mapNumber) {
    	    case 1:
    	        backgroundColor = new Color(200, 255, 255); // xanh nhạt
    	        break;
    	    case 2:
    	        backgroundColor = new Color(255, 200, 200); // hồng nhạt
    	        break;
    	    case 3:
    	        backgroundColor = new Color(200, 255, 200); // xanh lá nhạt
    	        break;
    	    default:
    	        backgroundColor = Color.LIGHT_GRAY; // fallback
    	}

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

            arrowRect = new Rectangle(4300, 370, 40, 500);


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