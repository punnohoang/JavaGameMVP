package model;

public class GameTimer {
    private long startTime;      // Thời điểm bắt đầu gần nhất
    private long elapsedTime;    // Thời gian tích lũy (milliseconds)
    private boolean running;     // Trạng thái chạy
    private boolean paused;      // Trạng thái pause

    public GameTimer() {
        reset();
    }

    public void start() {
        if (!running && !paused) {
            startTime = System.currentTimeMillis();
            running = true;
        }
    }

    public void pause() {
        if (running && !paused) {
            elapsedTime += System.currentTimeMillis() - startTime;
            running = false;
            paused = true;
            startTime = 0; // Ngăn giá trị sai
        }
    }

    public void resume() {
        if (paused && !running) {
            startTime = System.currentTimeMillis();
            paused = false;
            running = true;
        }
    }

    public void stop() {
        if (running && !paused) {
            elapsedTime += System.currentTimeMillis() - startTime;
        }
        running = false;
        paused = false;
        startTime = 0; // Ngăn giá trị sai
    }

    public void reset() {
        elapsedTime = 0;
        startTime = 0;
        running = false;
        paused = false;
    }

    public void restart() {
        reset(); // Reset hoàn toàn thời gian và trạng thái
        startTime = System.currentTimeMillis();
        running = true;
        paused = false;
    }

    public long getElapsedTime() {
        if (!running || paused || startTime == 0) {
            return elapsedTime;
        }
        long currentElapsed = System.currentTimeMillis() - startTime;
        return currentElapsed >= 0 && currentElapsed < 1_000_000_000 ? elapsedTime + currentElapsed : elapsedTime;
    }

    public String getFormattedTime() {
        long seconds = getElapsedTime() / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}