package game;

import java.awt.*;

public class PlayerProjectile extends Projectile {
    private int damage = 1;
    private int mapWidth = 22000;

    // ① 向きを指定するコンストラクタ
    public PlayerProjectile(int x, int y, boolean facingRight) {
        super(x, y, facingRight ? 15 : -15, 0);
        this.width = 40;
        this.height = 40;
    }

    // ② 速度を直接指定するコンストラクタ
    public PlayerProjectile(int x, int y, int speed) {
        super(x, y, speed, 0);
        this.width = 40;
        this.height = 40;
    }

    @Override
    public void update() {
        super.update();
        if (x < -50 || x > mapWidth) {
            setAlive(false);
        }
    }

    @Override
    public int getDamage() {
        return damage;
    }

    public void setDamage(int dmg) {
        this.damage = dmg;
    }

    public void draw(Graphics g, int cameraX, Image sprite) {
        if (!isAlive())
            return;
        if (sprite != null) {
            g.drawImage(sprite, getX() - cameraX, getY(), getWidth(), getHeight(), null);
        } else {
            super.draw(g, cameraX);
        }
    }
}