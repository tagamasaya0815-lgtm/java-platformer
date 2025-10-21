package game;

import java.awt.*;

public class Projectile {
    protected int x, y;
    protected int width = 35, height = 30; //ボス関係の弾やらなにやらの値変更場所です
    protected int dx, dy;
    protected boolean alive = true;
    protected int lifeTime = 200;
    protected Color color = Color.ORANGE; // ← 追加

    public Projectile(int x, int y, int dx, int dy) {
        this.x = x; this.y = y; this.dx = dx; this.dy = dy;
    }

    public void update() {
        x += dx; y += dy;
        if (--lifeTime <= 0) alive = false;
    }

    // 共通API
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { alive = a; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void draw(Graphics g, int cameraX) {
        if (!alive) return;
        g.setColor(color);                              // ← ここで color を使う
        g.fillOval(x - cameraX, y, width, height);
    }

    public int getDamage() { return 1; }
}