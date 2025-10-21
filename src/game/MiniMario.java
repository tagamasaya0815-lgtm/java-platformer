package game;

// -*- coding: utf-8 -*-
// MiniMario.java
// 完全版：スコア・アニメーション・BGM・障害物画像・ゴール表示・ゲームオーバー含む

// このコードはJavaの勉強のため色々調べながら独学で書いたものですが、初心者のため神クラス化してしまいました
// ただ成果物としては「動く」ということが確認できて楽しく作れました
// これから現在学習中のオブジェクト指向を意識しながらクラス分けをし、徐々に編集していくつもりです
// お見苦しい点もございますが、Java初学者の勉強用としてご容赦ください

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class MiniMario extends JPanel implements ActionListener, KeyListener{
    private Scene currentScene;                     // ← ここに置く（フィールド）
    private List<BufferedImage> characterImages;
    private int selectedCharacterIndex = 0;
    private List<CharacterData> characters = new ArrayList<>();
    private CharacterData currentCharacter;
    private boolean isMoving = false;               // プレイヤーが動いているか
    private int animationFrame = 0;                 // アニメーションのカウンター
    private List<Block> blocks = new ArrayList<>();
    private Image playerProjectileImage;
    private boolean upPressed = false;
    private boolean AttackPressed = false;          // ← これを追加
    private Image playerJump;
    private BossEnemy boss;                         // MiniMario.java 内
    private BossEnemy midBoss;                      // 二体目の中ボス↓も同じ感じ
    private BossEnemy midBoss2;
    private BossEnemy midBoss3;

    private boolean blinkClearText = true;          // クリア文字の点滅ON/OFF
    private int blinkCounterClear = 0;              // 点滅カウンター
    
    private boolean gameCleared = false;

    // --- Player HP ---
    private int playerHP = 5;                       // 初期HP
    private final int MAX_PLAYER_HP = 5;            // 最大HP
    private boolean invincible = false;             // 無敵状態かどうか
    private int invincibleTimer = 0;                // 無敵時間カウント（フレーム）

    private Image heartImage;

    private boolean attackPressed = false;              // 攻撃ボタン押下状態
    private int attackCooldown = 0;                     // 攻撃のクールダウン時間
    private final int ATTACK_COOLDOWN_TIME = 20;        // 攻撃間隔（フレーム数）

    private int jumpCount = 0;                          // 現在のジャンプ回数
    // private final int MAX_JUMP = 3;                  // 3段ジャンプなので最大3回(ここに関しては別のクラスでジャンプ実装したので)

    // --- Recovery Items ---
    private java.util.List<Rectangle> recoveryItems = new ArrayList<>();
    private java.util.List<Image> recoveryItemImages = new ArrayList<>();
    private java.util.List<Boolean> recoveryItemAlive = new ArrayList<>();

    // 気弾リスト
    private java.util.List<EnemyProjectile> projectiles = new ArrayList<>();
    // private java.util.List<Explosion> explosions = new ArrayList<>();

    // 効果音
    private Clip shootClip;
    private Clip hitClip;
    private Clip itemGetClip;
    
    // --- Player State ---
    private int playerX = 50, playerY = 600;
    private int playerWidth = 75, playerHeight = 75;
    private int velocityY = 0;
    private boolean onGround = true;
    private boolean leftPressed = false, rightPressed = false;
    private boolean facingRight = true;
    private boolean isGameOver = false;

    // --- Player State ---
    private boolean jumpPressed = false;   // ジャンプ入力フラグ
    // private boolean attackPressed = false; // 攻撃ボタン押下状態

    // --- BGM
    private Clip titleBGM;
    private Clip playBGM;
    private Clip gameOverBGM;
    private Clip gameClearBGM;
    
    // ---  ここからタイトル画面の作成
    private Image titleBackground;
    private boolean blinkText = true;
    private int blinkCounterTitle = 0;

    private enum GameState {
        TITLE,
        CHARACTER_SELECT,
        PLAYING,
        GAME_OVER,
        GAME_CLEAR
    }

    private boolean attackJustPressed = false;          // ←追加（押した瞬間フラグ）
    private boolean prevAttackPressed = false;          // 前フレーム押されてたか

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;

    private GameState gameState = GameState.TITLE;
    
    private void updatePlayerAnimation() {
        if (leftPressed || rightPressed) {
            isMoving = true;
            animationFrame++;
        } else {
            isMoving = false;
            animationFrame = 0;
        }
    }

    private double playerYVel = 0;   // プレイヤーの縦方向速度

    private void updatePlayer() {
        updatePlayerImageSet();
        
        // 無敵時間更新
        if (invincible) {
            invincibleTimer--;
            if (invincibleTimer <= 0) {
                invincible = false;
            }
        }
            
    }
    
    private void updateEnemies() {
        updatePlayerImageSet();
        // 敵の更新処理を書く
    }
    
    // --- Camera ---
    private int cameraX = 0;

    // --- Enemies, Obstacles, Goal ---
    private java.util.List<Rectangle> enemies = new ArrayList<>();
    private java.util.List<Image> enemyImages = new ArrayList<>();
    private java.util.List<Boolean> enemyAlive = new ArrayList<>();
    private java.util.List<Rectangle> obstacles = new ArrayList<>();
    private java.util.List<Image> obstacleImages = new ArrayList<>();
    private Rectangle goalRect = new Rectangle(21000, 120, 700, 770);

    // --- Enemy Attack (Projectiles) ---
    private Random random = new Random();
    private int enemyAttackCooldown = 0; // cooldown frames for next enemy attack
    private java.util.List<Projectile> enemyProjectiles = new ArrayList<>();
    
    // 敵の横移動用速度（左右にごぞごぞ動かす用）
    private java.util.List<Integer> enemyVelocities = new ArrayList<>();

    // 敵の移動可能範囲X座標[min,max]
    private java.util.List<int[]> enemyMoveRanges = new ArrayList<>();

    // --- Game State ---
    private boolean gameOver = false;
    private boolean gameClear = false;
    private int score = 0;
    private long startTime;

    // --- Timer and Sounds ---
    private javax.swing.Timer timer;
    private Clip bgmClip, jumpClip;

    // --- Player Images & Animation ---
    private Image playerIdle, playerLeft;
    private Image[] playerRun = new Image[2];
    private int runFrame = 0;
    private int frameCount = 0;
    private Image goalImage;

    private final int GRAVITY = 1;
    private final int JUMP_STRENGTH = -20;

    private void gameOver() {
        gameOver = true;            // 既存のフラグを立てる
        playGameOverBGM();          // ゲームオーバーBGMを再生
        // 他に必要な処理（リトライ表示やスコア保存など）を書く
    }

    private void damagePlayer(int amount) {
        if (invincible) return;                 // 無敵中はダメージ無効

        playerHP -= amount;
        if (playerHP <= 0) {
            playerHP = 0;
            gameOver(); // 既存のゲームオーバー処理を呼び出し
        } else {
            // 無敵時間付与
            invincible = true;
            invincibleTimer = 60; // 約1秒無敵
        }
    }

    // 気弾クラス
    class EnemyProjectile {
        int x, y, width, height, speedX;
        boolean alive = true;

        public EnemyProjectile(int x, int y, int speedX) {
            this.x = x;
            this.y = y;
            this.width = 16;
            this.height = 16;
            this.speedX = speedX;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }
    
    // 爆発エフェクトクラス(ここ多分上手く実装できてないから保留中)
    class Explosion {
        int x, y;
        int frame = 0;
        int maxFrame = 6;
        boolean alive = true;

        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            frame++;
            if (frame > maxFrame) {
                alive = false;
            }
        }
    }
    
    private void updateGame() {
        updatePlayerImageSet();
        // 他の更新処理
        updateProjectiles();
        // updateExplosions();
        updateRecoveryItems(); // ← 回復アイテム更新

        updatePlayer();
        checkProjectileCollisions();        // 敵弾とプレイヤー
        checkPlayerProjectileCollisions();  // プレイヤー弾と敵
        
        // --- ボスBGM再生 ---
        // if (boss != null && boss.isAlive()) {
        //     if (!bossBGMStarted && playerX >= 20000) {
        //         if (boss.getBGM() != null) boss.getBGM().loop(Clip.LOOP_CONTINUOUSLY);
        //         bossBGMStarted = true;
        //     }
        // }       
            // この項目に関しても実装がうまくいっていません、ステージの構成とかいろいろ→保留中
        // --- ボス死亡でBGM停止 ---
        // if (boss != null && !boss.isAlive() && bossBGMStarted) {
        //     if (boss.getBGM() != null) boss.getBGM().stop();
        //     bossBGMStarted = false;
        // }
        // boss.update(playerX, playerY);
        
        cameraX = playerX - 200;

        if (currentScene != null) {
            currentScene.update();
        }

        // --- 移動処理（キャラごとのスピード） ---
        if (leftPressed) {
            playerX -= currentCharacter.moveSpeed;  // キャラごと
            facingRight = false;
        }

        if (rightPressed) {
            playerX += currentCharacter.moveSpeed;
            facingRight = true;
        }

        // --- ジャンプ処理（キャラごとのジャンプ力、3段ジャンプ対応） ---
        if (jumpPressed && jumpCount < currentCharacter.maxJump) {
            velocityY = -currentCharacter.jumpPower;     // キャラごと
            playSE(currentCharacter.voiceJump);          // ボイス（ジャンプ）
            jumpCount++;
            jumpPressed = false; // 押しっぱなしで暴発しないように
        }

        // 重力
        velocityY += 1;
        playerY += velocityY;

        // 地面に着地したらリセット
        if (playerY >= 600) { // ← あなたのステージに合わせて調整
            playerY = 600;
            velocityY = 0;
            onGround = true;
            jumpCount = 0;
        } else {
            onGround = false;
        }

        
        // --- 攻撃処理 ---
        if (attackPressed && attackCooldown == 0) {
            shootProjectile();
            attackCooldown = ATTACK_COOLDOWN_TIME;
        }

        if (attackCooldown > 0) attackCooldown--;

       for (int i = 0; i < playerProjectiles.size(); i++) {
            PlayerProjectile p = playerProjectiles.get(i);
            p.update();
            if (!p.alive) {
                playerProjectiles.remove(i);
                i--;
            }
        }
        
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (int i = 0; i < recoveryItems.size(); i++) {
            if (!recoveryItemAlive.get(i)) continue; // もう取ったアイテムは無視

            Rectangle itemRect = recoveryItems.get(i);
            if (playerRect.intersects(itemRect)) {

                // アイテムを取った
                recoveryItemAlive.set(i, false); // 画面から消す

                // プレイヤーHP回復
                playerHP = Math.min(playerHP + 1, MAX_PLAYER_HP);

                // 取得音を鳴らす（itemGetClipがある場合）
                if (itemGetClip != null) {
                    playSE(itemGetClip);
                }
            }
        }

        // --- プレイヤー弾と敵の当たり判定 ---
        for (int i = 0; i < enemies.size(); i++) {
            if (!enemyAlive.get(i)) continue;

            Rectangle enemyRect = enemies.get(i);

            for (PlayerProjectile p : playerProjectiles) {
                if (!p.alive) continue;

                Rectangle projRect = new Rectangle(p.x, p.y, p.width, p.height);

                if (projRect.intersects(enemyRect)) {
                    // 敵を倒す
                    enemyAlive.set(i, false);
                    score += 100;                       // スコア加算（任意）
                    p.setAlive(false);
                }
            }
        }
    }
    
    private void updateProjectiles() {
        for (EnemyProjectile p : projectiles) {
            p.x += p.speedX;
            if (p.x < 0 || p.x > 22000) {
                p.alive = false;
            }

            // 敵との当たり判定はここに追加可能
            for (int j = 0; j < enemies.size(); j++) {
                if (!enemyAlive.get(j)) continue;
                if (p.getBounds().intersects(enemies.get(j))) {
                    enemyAlive.set(j, false);       // 敵を倒す
                    p.alive = false;
                    score += 100;

                    playSE(hitClip);        // 命中音再生
                    break;
                }
            }
            
        }
        projectiles.removeIf(p -> !p.alive);

        for (int i = 0; i < playerProjectiles.size(); i++) {
            PlayerProjectile p = playerProjectiles.get(i);
            p.update();

            // 画面外に出たら削除
            if (p.x < 0 || p.x > 22000) {
            playerProjectiles.remove(i);
            i--;
            }
        }
    }

    
    // 弾と敵の当たり判定
    private void checkProjectileCollisions() {
        
        for (int i = 0; i < playerProjectiles.size(); i++) {
        PlayerProjectile p = playerProjectiles.get(i);
        if (!p.isAlive()) continue;

        Rectangle pRect = p.getBounds();               // Projectile から取得

        for (int j = 0; j < enemies.size(); j++) {
            if (!enemyAlive.get(j)) continue;          // 倒された敵はスキップ

            Rectangle eRect = enemies.get(j);
            if (pRect.intersects(eRect)) {
                // 敵に命中！
                enemyAlive.set(j, false);      // 敵を倒す
                p.setAlive(false);                   // 弾を消す
                break;                                 // この弾は処理済みなので抜ける
            }
        }
    }
    }
    
    // ★ 新しく追加: プレイヤー弾と敵の当たり判定
    private void checkPlayerProjectileCollisions() {
    for (int i = 0; i < playerProjectiles.size(); i++) {
        PlayerProjectile p = playerProjectiles.get(i);
            if (!p.isAlive()) continue;

            Rectangle pRect = p.getBounds();

            for (int j = 0; j < enemies.size(); j++) {
                if (!enemyAlive.get(j)) continue;

                Rectangle eRect = enemies.get(j);
                if (pRect.intersects(eRect)) {
                    enemyAlive.set(j, false);  // 敵を倒す
                    p.setAlive(false);         // 弾を消す
                    break;
                }
            }
        }
    }
    
    // private void updateExplosions() {
    //     for (Explosion e : explosions) {
    //         e.update();
    //     }
    //     explosions.removeIf(e -> !e.alive);
    // }

    //この上下に関しては実装しようとしたが上手くいかなかったので保留にしています↑↓（爆発演出ですね）

    // private void drawExplosions(Graphics g) {
    //     for (Explosion e : explosions) {
    //         int size = 32 + e.frame * 4;
    //         int alpha = 255 - e.frame * 40;
    //         alpha = Math.max(0, alpha);

    //         g.setColor(new Color(255, 200, 50, alpha));
    //         g.fillOval(e.x - size / 2, e.y - size / 2, size, size);

    //         g.setColor(new Color(255, 100, 0, alpha));
    //         g.drawOval(e.x - size / 2, e.y - size / 2, size, size);
    //     }
    // }

    private void loadSE() {
        try {
            URL shootUrl = Objects.requireNonNull(getClass().getResource("/sounds/shoot.wav"));
            AudioInputStream shootStream = AudioSystem.getAudioInputStream(shootUrl);
            shootClip = AudioSystem.getClip();
            shootClip.open(shootStream);

            URL hitUrl = Objects.requireNonNull(getClass().getResource("/sounds/hit.wav"));
            AudioInputStream hitStream = AudioSystem.getAudioInputStream(hitUrl);
            hitClip = AudioSystem.getClip();
            hitClip.open(hitStream);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            URL itemGetUrl = Objects.requireNonNull(getClass().getResource("/sounds/item_get.wav"));
            AudioInputStream itemGetStream = AudioSystem.getAudioInputStream(itemGetUrl);
            itemGetClip = AudioSystem.getClip();
            itemGetClip.open(itemGetStream);
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSE(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }
   
    
    public MiniMario() {

        setPreferredSize(new Dimension(1920, 1080));
        setFocusable(true);
        addKeyListener(this);

        loadImages(); //　画像
        loadSounds(); // サウンド
        loadBGM();    // BGM
        loadSE();     // 効果音を読み込み

        // --- キャラクター読み込み ---
        
        loadCharacters();
        
        playTitleBGM();

        setupWorld();
        
        loadBoss(); //出現させるボスキャラを増やせます
        // loadMidBoss();
        // loadMidBoss2();
        // loadMidBoss3();
        timer = new javax.swing.Timer(16, this);
        timer.start();
        startTime = System.currentTimeMillis();
        
        updateGame();
        repaint();

        Image blockImg = null;
        List<Image> blockImages = new ArrayList<>();
        try {
            // 複数の画像を読み込む
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block2.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block3.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block4.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block5.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block6.png"))));
            blockImages.add(ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/block7.png"))));
    
            // ランダムブロックを追加（リストを渡す）
            generateRandomBlocks(100, 22000, 350, blockImages);

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<BufferedImage> characterImages = new ArrayList<>();
        try {
            characterImages.add(ImageIO.read(getClass().getResource("/images/char1.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char2.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char3.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char4.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char5.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char6.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char7.png")));
            characterImages.add(ImageIO.read(getClass().getResource("/images/char8.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // --- CharacterSelectScene を作成して currentScene にセット ---
        currentScene = new CharacterSelectScene(characterImages);
        currentCharacter = characters.get(0);  // 初期キャラ

        // ArrayList<Boolean> recoveryItemAlive = new ArrayList<>(); // trueなら画面に表示

        try {
                titleBackground = ImageIO.read(
                Objects.requireNonNull(getClass().getResource("/images/title_background.jpg"))
            );
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
                heartImage = ImageIO.read(
                Objects.requireNonNull(getClass().getResource("/images/heart.png"))
                );  // ← Fileで統一
            } catch (IOException e) {
            e.printStackTrace();
        }

        // 追加例
        int[][] positions = {
            {1000, 300},
            {2000, 150},
            {3000, 310},
            {4000, 260},
            {5000, 200},
            {6000, 300},
            {7000, 150},
            {8000, 310},
            {9000, 260},
            {10000, 200},
            {11000, 300},
            {12000, 150},
            {13000, 310},
            {15000, 260},
            {17000, 200},
        };

        for (int[] pos : positions) {
            recoveryItems.add(new Rectangle(pos[0], pos[1], 60, 60));
            recoveryItemImages.add(heartImage);
            recoveryItemAlive.add(true);
        }

             
        recoveryItemImages.add(heartImage);
        recoveryItemAlive.add(true);

        try {
                playerProjectileImage = ImageIO.read(
                Objects.requireNonNull(getClass().getResource("/images/player_shot.png"))
                );
            } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void generateRandomBlocks(int numBlocks, int mapWidth, int maxY, List<Image> blockImages) {
        Random rand = new Random();

        int width = 100;  
        int height = 53;

        for (int i = 0; i < numBlocks; i++) {
            int x = rand.nextInt(mapWidth);
            int y = rand.nextInt(maxY + 1);

            // 複数画像からランダム選択
            Image chosenImage = blockImages.get(rand.nextInt(blockImages.size()));

            blocks.add(new Block(x, y, width, height, chosenImage));
        }
    }

    private void updateRecoveryItems() {
        for (int i = 0; i < recoveryItems.size(); i++) {
            if (!recoveryItemAlive.get(i)) continue;
            Rectangle item = recoveryItems.get(i);
            Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

            if (playerRect.intersects(item)) {
                // HP回復（最大値を超えないように）
                playerHP = Math.min(playerHP + 1, MAX_PLAYER_HP);

                // アイテムを消す
                recoveryItemAlive.set(i, false);

                // 効果音（任意）
                playSE(hitClip);
                playSE(itemGetClip); 
            }
        }
    }

    private void drawRecoveryItems(Graphics g) {
        for (int i = 0; i < recoveryItems.size(); i++) {
            if (!recoveryItemAlive.get(i)) continue;
            Rectangle item = recoveryItems.get(i);
            g.drawImage(recoveryItemImages.get(i), item.x - cameraX, item.y, item.width, item.height, null);
        }
    }


    private Image idleImg;
    private Image idleImg2;
    private Image run1;
    private Image run2;
    private Image run3;
    private Image run4;

    // 今どっちのセットを使っているか
    private boolean useSet2 = false;

    // 座標監視
    private void updatePlayerImageSet() {
        int switchY = 400; // 境界線のY座標

        
        if (playerY < switchY) {
            useSet2 = true;
        } else {
            useSet2 = false;
        }
    }

    private void drawPlayer(Graphics g) {

        updatePlayerImageSet();

        Image idle, run_1, run_2;

        if (useSet2) {
            idle = idleImg2;
            run_1 = run3;
            run_2 = run4;
        } else {
            idle = idleImg;
            run_1 = run1;
            run_2 = run2;
        }

        g.drawImage(idle, playerX, playerY, null);
        
    }


    // --- Load images ---
    private void loadImages() {

        
        try {
                playerProjectileImage = ImageIO.read(
                Objects.requireNonNull(getClass().getResource("/images/player_shot.png"))
                );
            } catch (IOException e) {
            e.printStackTrace();
        }


        try {
        
            try {
            BufferedImage idleImg = ImageIO.read(
            Objects.requireNonNull(getClass().getResource("/images/player_idle1.png"))
            );
            playerIdle = idleImg.getScaledInstance(playerWidth, playerHeight, Image.SCALE_SMOOTH);
            playerLeft = flipImage(idleImg);

            BufferedImage run1 = ImageIO.read(
            Objects.requireNonNull(getClass().getResource("/images/player_run1.png"))
            );
            BufferedImage run2 = ImageIO.read(
                Objects.requireNonNull(getClass().getResource("/images/player_run2.png"))
            );
            playerRun[0] = run1.getScaledInstance(playerWidth, playerHeight, Image.SCALE_SMOOTH);
            playerRun[1] = run2.getScaledInstance(playerWidth, playerHeight, Image.SCALE_SMOOTH);

            } catch (Exception e) {
                e.printStackTrace();
            }
            this.idleImg = idleImg;
            this.run1 = run1;
            this.run2 = run2;


            BufferedImage enemy1  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy1.png")));
            BufferedImage enemy2  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy2.png")));
            BufferedImage enemy3  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy3.png")));
            BufferedImage enemy4  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy4.png")));
            BufferedImage enemy5  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy5.png")));
            BufferedImage enemy6  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy6.png")));
            BufferedImage enemy7  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy7.png")));
            BufferedImage enemy8  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy8.png")));
            BufferedImage enemy9  = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy9.png")));
            BufferedImage enemy10 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy10.png")));
            BufferedImage enemy11 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy11.png")));
            BufferedImage enemy12 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/enemy12.png")));
           
                
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 60, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80,80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy12.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy1.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy2.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy3.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy4.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy5.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy6.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy7.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy8.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy9.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy10.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            enemyImages.add(enemy11.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            


            BufferedImage obs1 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/obstacle1.png")));
            BufferedImage obs2 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/obstacle2.png")));
            BufferedImage obs3 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/obstacle3.png")));
            BufferedImage obs4 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/obstacle5.png")));
            BufferedImage obs5 = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/obstacle6.png")));



            obstacleImages.add(obs1.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            obstacleImages.add(obs2.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            obstacleImages.add(obs3.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
            obstacleImages.add(obs4.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
             obstacleImages.add(obs5.getScaledInstance(200, 200, Image.SCALE_SMOOTH));

            BufferedImage goalImg = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/goal.png")));
            goalImage = goalImg.getScaledInstance(600, 600, Image.SCALE_SMOOTH);

        } 
                
        catch (IOException e) {
            System.out.println("Failed to load images: " + e.getMessage());
        }
    }

    private void playJumpSE() {
        if (jumpClip != null) {
            jumpClip.stop();
            jumpClip.setFramePosition(0);
            jumpClip.start();
        }
    }


        

    // --- Load sounds ---
    private void loadSounds() {
        try {
            // BGM
            URL bgmUrl = Objects.requireNonNull(getClass().getResource("/sounds/play_bgm.wav"));
            AudioInputStream bgm = AudioSystem.getAudioInputStream(bgmUrl);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(bgm);

            // ジャンプSE
            URL jumpUrl = Objects.requireNonNull(getClass().getResource("/sounds/jump.wav"));
            AudioInputStream jump = AudioSystem.getAudioInputStream(jumpUrl);
            jumpClip = AudioSystem.getClip();
            jumpClip.open(jump);

            // ゲームオーバーBGM
            URL gameOverUrl = Objects.requireNonNull(getClass().getResource("/sounds/gameover_bgm.wav"));
            AudioInputStream gameOverStream = AudioSystem.getAudioInputStream(gameOverUrl);
            gameOverBGM = AudioSystem.getClip();
            gameOverBGM.open(gameOverStream);

            // ゲームクリアBGM
            URL clearUrl = Objects.requireNonNull(getClass().getResource("/sounds/gameClear_bgm.wav"));
            AudioInputStream clearStream = AudioSystem.getAudioInputStream(clearUrl);
            gameClearBGM = AudioSystem.getClip();
            gameClearBGM.open(clearStream);
        }
            
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 空中障害物の垂直移動速度
    private java.util.List<Integer> obstacleVelocities = new ArrayList<>();
    // 空中障害物の垂直移動範囲Y座標[min,max]
    private java.util.List<int[]> obstacleMoveRanges = new ArrayList<>();

    // --- Setup initial world ---
    private void setupWorld() {
        enemies.clear();
        enemyAlive.clear();
        obstacleVelocities.clear();
        obstacleMoveRanges.clear();
        obstacles.clear();
        enemyVelocities.clear();
        enemyMoveRanges.clear();


        enemies.add(new Rectangle(500, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(900, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(1200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(1500, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(1800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(2100, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(2400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(2900, 617, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(3000, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(3300, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(3800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4000, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4400, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5000, 607, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5300, 609, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5500, 603, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5900, 604, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6900, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6100, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6200, 620, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6300, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(1300, 50, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(2000, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4000, 60, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5000, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5600, 50, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(21280, 221, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6000, 604, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(720, 590, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(2600, 290, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4200, 590, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(9800, 290, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(5000, 240, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(4360, 260, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6510, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6600, 595, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(6050, 220, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(7500, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(7900, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8500, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(9100, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(9400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(9900, 617, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(10000, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(10300, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(10800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11000, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11400, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12000, 607, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12300, 609, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12500, 603, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12900, 604, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13000, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13100, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14200, 620, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14300, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8300, 50, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(9020, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11060, 60, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12080, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(12600, 50, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13080, 221, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(15000, 604, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(7720, 590, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(10700, 290, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11700, 590, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8800, 290, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14100, 240, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(11360, 260, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13510, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13600, 595, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(13250, 220, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14500, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(14900, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(15200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(15500, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(15800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(16100, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(16400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(16900, 617, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(17300, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(17500, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(17900, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(18200, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(18500, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(18800, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19100, 615, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19900, 617, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20000, 40, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20100, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20200, 620, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20300, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20400, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20500, 620, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20600, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20700, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(20900, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(7620, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(8110, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(10212, 620, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(17220, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(18420, 610, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19600, 600, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19650, 340, 80, 80)); enemyAlive.add(true);
        enemies.add(new Rectangle(19700, 230, 80, 80)); enemyAlive.add(true);

        obstacles.add(new Rectangle(700, 620, 200, 200));
        obstacles.add(new Rectangle(1100, 230, 200, 200));
        obstacles.add(new Rectangle(1300, 610, 200, 200));
        obstacles.add(new Rectangle(1700, 628, 200, 200));
        obstacles.add(new Rectangle(2100, 650, 200, 200));
        obstacles.add(new Rectangle(2400, 620, 200, 200));
        obstacles.add(new Rectangle(2700, 130, 200, 200));
        obstacles.add(new Rectangle(3100, 600, 200, 200));
        obstacles.add(new Rectangle(3500, 617, 200, 200));
        obstacles.add(new Rectangle(3900, 653, 200, 200));
        obstacles.add(new Rectangle(4300, 600, 200, 200));
        obstacles.add(new Rectangle(4700, 230, 200, 200));
        obstacles.add(new Rectangle(5000, 610, 200, 200));
        obstacles.add(new Rectangle(5400, 612, 200, 200));
        obstacles.add(new Rectangle(5700, 650, 200, 200));
        obstacles.add(new Rectangle(6300, 620, 200, 200));
        obstacles.add(new Rectangle(6500, 230, 200, 200));
        obstacles.add(new Rectangle(7300, 610, 200, 200));
        obstacles.add(new Rectangle(7700, 628, 200, 200));
        obstacles.add(new Rectangle(8100, 650, 200, 200));
        obstacles.add(new Rectangle(8400, 630, 200, 200));
        obstacles.add(new Rectangle(8700, 250, 200, 200));
        obstacles.add(new Rectangle(9100, 620, 200, 200));
        obstacles.add(new Rectangle(9500, 617, 200, 200));
        obstacles.add(new Rectangle(9900, 653, 200, 200));
        obstacles.add(new Rectangle(10300, 600, 200, 200));
        obstacles.add(new Rectangle(10700, 150, 200, 200));
        obstacles.add(new Rectangle(11000, 610, 200, 200));
        obstacles.add(new Rectangle(11400, 606, 200, 200));
        obstacles.add(new Rectangle(11700, 650, 200, 200));
        obstacles.add(new Rectangle(12000, 620, 200, 200));
        obstacles.add(new Rectangle(12200, 230, 200, 200));
        obstacles.add(new Rectangle(12300, 610, 200, 200));
        obstacles.add(new Rectangle(12700, 628, 200, 200));
        obstacles.add(new Rectangle(13100, 650, 200, 200));
        obstacles.add(new Rectangle(13400, 630, 200, 200));
        obstacles.add(new Rectangle(13700, 150, 200, 200));
        obstacles.add(new Rectangle(14100, 620, 200, 200));
        obstacles.add(new Rectangle(14500, 617, 200, 200));
        obstacles.add(new Rectangle(14900, 653, 200, 200));
        obstacles.add(new Rectangle(15300, 608, 200, 200));
        obstacles.add(new Rectangle(15700, 250, 200, 200));
        obstacles.add(new Rectangle(16000, 610, 200, 200));
        obstacles.add(new Rectangle(16400, 630, 200, 200));
        obstacles.add(new Rectangle(16700, 650, 200, 200));
        obstacles.add(new Rectangle(17000, 620, 200, 200));
        obstacles.add(new Rectangle(17200, 230, 200, 200));
        obstacles.add(new Rectangle(17400, 610, 200, 200));
        obstacles.add(new Rectangle(17700, 628, 200, 200));
        obstacles.add(new Rectangle(18100, 650, 200, 200));
        obstacles.add(new Rectangle(18400, 630, 200, 200));
        obstacles.add(new Rectangle(18700, 150, 200, 200));
        obstacles.add(new Rectangle(19100, 640, 200, 200));
        obstacles.add(new Rectangle(19500, 617, 200, 200));
        obstacles.add(new Rectangle(19900, 653, 200, 200));
        obstacles.add(new Rectangle(20700, 600, 200, 200));
        obstacles.add(new Rectangle(20900, 253, 200, 200));
        
        for (Rectangle enemy : enemies) {
            enemyVelocities.add(5); // 左右速度
            int minX = enemy.x - 100;
            int maxX = enemy.x + 80;
            enemyMoveRanges.add(new int[]{minX, maxX});

           
            // 敵ごとの基準Y座標（今の配置をそのまま groundY とする）
            enemyGroundY.add(enemy.y);
            enemyVY.add(0); // 初期の縦速度
        }
        
        for (int i = 0; i < enemies.size(); i++) {
            Rectangle enemy = enemies.get(i);

            // --- 左右移動 ---
            int vx = enemyVelocities.get(i);
            int[] range = enemyMoveRanges.get(i);
            enemy.x += vx;

            if (enemy.x < range[0] || enemy.x > range[1]) {
                enemyVelocities.set(i, -vx); // 反転
            }

            
            // --- ジャンプ処理 ---
            int vy = enemyVY.get(i);
            vy += 1; // 重力
            enemy.y += vy;

            // 地面に戻す
            if (enemy.y >= enemyGroundY.get(i)) {
                enemy.y = enemyGroundY.get(i);
                vy = 0;

                // ランダムでジャンプ開始
                if (Math.random() < 0.01) { // 1%の確率でジャンプ
                    vy = -12; // ジャンプ強さ
                }
            }

            enemyVY.set(i, vy);
            // enemyVelocitiesY.set(i, vy);
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obs = obstacles.get(i);

            // 2番目から始めて+5ごとに動かす (i=1,6,11,...)
            if (i % 5 == 1) {
            obstacleVelocities.add(1); // 上下にゆっくり動く
            int minY = obs.y - 30;  // 上に30ピクセルまで
            int maxY = obs.y + 30;  // 下に30ピクセルまで
            obstacleMoveRanges.add(new int[]{minY, maxY});
            } else {
                obstacleVelocities.add(0); // 動かない
                obstacleMoveRanges.add(new int[]{obs.y, obs.y});
            }
        }

        
    }
        
    // --- Flip image horizontally ---
    private Image flipImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage flipped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(img, width, 0, -width, height, null);
        g.dispose();
        return flipped.getScaledInstance(playerWidth, playerHeight, Image.SCALE_SMOOTH);
    }
    
    // --- Paint component ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        updatePlayerImageSet();
        drawProjectiles(g); // 他の描画
        // drawExplosions(g); // 爆発描画
        drawPlayer(g); // 上下キャラ切り替え用で使用
        drawRecoveryItems(g); // ← 回復アイテム描画
        
        // === タイトル画面 ===
        if (gameState == GameState.TITLE) {
            // 背景画像を画面サイズに合わせて描画
            g.drawImage(titleBackground, 0, 0, getWidth(), getHeight(), this);

            // ゲームタイトル
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Noraneko World — Learning Edition", getWidth() / 3 - 150, getHeight() / 3);

            // 点滅する文字
            blinkCounterTitle++;
            if (blinkCounterTitle % 30 == 0) blinkText = !blinkText;
            if (blinkText) {
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.drawString("Press SPACE to Start", getWidth() / 2 - 150, getHeight() / 2);
            }
                
            return; // タイトル画面はここで終了
        }
        
        if (gameState == GameState.CHARACTER_SELECT) {
            if (currentScene != null) currentScene.draw(g);
            return;
        }

        if (currentScene != null) {
            currentScene.draw(g); // 現在のシーンを描画
        }

        // === 背景（夜空グラデーション） ===
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 112), 0, getHeight()/1.5f, new Color(72, 61, 139));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 星
        g2.setColor(Color.YELLOW);
        Random starRand = new Random(12345);
        for (int i = 0; i < 100; i++) {
            int x = starRand.nextInt(getWidth());
            int y = starRand.nextInt(200);
            int size = starRand.nextInt(2) + 1;
            g2.fillOval(x, y, size, size);
        }

        // === 地面 ===
        g.setColor(new Color(124, 252, 0));
        g.fillRect(0, 660, getWidth(), 300);

        
        // === ゴール ===
        g.drawImage(goalImage, goalRect.x - cameraX, goalRect.y, goalRect.width, goalRect.height, this);

         // ブロック描画（カメラ補正）
        for (Block block : blocks) {
            block.draw(g, cameraX);
        }

        
        // 回復アイテム描画（横スクロールのみ対応）
        for (int i = 0; i < recoveryItems.size(); i++) {
            if (recoveryItemAlive.get(i)) {
                Rectangle r = recoveryItems.get(i);
                g.drawImage(recoveryItemImages.get(i),
                r.x - cameraX, r.y, r.width, r.height, null);
            }
        }
        
        for (int i = 0; i < recoveryItems.size(); i++) {
            if (!recoveryItemAlive.get(i)) continue; // 取ったアイテムは描画しない
        }

        // === プレイヤー ===
        Image playerImg;
        if (playerY < 600) {
            // ジャンプ中
            playerImg = playerJump; // ← jump用画像を用意
        } else if (leftPressed || rightPressed) {
            runFrame = (frameCount / 10) % 2;
            playerImg = facingRight ? playerRun[runFrame] : playerLeft;
        } else {
            playerImg = facingRight ? playerIdle : playerLeft;
        }

        if (leftPressed || rightPressed) {
         int runFrame = (frameCount / 10) % 2;
            playerImg = facingRight ? currentCharacter.run[runFrame] : currentCharacter.left;
            } else {
            playerImg = facingRight ? currentCharacter.idle : currentCharacter.left;
            }
            g.drawImage(playerImg, playerX - cameraX, playerY, this);
            frameCount++;

        // === 敵 ===
        for (int i = 0; i < enemies.size(); i++) {
            if (!enemyAlive.get(i)) continue;
            Rectangle enemy = enemies.get(i);
            Image img = enemyImages.get(i % enemyImages.size());
            g.drawImage(img, enemy.x - cameraX, enemy.y, this);
        }

        // === 敵の弾 ===
        for (Projectile p : enemyProjectiles) {
            g.setColor(Color.ORANGE);
            g.fillOval((int)p.x - cameraX, (int)p.y, 15, 15);
        }

        // === 障害物 ===
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obs = obstacles.get(i);
            Image obsImg = obstacleImages.get(i % obstacleImages.size());
            g.drawImage(obsImg, obs.x - cameraX, obs.y, obs.width, obs.height, this);
        }

        if(boss != null && boss.isAlive()) {
            boss.draw(g, cameraX);
        }

        if(midBoss != null && midBoss.isAlive()) {
            midBoss.draw(g, cameraX);
        }
        
        if (midBoss2 != null && midBoss2.isAlive()) {
            midBoss2.draw(g, cameraX);
        }

        if (midBoss3 != null && midBoss3.isAlive()) {
            midBoss3.draw(g, cameraX);
        }

        // === スコア・時間 ===
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Time: " + elapsed + "s", 20, 40);
        g.drawString("Score: " + score, 20, 20);

        // === ゲームオーバー／クリア表示 ===
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.RED);
            g.drawString("GAME OVER", getWidth() / 2 - 120, getHeight() / 2);
        }
        if (gameClear) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.CYAN);
            g.drawString("GOAL!", getWidth() / 2 - 70, getHeight() / 2);
        }
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", getWidth() / 2 - 80, getHeight() / 2);
        }

        // HPバー描画
        g.setColor(Color.PINK);
        for (int i = 0; i < playerHP; i++) {
            // g.fillRect(20 + i * 30, 20, 25, 25); // 赤い四角でHP表示
            g.drawImage(heartImage, 20 + (i * 40), 50, 32, 32, this);
        }

        if (invincible) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            for (int i = 0; i < playerHP; i++) {
                g2d.drawImage(heartImage, 20 + (i * 40), 50, 40, 40, this);
            }
            g2d.dispose();
            } else {
            for (int i = 0; i < playerHP; i++) {
                g.drawImage(heartImage, 20 + (i * 40), 50, 40, 40, this);
            }
        }

        for (PlayerProjectile p : playerProjectiles) {
            p.draw(g, cameraX, playerProjectileImage);
        }
    
        
        // --- ゲームクリア半透明ポップアップ ---
        if (gameCleared) {
            blinkCounterClear++;
            if (blinkCounterClear % 30 == 0) blinkClearText = !blinkClearText;
            drawClearPopup(g);  // 最後に描画
        }
    }



    // ← ここに drawClearPopup を追加
    private void drawClearPopup(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int popupWidth = 600;
        int popupHeight = 300;
        int popupX = (getWidth() - popupWidth) / 2;
        int popupY = (getHeight() - popupHeight) / 2;

        // 半透明背景
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(popupX, popupY, popupWidth, popupHeight, 20, 20);

        // 枠線
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(popupX, popupY, popupWidth, popupHeight, 20, 20);

        // GAME CLEAR! 文字（点滅）
        if (blinkClearText) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 48));
            g2.setColor(Color.PINK);
            String clearText = "GAME CLEAR!";
            int textWidth = g2.getFontMetrics().stringWidth(clearText);
            g2.drawString(clearText, popupX + (popupWidth - textWidth) / 2, popupY + 80);
        }

        // SCORE 常時表示
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2.setColor(Color.PINK);
        String scoreText = "SCORE: " + score;
        int scoreWidth = g2.getFontMetrics().stringWidth(scoreText);
        g2.drawString(scoreText, popupX + (popupWidth - scoreWidth) / 2, popupY + 160);

        // 操作案内
        g2.setFont(new Font("SansSerif", Font.PLAIN, 24));
        String infoText = "Press Enter to Restart / Esc to Quit";
        int infoWidth = g2.getFontMetrics().stringWidth(infoText);
        g2.drawString(infoText, popupX + (popupWidth - infoWidth) / 2, popupY + 240);
    }
    
    private void drawProjectiles(Graphics g) {
        g.setColor(Color.YELLOW);
        for (PlayerProjectile p : playerProjectiles) {
            g.fillRect((int)p.x - cameraX, (int)p.y - cameraY, 8, 8);
        }
    }
    
    
    private int cameraY = 0; // ← これを追加

    private ArrayList<PlayerProjectile> playerProjectiles = new ArrayList<>();
    
    private static final int WORLD_WIDTH = 23000;
    private static final int WORLD_HEIGHT = 2000; // ← 縦の高さ（新しく追加）

    private void generateRandomBlocks(int numBlocks, int mapWidth, int maxY, Image blockImage) {
        Random rand = new Random();

        int width = 80;
        int height = 80;

        for (int i = 0; i < numBlocks; i++) {
            int x = rand.nextInt(mapWidth);  // 0～mapWidth の範囲
            int y = rand.nextInt(maxY + 1); // 0～maxY の範囲

            Block newBlock = new Block(x, y, width, height, blockImage);
            blocks.add(newBlock);
        }

        System.out.println("生成ブロック数: " + blocks.size());

    }

    // --- Game update loop ---
    @Override
    public void actionPerformed(ActionEvent e) {

        updatePlayerImageSet();
        updateProjectiles();
        checkProjectileCollisions(); // ←ここで当たり判定を実行
        
        checkPlayerProjectileCollisions();  // プレイヤー弾と敵
        
        if (currentScene != null) {
            currentScene.update(); // 現在のシーンを更新
        }

        // --- 気弾発射（Downキー：押された瞬間だけ1発） ---
        if (attackPressed && !prevAttackPressed) {
            int speed = facingRight ? 6 : -6; 
            int startX = facingRight ? playerX + playerWidth : playerX - 16;
            int startY = playerY + playerHeight / 2;

            playerProjectiles.add(new PlayerProjectile(startX, startY, speed));
            playSE(currentCharacter.voiceAttack); // キャラごとの攻撃ボイス
        }

        // 状態を更新（次フレームの比較用）
        prevAttackPressed = attackPressed;

        if (attackPressed) {

    
            // プレイヤーの攻撃範囲を作成（プレイヤーの向きに応じて前方に矩形を設定）
            int attackRangeWidth = 40;
            int attackRangeHeight = playerHeight;
            int attackRangeX = facingRight ? playerX + playerWidth : playerX - attackRangeWidth;
            int attackRangeY = playerY;
            Rectangle attackRect = new Rectangle(attackRangeX, attackRangeY, attackRangeWidth, attackRangeHeight);
                
            Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
            for (int i = 0; i < enemies.size(); i++) {
                Rectangle enemyRect = enemies.get(i).getBounds();
                Rectangle enemyTop = new Rectangle(enemyRect.x, enemyRect.y, enemyRect.width, 20); // 敵の上面

                if (playerRect.intersects(enemyRect)) {
                    if (playerRect.intersects(enemyTop) && velocityY > 0) {
                        // 敵を踏んだ場合
                        enemies.remove(i);
                        velocityY = -10; // 跳ねる
                        score += 300;
                        i--;
                    } else {
                        damagePlayer(1); // HPを1減らす
                        
                    }
                }
                //  下方向の判定を広げる例（踏み判定用の矩形を作る）
                int stompHeight = 50; // 何ピクセル分広げるか
                Rectangle stompRect = new Rectangle(playerX, playerY + playerHeight - stompHeight, playerWidth, stompHeight);

                if (stompRect.intersects(enemyRect) && velocityY > 0) { // velocityY>0で落下中だけ判定
                    // 敵を倒す
                    velocityY = -15;
                }
            }

            for (PlayerProjectile p : playerProjectiles) {
                p.update();
                if (p.x < -50 || p.x > mapWidth) {
                p.alive = false;
                }
            }

            
        }

        if (gameOver || gameClear) {
                repaint();
            return;
        }
        
        if (gameOver) {
            playGameOverBGM();
        }

        // --- 移動処理（キャラごとのスピード） ---
        if (leftPressed) {
            playerX -= currentCharacter.moveSpeed;  // キャラごと
            facingRight = false;
        }
        if (rightPressed) {
            playerX += currentCharacter.moveSpeed;
            facingRight = true;
        }

        cameraX = playerX - 100;
        cameraX = playerX - SCREEN_WIDTH / 4;
        cameraY = playerY - SCREEN_HEIGHT / 2;

        // 範囲チェック（ワールド外に出さない）
        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (cameraX > WORLD_WIDTH - SCREEN_WIDTH) cameraX = WORLD_WIDTH - SCREEN_WIDTH;
        if (cameraY > WORLD_HEIGHT - SCREEN_HEIGHT) cameraY = WORLD_HEIGHT - SCREEN_HEIGHT;


        // Gravity & jump
        if (!onGround) {
            velocityY += GRAVITY;
            playerY += velocityY;
        }


        // --- ブロックとの接地判定 ---
        onGround = false;  // 一旦 false にリセット

        for (Block block : blocks) {
            Rectangle blockRect = new Rectangle(block.getX(), block.getY(), block.getWidth(), block.getHeight());
            Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight); // 一度だけ
            // 足がブロックに重なっているか判定
            if (playerRect.intersects(blockRect)) {
                int playerBottom = playerY + playerHeight;
                int blockTop = block.getY();
                if (playerBottom >= blockTop && playerBottom <= blockTop + 20 && velocityY >= 0) {
                    // ブロックの上に乗せる
                    playerY = blockTop - playerHeight;
                    velocityY = 0;
                    onGround = true;
                    jumpCount = 0;
                }
            }
        }

        if (playerY >= 600) {
            playerY = 600;
            velocityY = 0;
            onGround = true;
            jumpCount = 0; // ←ここで必ずリセット
        }
        
        if (gameState == GameState.TITLE) {
                repaint();
                return; // タイトル時はゲーム処理しない
            }

            // ge-mu 
            if (gameState == GameState.PLAYING) {
            // ゲームロジック更新
            updatePlayer();
            updateEnemies();
            // updateRideBlocks();
            updateProjectiles();
            // updateExplosions();
        }

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        
        repaint();

        // 敵の移動処理（横移動＋ジャンプ）
        for (int i = 0; i < enemies.size(); i++) {
            if (!enemyAlive.get(i)) continue;
            Rectangle enemy = enemies.get(i);

            // --- 横移動 ---
            int vel = enemyVelocities.get(i);
            int[] range = enemyMoveRanges.get(i);

            enemy.x += vel;
            if (enemy.x < range[0] || enemy.x > range[1]) {
                vel = -vel;
                enemyVelocities.set(i, vel);
                enemy.x = Math.max(range[0], Math.min(range[1], enemy.x));
            }

            // --- 縦ジャンプ処理 ---
            int vy = enemyVY.get(i);
            vy += 1; // 重力加算
            enemy.y += vy;

            // 基準Yに戻ったら着地
            if (enemy.y >= enemyGroundY.get(i)) {
                enemy.y = enemyGroundY.get(i);
                vy = 0;

                // ランダムにジャンプ開始
                if (Math.random() < 0.01) { // 1%くらいの確率でジャンプ
                    vy = -12; // ジャンプ強さ
                }
            }

            enemyVY.set(i, vy);
        }

        // Enemy collision check
        for (int i = 0; i < enemies.size(); i++) {
            Rectangle enemy = enemies.get(i);
            if (!enemyAlive.get(i)) continue;

            if (playerRect.intersects(enemy)) {
                int playerBottom = playerY + playerHeight;
                int enemyTop = enemy.y;

                if (playerBottom <= enemyTop + 10 && velocityY >= 0) {
                    // 敵を踏んだ
                    enemyAlive.set(i, false);
                    score += 300;
                    velocityY = JUMP_STRENGTH / 2; // バウンドジャンプ
                    } else {
                    damagePlayer(1);
                }
            }
            
        }
        

        for (int i = 0; i < recoveryItems.size(); i++) {
            if (!recoveryItemAlive.get(i)) continue; // もう取ったアイテムは無視

            Rectangle itemRect = recoveryItems.get(i);
            if (playerRect.intersects(itemRect)) {
                // アイテムを取った
                recoveryItemAlive.set(i, false); // 画面から消す

                // プレイヤーHP回復
                playerHP = Math.min(playerHP + 1, MAX_PLAYER_HP);

                score += 777; // 好きな値で
                System.out.println("Score: " + score);

                // 取得音を鳴らす（itemGetClipがある場合）
                if (itemGetClip != null) {
                playSE(itemGetClip);
                }
            }
        }

        // 障害物の垂直移動処理（上下に浮遊）
        for (int i = 0; i < obstacles.size(); i++) {
                int vel = obstacleVelocities.get(i);
                if (vel == 0) continue; // 動かない障害物はスキップ

                Rectangle obs = obstacles.get(i);
                int[] range = obstacleMoveRanges.get(i);

            obs.y += vel;

            if (obs.y < range[0] || obs.y > range[1]) {
                    vel = -vel;
                    obstacleVelocities.set(i, vel);
                obs.y = Math.max(range[0], Math.min(range[1], obs.y));
            }
        }

        // Enemy attacks handling
        if (enemyAttackCooldown > 0) {
            enemyAttackCooldown--;
        } 
        else {
            // 10% chance to shoot projectile
            if (random.nextInt(100) < 10) {
                    if (!enemies.isEmpty()) {
                        Rectangle enemy = enemies.get(random.nextInt(enemies.size()));
                        if (enemyAlive.get(enemies.indexOf(enemy))) {
                        enemyShoot(enemy.x, enemy.y);
                        enemyAttackCooldown = 60;
                    }
                }
            }
        }

        // Update enemy projectiles
        for (int i = 0; i < enemyProjectiles.size(); i++) {
            Projectile p = enemyProjectiles.get(i);
            p.update();
            // Remove if off-screen
            if (p.y < 0 || p.y > getHeight()) {
                enemyProjectiles.remove(i--);
                continue;
            }
            // Check collision with player
            Rectangle projRect = new Rectangle((int)p.x, (int)p.y, 5, 5);
            // if (projRect.intersects(playerRect)) {
            //     gameOver = true;
            // }
            if (projRect.intersects(playerRect)) {
                damagePlayer(1); // HPを1減らす
                enemyProjectiles.remove(i--); // 当たった弾を消す
                continue;
            }
        }
        

        
        // 中ボスの処理
        if (gameState == GameState.PLAYING && midBoss != null && midBoss.isAlive()) {
            midBoss.update(playerX, playerY);

            // プレイヤー弾との当たり判定
            Rectangle midBossRect = midBoss.getHitBox();
            for (PlayerProjectile p : playerProjectiles) {
                Rectangle projRect = new Rectangle(p.x, p.y, p.width, p.height);
                if (projRect.intersects(midBossRect)) {
                    midBoss.takeDamage(5);
                    p.alive = false;
                    score += 50;

                    // 撃破ボーナス
                    if (!midBoss.isAlive() && !midBoss.isCounted) {
                        score += 5000;
                        midBoss.isCounted = true;
                    }
                }
            }

            // プレイヤー接触ダメージ
            if (playerRect.intersects(midBossRect)) {
                damagePlayer(1);
            }
        }

        // --- 中ボス2の更新 ---
        if (midBoss2 != null && midBoss2.isAlive()) {
            midBoss2.update(playerX, playerY);

            // プレイヤー弾との当たり判定
            Rectangle bossRect = midBoss2.getHitBox();
            for (PlayerProjectile p : playerProjectiles) {
                Rectangle projRect = new Rectangle(p.x, p.y, p.width, p.height);
                    if (projRect.intersects(bossRect)) {
                        midBoss2.takeDamage(5);
                        p.alive = false;
                        score += 70;

                    // 撃破ボーナス
                    if (!midBoss2.isAlive() && !midBoss2.isCounted) {
                        score += 12000;
                        midBoss.isCounted = true;
                    }
                }
            }

            // プレイヤー接触ダメージ
            if (playerRect.intersects(bossRect)) {
                damagePlayer(1);
            }
        }

        // --- 中ボス3の処理 ---
        if (gameState == GameState.PLAYING && midBoss3 != null && midBoss3.isAlive()) {
            midBoss3.update(playerX, playerY);

            // プレイヤー弾との当たり判定
            Rectangle midBoss3Rect = midBoss3.getHitBox();
            for (PlayerProjectile p : playerProjectiles) {
                Rectangle projRect = new Rectangle(p.x, p.y, p.width, p.height);
                if (projRect.intersects(midBoss3Rect)) {
                    midBoss3.takeDamage(5);
                    p.alive = false;

                    // スコア加算（ヒットごと）
                    score += 250;

                    // 撃破時ボーナス
                    if (!midBoss3.isAlive() && !midBoss3.isCounted) {
                        score += 33300;
                        midBoss3.isCounted = true;
                    }
                }
            }

            // プレイヤー接触ダメージ
            if (playerRect.intersects(midBoss3Rect)) {
                damagePlayer(1);
            }
        }

        // --- ボス処理追加 ---
        if (gameState == GameState.PLAYING && boss != null && boss.isAlive()) {
            boss.update(playerX, playerY);

            // プレイヤー弾との当たり判定
            Rectangle bossRect = boss.getHitBox();
            for (PlayerProjectile p : playerProjectiles) {
                Rectangle projRect = new Rectangle(p.x, p.y, p.width, p.height);
                if (projRect.intersects(bossRect)) {
                    boss.takeDamage(5);
                    p.alive = false;
                    score += 100;
                    // 2. 撃破時ボーナス（1回だけ）
                    if (!boss.isAlive() && !boss.isCounted) {
                        score += 25000;
                        boss.isCounted = true;
                    }
                }
            }

            // プレイヤー接触ダメージ
            if (playerRect.intersects(bossRect)) {
                damagePlayer(1);
            }
        
        }

        repaint();

       
        //Check goal collision
        if (playerRect.intersects(goalRect)) {
            gameCleared = true;
            score += 6666;
            gameClear = true;
        }
        // ゴール判定

        if (gameClear) {
            playGameClearBGM();
            repaint();
            return;
        }  
        
    }
    
    // --- Enemy shoots a projectile ---
    private void enemyShoot(int enemyX, int enemyY) {
        double px = playerX + playerWidth / 2;
        double py = playerY + playerHeight / 2;

        double ex = enemyX + 40; // 敵の中心付近
        double ey = enemyY + 40;

        double dx = px - ex;
        double dy = py - ey;
        double dist = Math.sqrt(dx * dx + dy * dy);

        double speed = 15.0;
        double vx = speed * dx / dist;
        double vy = speed * dy / dist;

        enemyProjectiles.add(new Projectile(ex, ey, vx, vy));
    }
        
    // --- Projectile class ---
    class Projectile {
        double x, y;
        double vx, vy;  // 速度のX,Y成分

        Projectile(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }

        void update() {
            x += vx;
            y += vy;
            
            for (int i = 0; i < playerProjectiles.size(); i++) {
                PlayerProjectile p = playerProjectiles.get(i);
                p.update();
                if (!p.alive) {
                playerProjectiles.remove(i);
                i--;
                }
            }
        }
    }

    

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_UP) attackPressed = true;
        if (key == KeyEvent.VK_DOWN) attackPressed = true; // ← Downキー

        // タイトル画面
        if (gameState == GameState.TITLE) {
            if (key == KeyEvent.VK_SPACE) {
                gameState = GameState.CHARACTER_SELECT;
                startTime = System.currentTimeMillis();
                stopAllBGM();
                playPlayBGM();
            }
            return;
        }
        
        
        if (gameState == GameState.TITLE && e.getKeyCode() == KeyEvent.VK_SPACE) {
                gameState = GameState.CHARACTER_SELECT;
        }

        // キャラ選択画面で左右操作
        if (gameState == GameState.CHARACTER_SELECT) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) ((CharacterSelectScene)currentScene).moveLeft();
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) ((CharacterSelectScene)currentScene).moveRight();
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                
                int index = ((CharacterSelectScene)currentScene).getSelectedIndex();
                currentCharacter = characters.get(index);
                gameState = GameState.PLAYING;
                stopAllBGM();
                playPlayBGM();

            }
        }

        

        // ゲーム中
        if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT) rightPressed = true;

            
            if (key == KeyEvent.VK_SPACE) { // ジャンプ
                if (onGround || jumpCount < currentCharacter.maxJump) {
                    velocityY = -currentCharacter.jumpPower; // マイナスにして上方向に
                    onGround = false;
                    jumpCount++;
                    playSE(currentCharacter.voiceJump);      // キャラごとのジャンプ音
                }
            }

            
            if (key == KeyEvent.VK_UP) { // 攻撃
                if (attackCooldown == 0) {
                    attackPressed = true;
                    attackCooldown = ATTACK_COOLDOWN_TIME;
                    playSE(currentCharacter.voiceAttack); // キャラごとの攻撃音
                }
            }

            if (key == KeyEvent.VK_DOWN) { // 気弾
                
                if (!attackPressed) {             // まだ押されてない状態なら
                    attackJustPressed = true;     // 押した瞬間フラグを立てる
                }
                attackPressed = true;             // 押されている状態
                playSE(currentCharacter.voiceAttack);
            }
        }

               
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
            attackPressed = true;
            break;
            // 他のキー処理（←↑→ など）
        }
    
        // ゲームオーバー・クリア時リスタート
        if (key == KeyEvent.VK_R && (gameOver || gameClear)) {
            restartGame();
            stopAllBGM();
            playPlayBGM();
         }
         if (gameCleared) {
        if (key == KeyEvent.VK_ENTER) {
            restartGame(); // リスタート
        } else if (key == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        return; // ゲームクリア中は通常操作を無効化
    }

    // --- クリア画面でEnterキーを押したらタイトルへ戻る ---
    if (gameCleared && key == KeyEvent.VK_ENTER) {
        gameCleared = false;     // クリア状態解除
        stopAllBGM();            // 再生中のBGMを止める
        playTitleBGM();          // タイトルBGM再生
        gameState = GameState.TITLE; // ← ここでタイトル画面に戻す
        repaint();
        return;
    }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_UP) attackPressed = false;
        if (key == KeyEvent.VK_DOWN) attackPressed = false; // ← Downキー

        
        if (key == KeyEvent.VK_DOWN) {
            attackPressed = false;            // 離された
        }
        
        if (attackPressed) {
            playerProjectiles.add(new PlayerProjectile(
            playerX + 40, // プレイヤー右端あたりから
            playerY + 20, // プレイヤー中央あたりから
            10            // 速度（右方向）
            ));
        }

        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) jumpPressed = false;
        if (key == KeyEvent.VK_UP) {
            attackPressed = false;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
            attackPressed = false;
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    
    // --- Restart game ---
    private void restartGame() {
        
        loadImages();  // HPアイコン再読み込み

        playerX = 100;
        playerY = 600;
        velocityY = 0;
        onGround = true;
        score = 0;
        gameOver = false;
        gameClear = false;
        gameCleared = false;          // ← クリア画面フラグリセット
        blinkCounterClear = 0;        // ← 点滅用カウンタリセット
        blinkClearText = true;        // ← 点滅用フラグ初期化
        startTime = System.currentTimeMillis();
        playerHP = MAX_PLAYER_HP;
        invincible = false;
        invincibleTimer = 0;

        for (int i = 0; i < enemyAlive.size(); i++) {
            enemyAlive.set(i, true);
        }
        enemyProjectiles.clear();
        playerProjectiles.clear();    // ← プレイヤー弾もリセット

        // アイテム復活
        for (int i = 0; i < recoveryItemAlive.size(); i++) {
            recoveryItemAlive.set(i, true);
        }
   
        // ゲーム状態をPLAYINGに戻す
        gameState = GameState.PLAYING;
    }





    // --- BGM関連
    private void loadBGM() {
        titleBGM = loadClip("/sounds/title_bgm.wav");
        playBGM = loadClip("/sounds/play_bgm.wav");
        gameOverBGM = loadClip("/sounds/gameover_bgm.wav");
        gameClearBGM = loadClip("/sounds/gameClear_bgm.wav");
    }

    private void playTitleBGM() {
        stopAllBGM();
        if (titleBGM != null) {
            titleBGM.setFramePosition(0);
            titleBGM.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void playPlayBGM() {
        stopAllBGM();
        if (playBGM != null) {
            playBGM.setFramePosition(0);
            playBGM.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void playGameOverBGM() {
        stopAllBGM();
        if (gameOverBGM != null) {
            gameOverBGM.setFramePosition(0);
            gameOverBGM.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void playGameClearBGM() {
        // プレイBGMを止める
        if (playBGM != null && playBGM.isRunning()) {
            playBGM.stop();
        }
        
        if (gameClearBGM != null) {
            gameClearBGM.stop();
            gameClearBGM.setFramePosition(0); // 最初から再生
            gameClearBGM.start();
        }
    }

    private void stopAllBGM() {
        if (titleBGM != null && titleBGM.isRunning()) titleBGM.stop();
        if (playBGM != null && playBGM.isRunning()) playBGM.stop();
        if (gameOverBGM != null && gameOverBGM.isRunning()) gameOverBGM.stop();
        if (gameClearBGM != null && gameClearBGM.isRunning()) gameClearBGM.stop();
    }

    // 例えばゲーム開始時に呼ぶメソッド
    private void startGame() {
        gameState = GameState.PLAYING;
        playerHP = 3;       // 最大HPに戻す
        invincible = false; // 無敵解除

        score = 0;
        playerX = 100;
        playerY = 600;
        velocityY = 0;
        onGround = true;

        playPlayBGM();
        // 他の初期化処理...
    }

    // フィールド
    ArrayList<Integer> enemyGroundY = new ArrayList<>();
    ArrayList<Integer> enemyVY = new ArrayList<>();

    private void shootProjectile() {
        int projX = playerX + (facingRight ? playerWidth : -20);
        int projY = playerY + playerHeight / 2;

        PlayerProjectile proj = new PlayerProjectile(projX, projY, facingRight ? 6 : -6);
        playerProjectiles.add(proj);

        // 攻撃ボイス再生（キャラごと）
        playSE(currentCharacter.voiceAttack);

        // 発射音（共通SE）
        playSE(shootClip);
    }

    int mapWidth = 22000;

    private void loadCharacters() {
    try {
        // キャラごとのパラメータ（例）
        int[] speeds    = { 13, 6, 11, 12, 8, 17, 10, 15 };   // moveSpeed
        int[] jumps     = {20,20,22,21,24,24,20,22 };   // jumpPower
        int[] maxJumps  = { 4, 3, 4, 4, 3, 2, 3, 5 };   // maxJump

        for (int i = 1; i <= 8; i++) {
            // 画像
            java.awt.image.BufferedImage idleImg = javax.imageio.ImageIO.read(
                java.util.Objects.requireNonNull(
                    getClass().getResource("/images/player_idle" + i + ".png")
                )
            );
            java.awt.image.BufferedImage run1 = javax.imageio.ImageIO.read(
                java.util.Objects.requireNonNull(
                    getClass().getResource("/images/player_run" + (i*2-1) + ".png")
                )
            );
            java.awt.image.BufferedImage run2 = javax.imageio.ImageIO.read(
                java.util.Objects.requireNonNull(
                    getClass().getResource("/images/player_run" + (i*2) + ".png")
                )
            );

            // スケール＆左右反転
            java.awt.Image idleScaled = idleImg.getScaledInstance(playerWidth, playerHeight, java.awt.Image.SCALE_SMOOTH);
            java.awt.Image leftImg = flipImage(idleImg); // 既存のflipImage(BufferedImage)を利用
            java.awt.Image[] run = new java.awt.Image[] {
                run1.getScaledInstance(playerWidth, playerHeight, java.awt.Image.SCALE_SMOOTH),
                run2.getScaledInstance(playerWidth, playerHeight, java.awt.Image.SCALE_SMOOTH)
            };

            // ボイス（あるものだけでOK。無ければ null のまま）
            javax.sound.sampled.Clip voiceJump   = loadClip("/sounds/voice_jump" + i + ".wav");
            javax.sound.sampled.Clip voiceAttack = loadClip("/sounds/voice_attack" + i + ".wav");

            // CharacterData 生成
            CharacterData character = new CharacterData(
                idleScaled,
                leftImg,
                run,                 // ← ★ここにカンマが必要！
                speeds[i-1],
                jumps[i-1],
                maxJumps[i-1],
                voiceJump,
                voiceAttack
            );
            characters.add(character);
        }

        // デフォルトは1番目
        currentCharacter = characters.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean bossBGMStarted = false;  // ボスBGMが再生済みかどうか

    private void loadBoss() {
        try {
            // 移動アニメーション
            Image[] moveSprites = new Image[2];
            moveSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/boss_move1.png")));
            moveSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/boss_move2.png")));

            // 攻撃アニメーション
            Image[] attackSprites = new Image[2];
            attackSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/boss_attack1.png")));
            attackSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/boss_attack2.png")));

            // ボイス・攻撃音・BGM
            Clip voiceClip = loadClip("/sounds/boss_voice.wav");
            Clip attackClip = loadClip("/sounds/boss_attack.wav");
            Clip bgmClip = loadClip("/sounds/boss_bgm.wav");

            // ボス生成
            boss = new BossEnemy(
                20000,    // 出現X
                400,      // 出現Y
                1000,      // HP
                moveSprites,
                attackSprites,
                voiceClip,
                attackClip,
                bgmClip
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void loadMidBoss() {
        try {
            // 移動アニメーション
            Image[] moveSprites = new Image[2];
            moveSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss_move1.png")));
            moveSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss_move2.png")));

            // 攻撃アニメーション
            Image[] attackSprites = new Image[2];
            attackSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss_attack1.png")));
            attackSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss_attack2.png")));

            // ボイス・攻撃音・BGM
            Clip voiceClip = loadClip("/sounds/midboss_voice.wav");
            Clip attackClip = loadClip("/sounds/midboss_attack.wav");
            Clip bgmClip = loadClip("/sounds/midboss_bgm.wav"); // 任意

            // 中ボス生成
            midBoss = new BossEnemy(
                15000,   // 出現X座標（マップ上の位置）
                400,     // 出現Y座標
                600,     // HP
                moveSprites,
                attackSprites,
                voiceClip,
                attackClip,
                bgmClip
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMidBoss2() {
        try {
            // 移動アニメーション
            Image[] moveSprites = new Image[2];
            moveSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss2_move1.png")));
            moveSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss2_move2.png")));

            // 攻撃アニメーション
            Image[] attackSprites = new Image[2];
            attackSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss2_attack1.png")));
            attackSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss2_attack2.png")));

            // ボイス・攻撃音・BGM
            Clip voiceClip = loadClip("/sounds/midboss2_voice.wav");
            Clip attackClip = loadClip("/sounds/midboss2_attack.wav");
            Clip bgmClip   = loadClip("/sounds/midboss2_bgm.wav"); // 任意

            // 2体目の中ボス生成
            midBoss2 = new BossEnemy(
                10000,   // 出現X座標（マップに合わせて調整）
                400,    // 出現Y座標
                500,    // HP
                moveSprites,
                attackSprites,
                voiceClip,
                attackClip,
                bgmClip
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMidBoss3() {
        try {
            // 移動アニメーション
            Image[] moveSprites = new Image[2];
            moveSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss3_move1.png")));
            moveSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss3_move2.png")));

            // 攻撃アニメーション
            Image[] attackSprites = new Image[2];
            attackSprites[0] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss3_attack1.png")));
            attackSprites[1] = ImageIO.read(Objects.requireNonNull(getClass().getResource("/images/midboss3_attack2.png")));

            // ボイス・攻撃音・BGM
            Clip voiceClip = loadClip("/sounds/midboss3_voice.wav");
            Clip attackClip = loadClip("/sounds/midboss3_attack.wav");
            Clip bgmClip   = loadClip("/sounds/midboss3_bgm.wav"); // 任意

            // 中ボス3生成
            midBoss3 = new BossEnemy(
                23000,   // 出現X座標（マップ位置調整してね）
                400,    // 出現Y座標
                1500,    // HP
                moveSprites,
                attackSprites,
                voiceClip,
                attackClip,
                bgmClip
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clip読み込み用
    private Clip loadClip(String path) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                Objects.requireNonNull(getClass().getResource(path))
            );
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean prevSpaceKey = false;  // 前フレームのスペースキー状態

    
}
    