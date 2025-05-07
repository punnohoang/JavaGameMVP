package view;

import presenter.GamePresenter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputManager extends KeyAdapter {
    private final GamePresenter presenter;

    public InputManager(GamePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        presenter.handleKeyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        presenter.handleKeyReleased(e.getKeyCode());
    }
}
