package game;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class Block {
    private int x, y, width, height;
    private Image image; // ブロック画像

    public Block(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public boolean isOnTop(int playerX, int playerY, int playerWidth, int playerHeight) {
        return playerY + playerHeight <= y + 5 &&
               playerX + playerWidth > x && playerX < x + width &&
               playerY + playerHeight >= y; // 上に乗った判定
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void draw(Graphics g, int cameraX) {
        if (image != null) {
            g.drawImage(image, x - cameraX, y, width, height, null);
        } else {
            // 透明
            g.setColor(new Color(0,0,0,0));
            g.fillRect(x - cameraX, y, width, height);
        }
    }
}