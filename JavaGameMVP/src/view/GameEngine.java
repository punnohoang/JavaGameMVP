package view;

import presenter.GamePresenter;

public class GameEngine {
    private final GamePresenter presenter;

    public GameEngine(GamePresenter presenter) {
        this.presenter = presenter;
    }

    public void update() {
        presenter.update();
    }

    public GamePresenter getPresenter() {
        return presenter;

    }
}
