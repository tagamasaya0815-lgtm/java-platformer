package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class CharacterSelectScene extends Scene {
    private List<BufferedImage> characters; // 8キャラの画像
    private int selectedIndex = 0;

    // キー入力を MiniMario から渡す
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean enterPressed = false;

    // キャラクター描画サイズ（簡単に変更可能）
    private int charWidth = 100;
    private int charHeight = 100;

    public CharacterSelectScene(List<BufferedImage> characters) {
        this.characters = characters;
    }

    // キャラクターサイズ変更用メソッド
    public void setCharacterSize(int width, int height) {
        this.charWidth = width;
        this.charHeight = height;
    }

    public void setLeftPressed(boolean pressed) { leftPressed = pressed; }
    public void setRightPressed(boolean pressed) { rightPressed = pressed; }
    public void setEnterPressed(boolean pressed) { enterPressed = pressed; }

    @Override
    public void update() {
        // 左右移動
        if (leftPressed) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = characters.size() - 1;
            leftPressed = false; // 1回移動したらリセット
        }
        if (rightPressed) {
            selectedIndex++;
            if (selectedIndex >= characters.size()) selectedIndex = 0;
            rightPressed = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.PINK);
        g.fillRect(0, 0, 1980, 1080);

        for (int i = 0; i < characters.size(); i++) {
            int x = 200 + (i % 4) * 350;
            int y = 200 + (i / 4) * 300;
            
            // キャラクターを指定サイズで描画
            g.drawImage(characters.get(i), x, y, charWidth, charHeight, null);

            // 選択中キャラクターの枠
            if (i == selectedIndex) {
                g.setColor(Color.WHITE);
                g.drawRect(x - 5, y - 5, charWidth + 10, charHeight + 10);
            }
        }

        g.setColor(Color.BLACK);
        g.drawString("← → でキャラ選択、Enterで決定", 700, 650);
    }

    public void moveLeft() {
        selectedIndex = (selectedIndex + characters.size() - 1) % characters.size();
    }

    public void moveRight() {
        selectedIndex = (selectedIndex + 1) % characters.size();
    }

    public int getSelectedIndex() { return selectedIndex; }
}
