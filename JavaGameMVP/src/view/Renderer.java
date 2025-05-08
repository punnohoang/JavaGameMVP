package view;

import model.*;
import presenter.GamePresenter;

import java.awt.*;
import java.util.List;

public class Renderer {
    private final DeathZone deathZoneDrawer = new DeathZone();

    public void render(Graphics g, GamePresenter presenter, int panelWidth) {
        GameModel model = presenter.getModel();
        Map map = model.getGameMap();

        map.getBackground().draw(g, map.getCameraX(), map.getWidth(), map.getHeight());

        for (Rectangle zone : map.getDeathZoneBounds()) {
            deathZoneDrawer.draw(g, zone, map.getCameraX());
        }

        map.getFoothold().draw(g, map.getColumns(), map.getCameraX());

        g.setColor(new Color(139, 69, 19));
        g.fillRect(0, map.getHeight() - 40, panelWidth, 40);

        Ball ball = model.getBall();
        g.setColor(Color.RED);
        g.fillOval(ball.x - map.getCameraX(), ball.y, ball.width, ball.height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Time: " + presenter.getFormattedPlayTime(), 10, 50);
        g.drawString("Map: " + (model.getCurrentMapIndex() + 1), 10, 70);

        if (presenter.isPaused() && !presenter.isDead() && !presenter.hasWonFinalMap()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String pauseMessage = "Paused";
            int stringWidth = g.getFontMetrics().stringWidth(pauseMessage);
            g.drawString(pauseMessage, (panelWidth - stringWidth) / 2, map.getHeight() / 2);
        }

        if (presenter.isDead() || presenter.hasWonFinalMap()) {
            drawEndGameScreen(g, presenter, panelWidth, map.getHeight());
        }
    }

    private void drawEndGameScreen(Graphics g, GamePresenter presenter, int panelWidth, int panelHeight) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));

        FontMetrics fm = g.getFontMetrics();
        int y = panelHeight / 2 - 50;

        if (presenter.isDead()) {
            String gameOverMsg = "GAME OVER";
            int stringWidth = fm.stringWidth(gameOverMsg);
            g.drawString(gameOverMsg, (panelWidth - stringWidth) / 2, y);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            y += 50;
            String restartMsg = "Press R to Restart";
            stringWidth = fm.stringWidth(restartMsg);
            g.drawString(restartMsg, (panelWidth - stringWidth) / 2, y);
            y += 30;
            String quitMsg = "Press Q to Quit";
            stringWidth = fm.stringWidth(quitMsg);
            g.drawString(quitMsg, (panelWidth - stringWidth) / 2, y);
            if (presenter.getReviveCount() < presenter.getMaxRevives() && !presenter.hasWonFinalMap()) {
                y += 30;
                String reviveMsg = "Press V to Revive (" + (presenter.getMaxRevives() - presenter.getReviveCount()) + " left)";
                stringWidth = fm.stringWidth(reviveMsg);
                g.drawString(reviveMsg, (panelWidth - stringWidth) / 2, y);
            }
        } else if (presenter.hasWonFinalMap()) {
            String winMsg = "YOU WIN!";
            int stringWidth = fm.stringWidth(winMsg);
            g.drawString(winMsg, (panelWidth - stringWidth) / 2, y);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            y += 50;
            String restartMsg = "Press R to Restart";
            stringWidth = fm.stringWidth(restartMsg);
            g.drawString(restartMsg, (panelWidth - stringWidth) / 2, y);
            y += 30;
            String quitMsg = "Press Q to Quit";
            stringWidth = fm.stringWidth(quitMsg);
            g.drawString(quitMsg, (panelWidth - stringWidth) / 2, y);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        y += 40;
        String timeMsg = "Time Played: " + presenter.getFormattedPlayTime();
        int stringWidth = fm.stringWidth(timeMsg);
        g.drawString(timeMsg, (panelWidth - stringWidth) / 2, y);

        List<String> top3 = presenter.getTop3Players();
        if (top3 != null && !top3.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            y += 30;
            String top3Msg = "Top 3 Players:";
            stringWidth = fm.stringWidth(top3Msg);
            g.drawString(top3Msg, (panelWidth - stringWidth) / 2, y);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            for (String info : top3) {
                y += 25;
                stringWidth = fm.stringWidth(info);
                g.drawString(info, (panelWidth - stringWidth) / 2, y);
            }
        }
    }
}