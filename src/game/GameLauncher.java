package game;

import javax.swing.JFrame;

public class GameLauncher {
    public static void main(String[] args) {
        // ウィンドウ作成
        JFrame frame = new JFrame("Mini Mario");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ゲーム本体
        MiniMario miniMario = new MiniMario();
        frame.getContentPane().add(miniMario);

        // ウィンドウ設定
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
