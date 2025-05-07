package view;

import presenter.GamePresenter;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GamePanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final GameEngine engine;
    private final Renderer renderer;
    private final Timer timer;

    public GamePanel(String playerName) {
        GamePresenter presenter = new GamePresenter(new model.GameModel(), playerName);
        this.engine = new GameEngine(presenter);
        this.renderer = new Renderer();

        setPreferredSize(new Dimension(640, 500));
        setFocusable(true);
        addKeyListener(new InputManager(presenter));

        timer = new Timer(15, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        engine.update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render(g, engine.getPresenter(), getWidth());
    }
}
