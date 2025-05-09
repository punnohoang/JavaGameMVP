package view;

import model.*;
import presenter.GamePresenter;

import java.awt.*;
import java.util.List;

public class Renderer {
	private final DeathZone deathZoneDrawer = new DeathZone();
	private final Color groundColor = new Color(139, 69, 19);
	private final Color ballColor = Color.RED;
	private final Color textColor = Color.BLACK;
	private final Font infoFont = new Font("Arial", Font.PLAIN, 16);
	private final Font titleFont = new Font("Arial", Font.BOLD, 36);
	private final Font subtitleFont = new Font("Arial", Font.BOLD, 20);
	private final Font leaderboardFont = new Font("Arial", Font.BOLD, 18);

	public void render(Graphics g, GamePresenter presenter, int panelWidth) {
		GameModel model = presenter.getModel();
		GameMap map = model.getGameMap();
		Ball ball = model.getBall();

		// Vẽ background
		map.getBackground().draw(g, map.getCameraX(), map.getWidth(), map.getHeight());

		// Vẽ death zones
		for (Rectangle zone : map.getDeathZoneBounds()) {
			deathZoneDrawer.draw(g, zone, map.getCameraX());
		}

		// Vẽ foothold
		map.getFoothold().draw(g, map.getColumns(), map.getCameraX());

		// Vẽ ground
		g.setColor(groundColor);
		g.fillRect(0, map.getHeight() - 40, panelWidth, 40);

		// Vẽ bóng
		g.setColor(ballColor);
		g.fillOval(ball.x - map.getCameraX(), ball.y, ball.width, ball.height);

		// Vẽ thông tin
		g.setColor(textColor);
		g.setFont(infoFont);
		g.drawString("Time: " + presenter.getFormattedPlayTime(), 10, 50);
		g.drawString("Map: " + (model.getCurrentMapIndex() + 1), 10, 70); // Thêm lại hiển thị Map
		g.drawString("Press SPACE to Pause!", 10, 90); // Điều chỉnh vị trí để tránh chồng lấn

		// Vẽ thông báo paused
		if (presenter.isPaused() && !presenter.isDead() && !presenter.hasWonFinalMap()) {
			g.setColor(textColor);
			g.setFont(titleFont);
			g.drawString("Paused", 200, 200);
		}

		// Vẽ màn hình kết thúc
		if (presenter.isDead() || presenter.hasWonFinalMap()) {
			drawEndGameScreen(g, presenter);
		}
	}

	private void drawEndGameScreen(Graphics g, GamePresenter presenter) {
		g.setColor(textColor);
		g.setFont(titleFont);

		if (presenter.isDead()) {
			g.drawString("GAME OVER", 200, 200);
			g.setFont(subtitleFont);
			g.drawString("Press R to Restart", 230, 250);
			g.drawString("Press Q to Quit", 240, 280);
		} else {
			g.drawString("YOU WIN!", 220, 200);
		}

		g.setFont(subtitleFont);
		g.drawString("Time Played: " + presenter.getFormattedPlayTime(), 230, 310);

		List<String> top3 = presenter.getTop3Players();
		if (top3 != null && !top3.isEmpty()) {
			g.setFont(leaderboardFont);
			g.drawString("Top 3 Players:", 230, 340);
			g.setFont(subtitleFont);
			int y = 370;
			for (String info : top3) {
				g.drawString(info, 240, y);
				y += 25;
			}
		}
	}
}

