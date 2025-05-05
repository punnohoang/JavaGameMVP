package model;

public class GameTimer {
    private long startTime;
    private long elapsedTime; // milliseconds
    private long pauseTime;   // Tổng thời gian đã pause
    private boolean running;

    public GameTimer() {
        reset();
    }

    public void start() {
        if (!running) {
            startTime = System.currentTimeMillis() - elapsedTime;
            running = true;
        }
    }

    public void stop() {
        if (running) {
            elapsedTime = getElapsedTime();
            running = false;
        }
    }

    public void reset() {
        elapsedTime = 0;
        pauseTime = 0;
        startTime = System.currentTimeMillis();
        running = true;
    }

    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime + pauseTime;
        }
        return elapsedTime;
    }

    public String getFormattedTime() {
        long seconds = getElapsedTime() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
