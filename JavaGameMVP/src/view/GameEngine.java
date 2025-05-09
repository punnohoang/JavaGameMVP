package view;

import presenter.GamePresenter;

public class GameEngine {
    private final GamePresenter presenter;

    public GameEngine(GamePresenter presenter) {
        this.presenter = presenter;
    }

    public void update() {
        if (!presenter.isPaused() && !presenter.hasWonFinalMap() && !presenter.isDead()) {
            presenter.update(); // Chỉ cập nhật khi không paused, chưa thắng, và chưa chết
        }
    }

    public GamePresenter getPresenter() {
        return presenter;
    }
}
