package view;

import model.*;
import presenter.GamePresenter;
import model.DeathZone;

import java.awt.*;
import java.util.List;

public class Renderer {
    private final GamePresenter presenter;
    private final DeathZone deathZoneDrawer = new DeathZone();

    public Renderer(GamePresenter presenter) {
        this.presenter = presenter;
    }

    public void render(Graphics g, int width, int height) {
        GameModel model = presenter.getModel();
        GameMap map = model.getGameMap();

        map.getBackground().draw(g, map.getCameraX(), map.getWidth(), map.getHeight());

        for (Rectangle zone : map.getDeathZoneBounds()) {
            deathZoneDrawer.draw(g, zone, map.getCameraX());
        }

        map.getFoothold().draw(g, map.getColumns(), map.getCameraX());

        g.setColor(new Color(139, 69, 19));
        g.fillRect(0, map.getHeight() - 40, width, 40);

        Ball ball = model.getBall();
        g.setColor(Color.RED);
        g.fillOval(ball.x - map.getCameraX(), ball.y, ball.width, ball.height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Deaths: " + presenter.getDeathCount(), 10, 30);
        g.drawString("Time: " + presenter.getFormattedPlayTime(), 10, 50);

        if (presenter.isDead() || presenter.hasWonFinalMap()) {
            drawEndGameScreen(g);
        }

        if (presenter.isPaused()) {
            drawPauseOverlay(g, width, height);
        }
    }

    private void drawEndGameScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));

        if (presenter.isDead()) {
            g.drawString("GAME OVER", 200, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", 230, 250);
            g.drawString("Press Q to Quit", 240, 280);
        } else {
            g.drawString("YOU WIN!", 220, 200);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Time Played: " + presenter.getFormattedPlayTime(), 230, 310);

        List<String> top3 = presenter.getTop3Players();
        if (top3 != null && !top3.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Top 3 Players:", 230, 340);
            int y = 370;
            for (String info : top3) {
                g.drawString(info, 240, y);
                y += 25;
            }
        }
    }

    private void drawPauseOverlay(Graphics g, int width, int height) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String pauseText = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(pauseText)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(pauseText, x, y);
    }
}
