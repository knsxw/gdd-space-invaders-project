package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TitleScene extends JPanel {

    private int frame = 0;
    private Image image;
    private AudioPlayer audioPlayer;
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private Timer timer;
    private Game game;

    public TitleScene(Game game) {
        this.game = game;
        addKeyListener(new TAdapter());
    }

    private void initBoard() {

    }

    public void start() {
        setFocusable(true);
        setBackground(Color.black);
        javax.swing.SwingUtilities.invokeLater(this::requestFocusInWindow);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        initTitle();
        initAudio();
    }

    public void stop() {
        try {
            if (timer != null) {
                timer.stop();
            }

            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void initTitle() {
        var ii = new ImageIcon(IMG_TITLE);
        image = ii.getImage();

    }

    private void initAudio() {
        try {
            String filePath = "src/audio/title.wav";
            audioPlayer = new AudioPlayer(filePath);

            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error with playing sound.");
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        g.drawImage(image, 0, 0, d.width, d.height, this);
        g.setColor(new Color(0, 0, 20, 145));
        g.fillRoundRect(45, 70, d.width - 90, 180, 28, 28);

        g.setColor(Color.white);
        g.setFont(new Font("Dialog", Font.BOLD, 48));
        String title = "GDD PROJECT 1";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (d.width - titleWidth) / 2, 142);
        g.setColor(Color.cyan);
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        String subtitle = "A SIDE-SCROLLING SPACE ODYSSEY";
        int subtitleWidth = g.getFontMetrics().stringWidth(subtitle);
        g.drawString(subtitle, (d.width - subtitleWidth) / 2, 180);
        g.setColor(Color.white);
        g.setFont(new Font("Dialog", Font.PLAIN, 16));
        String team = "Khine Khant  •  Hein Oke Soe";
        int teamWidth = g.getFontMetrics().stringWidth(team);
        g.drawString(team, (d.width - teamWidth) / 2, 220);

        if (frame % 60 < 30) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.white);
        }

        g.setFont(new Font("Dialog", Font.BOLD, 28));
        String text = "Press SPACE to Start";
        int stringWidth = g.getFontMetrics().stringWidth(text);
        int x = (d.width - stringWidth) / 2;
        // int stringHeight = g.getFontMetrics().getAscent();
        // int y = (d.height + stringHeight) / 2;
        g.setColor(new Color(0, 0, 20, 185));
        g.fillRoundRect(x - 25, 555, stringWidth + 50, 65, 22, 22);
        g.setColor(frame % 60 < 30 ? Color.CYAN : Color.WHITE);
        g.drawString(text, x, 598);

        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.drawString("ARROWS move  •  SPACE fires  •  Collect power upgrades", 185, 650);

    }

    private void update() {
        frame++;
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("Title.keyPressed: " + e.getKeyCode());
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_SPACE) {
                // Load the next scene
                game.loadScene1();
            }

        }
    }
}
