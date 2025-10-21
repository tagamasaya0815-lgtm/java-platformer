package game;

import java.awt.*;

public class AttackEffect {
    int x, y;
    int lifeTime;
    Color color;

    public AttackEffect(int x, int y) {
        this.x = x; this.y = y;
        this.lifeTime = 20;
        this.color = Color.ORANGE;
    }

    public void update() { lifeTime--; }

    public void draw(Graphics g, int cameraX) {
        if (lifeTime <= 0) return;
        Graphics2D g2 = (Graphics2D) g;
        float alpha = Math.max(0.0f, lifeTime / 20.0f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(color);
        g2.fillOval(x - cameraX, y, 30, 30);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public boolean isAlive() { return lifeTime > 0; }
}

