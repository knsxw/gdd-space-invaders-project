package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import javax.swing.JFrame;

public class Game extends JFrame {
    private final TitleScene titleScene;
    private final Scene1 scene1;
    private final Scene2 scene2;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
        setTitle("Nebula Vanguard");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        loadTitle();
    }

    private void showScene(javax.swing.JPanel scene) {
        getContentPane().removeAll();
        add(scene);
        revalidate();
        repaint();
    }

    public void loadTitle() {
        scene1.stop();
        scene2.stop();
        showScene(titleScene);
        titleScene.start();
    }

    public void loadScene1() {
        titleScene.stop();
        showScene(scene1);
        scene1.start();
    }

    public void loadScene2() {
        scene1.stop();
        showScene(scene2);
        scene2.startWithProgress(scene1.getPlayer(), scene1.getScore());
    }
}
