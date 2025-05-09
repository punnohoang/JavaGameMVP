package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MapDataLoader {

    public static List<int[]> readColumn(String fileName) {
        List<int[]> columnPositions = new ArrayList<>();
        BufferedReader reader = null;

        try {
            // Thử đọc từ classpath (nếu dùng resources chuẩn)
            InputStream inputStream = MapDataLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                //System.out.println("Đọc file từ classpath: " + fileName);
            } else {
                // Nếu không có trong classpath, đọc từ thư mục src/model
            	File file = new File("C:\\Users\\hoang\\git\\JavaGameMVP\\JavaGameMVP\\src\\model\\" + fileName);
                if (file.exists()) {
                    reader = new BufferedReader(new FileReader(file));
                    //System.out.println("Đọc file từ hệ thống: " + file.getAbsolutePath());
                } else {
                    System.err.println("Không tìm thấy file: " + file.getAbsolutePath());
                    return columnPositions;
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    int x = Integer.parseInt(parts[0].trim());
                    int height = Integer.parseInt(parts[1].trim());
                    columnPositions.add(new int[]{x, height});
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Lỗi khi đọc file " + fileName + ": " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                System.err.println("Không thể đóng file: " + e.getMessage());
            }
        }

        return columnPositions;
    }
}
