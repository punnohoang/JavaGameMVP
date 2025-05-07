package view;

import presenter.GamePresenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameEngine implements ActionListener {
    private final GamePresenter presenter;
    private final Timer timer;

    public GameEngine(GamePresenter presenter) {
        this.presenter = presenter;
        timer = new Timer(15, this);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public GamePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.update();
    }
}
