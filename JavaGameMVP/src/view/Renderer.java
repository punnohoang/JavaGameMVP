package view;

import model.*;
import presenter.GamePresenter;

import java.awt.*;
import java.util.List;

public class Renderer {
	private final DeathZone deathZoneDrawer = new DeathZone();
	private final int screenWidth = 640; // Giả định chiều rộng màn hình
	private final int screenHeight = 500; // Giả định chiều cao màn hình

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
		g.drawString("Time: " + presenter.getFormattedPlayTime(), 10, 20);
		g.drawString("Map: " + (model.getCurrentMapIndex() + 1), 10, 40);
		// g.drawString("Press SPACE to Pause !" , 10, 90);

		if (presenter.isPaused() && !presenter.isDead() && !presenter.hasWonFinalMap()) {
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial", Font.BOLD, 36));
			FontMetrics fm = g.getFontMetrics();
			String pauseMessage = "Paused";
			int stringWidth = fm.stringWidth(pauseMessage);
			g.drawString(pauseMessage, (panelWidth - stringWidth) / 2, map.getHeight() / 2);
		}

		if (presenter.isDead() && !presenter.hasWonFinalMap()) {
			drawEndGameScreen(g, presenter, panelWidth, map.getHeight());
		}

		if (presenter.hasWonFinalMap()) {
			// Vẽ nền mờ để làm nổi bật
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, screenWidth, screenHeight);

			// Vẽ "You Win"
			g.setColor(Color.YELLOW);
			g.setFont(new Font("Arial", Font.BOLD, 40));
			FontMetrics fm = g.getFontMetrics();
			String youWin = "You Win!";
			int textWidth = fm.stringWidth(youWin);
			g.drawString(youWin, (screenWidth - textWidth) / 2, screenHeight / 3);

			// Vẽ top 3 người chơi
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			fm = g.getFontMetrics();
			List<String> topPlayers = presenter.getTop3Players();
			if (topPlayers == null || topPlayers.isEmpty()) {
				String noPlayers = "No players in leaderboard yet.";
				textWidth = fm.stringWidth(noPlayers);
				g.drawString(noPlayers, (screenWidth - textWidth) / 2, screenHeight / 2);
			} else {
				for (int i = 0; i < topPlayers.size(); i++) {
					String player = (i + 1) + ". " + topPlayers.get(i);
					textWidth = fm.stringWidth(player);
					g.drawString(player, (screenWidth - textWidth) / 2, screenHeight / 2 + i * 30);
				}
			}
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

			y += 50;
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			fm = g.getFontMetrics();

			String restartMsg = "Press R to Restart";
			stringWidth = fm.stringWidth(restartMsg);
			g.drawString(restartMsg, (panelWidth - stringWidth) / 2, y);

			y += 30;
			String quitMsg = "Press Q to Quit";
			stringWidth = fm.stringWidth(quitMsg);
			g.drawString(quitMsg, (panelWidth - stringWidth) / 2, y);

			// if (presenter.getReviveCount() < presenter.getMaxRevives() &&
			//     !presenter.hasWonFinalMap()) {
			//     y += 30;
			//     String reviveMsg = "Press V to Revive (" + (presenter.getMaxRevives() -
			//         presenter.getReviveCount()) + " left)";
			//     stringWidth = fm.stringWidth(reviveMsg);
			//     g.drawString(reviveMsg, (panelWidth - stringWidth) / 2, y);
			// }
		}

		y += 40;
		String timeMsg = "Time Played: " + presenter.getFormattedPlayTime();
		int stringWidth = fm.stringWidth(timeMsg);
		g.drawString(timeMsg, (panelWidth - stringWidth) / 2, y);

		List<String> top3 = presenter.getTop3Players();
		if (top3 != null && !top3.isEmpty()) {
			y += 30;
			g.setFont(new Font("Arial", Font.BOLD, 18));
			fm = g.getFontMetrics();

			String top3Msg = "Top 3 Players:";
			stringWidth = fm.stringWidth(top3Msg);
			g.drawString(top3Msg, (panelWidth - stringWidth) / 2, y);

			g.setFont(new Font("Arial", Font.BOLD, 16));
			fm = g.getFontMetrics();

			for (String info : top3) {
				y += 25;
				stringWidth = fm.stringWidth(info);
				g.drawString(info, (panelWidth - stringWidth) / 2, y);
			}
		}
	}
}
