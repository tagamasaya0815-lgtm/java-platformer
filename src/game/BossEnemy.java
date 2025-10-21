package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

public class BossEnemy {
    private double posX, posY; // 内部座標を小数で保持
    private double vx, vy;     // 速度ベクトル
    private double speed = 2.0; // ボスの移動速度
    
    int x, y, hp, maxHp;
    boolean onGround;
    double yVelocity;
    int attackCooldown;
    int frameCount;
    int phase;
    Random rand = new Random();

    Image[] moveSprites, attackSprites;
    ArrayList<Projectile> projectiles = new ArrayList<>();
    ArrayList<AttackEffect> effects = new ArrayList<>();
    Clip attackSound, voiceSound, bgm;

   
    private boolean alive = true;
    public boolean isCounted = false; // 撃破ボーナス済みかどうか

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public BossEnemy(int startX, int startY, int hp, Image[] moveSprites, Image[] attackSprites,
    Clip voice, Clip attackSE, Clip bossBGM) {

        this.posX = startX;
        this.posY = startY;
        this.x = startX;
        this.y = startY;

        this.hp = hp;
        this.maxHp = hp;
        this.onGround = true;
        this.yVelocity = 0;
        this.attackCooldown = 0;
        this.frameCount = 0;
        this.phase = 1;
        this.moveSprites = moveSprites;
        this.attackSprites = attackSprites;
        this.voiceSound = voice;
        this.attackSound = attackSE;
        this.bgm = bossBGM;

        
    }

    public void update(int playerX, int playerY) {
        frameCount++;
        applyGravity();
        move(playerX, playerY);
        attack(playerX, playerY);
        updateProjectiles(playerX, playerY);
        updateEffects();
        changePhase();
    }

    private void applyGravity() {
        if(!onGround){ yVelocity+=0.5; y+=yVelocity; if(y>=400){y=400;yVelocity=0;onGround=true;} }
    }

    private void move(int targetX, int targetY) {
        double dx = targetX - posX;
        double dy = targetY - posY;
        double dist = Math.sqrt(dx*dx + dy*dy);

        if (dist > 1) { // 一定距離以上だけ移動
            vx = speed * dx / dist;
            vy = speed * dy / dist;
            posX += vx;
            posY += vy;
        }

        // 整数座標に反映
        x = (int) posX;
        y = (int) posY;
    }

    private void attack(int playerX, int playerY){
        attackCooldown++;
        int cooldownThreshold = phase==1?60:30;
        if(attackCooldown>cooldownThreshold){
            int choice;
            if(phase==1){
                choice=rand.nextInt(3);
                if(choice==0) meleeAttack(playerX, playerY);
                else projectileAttackSingle();
            } else {
                choice=rand.nextInt(5);
                switch(choice){
                    case 0: meleeAttack(playerX,playerY); break;
                    case 1: projectileAttackMulti(); break;
                    case 2: projectileAttackDiagonal(); break;
                    case 3: homingAttack(playerX,playerY); break;
                    case 4: jumpAttack(playerX,playerY); break;
                }
            }
            attackCooldown=0;
        }
    }

    private void meleeAttack(int px,int py){ 
        effects.add(new AttackEffect(x,y)); 
        if(attackSound!=null) attackSound.start(); 
    }

    private void projectileAttackSingle(){ 
        projectiles.add(new Projectile(x + getHitBox().width / 2,
        y + getHitBox().height / 2,
        5, -3 + rand.nextInt(6))); if(attackSound!=null) attackSound.start(); 
    }

    private void projectileAttackMulti(){ 
        for(int i=0;i<3;i++) projectiles.add(new Projectile(x,y,7,-3+i*2)); 
        if(attackSound!=null) attackSound.start(); 
    }

    private void projectileAttackDiagonal(){ 
        projectiles.add(new Projectile(x,y,5,-5)); 
        projectiles.add(new Projectile(x,y,5,5)); 
        if(attackSound!=null) attackSound.start(); 
    }

    private void homingAttack(int px,int py){ 
        projectiles.add(new HomingProjectile(x,y,px,py,6)); 
        if(attackSound!=null) attackSound.start(); 
    }

    private void jumpAttack(int px,int py){ 
        if(onGround)yVelocity=-12; 
        projectiles.add(new Projectile(x,y,7,-5)); 
        if(attackSound!=null) attackSound.start(); 
    }

    private void updateProjectiles(int playerX,int playerY){
        ArrayList<Projectile> alive=new ArrayList<>();
        for(Projectile p:projectiles){
            p.update();
            if(p.isAlive()){
                alive.add(p);
            }
        }
        projectiles=alive;
    }

    private void updateEffects(){
        ArrayList<AttackEffect> alive=new ArrayList<>();
        for(AttackEffect e:effects){ e.update(); if(e.isAlive()) alive.add(e); }
        effects=alive;
    }

    private void changePhase(){ if(hp<maxHp/2) phase=2; }
   
    public void draw(Graphics g, int cameraX){
        // フレームカウントを遅く使うために divisor を追加
        int moveFrameSpeed = 5;    // 数値を大きくすると移動アニメの切り替えが遅くなる
        int attackFrameSpeed = 5;  // 攻撃アニメの切り替え速度

        Image sprite;
        if(attackCooldown < 10){
            // 攻撃アニメ
            sprite = attackSprites[(frameCount / attackFrameSpeed) % attackSprites.length];
        } else {
            // 移動アニメ
            sprite = moveSprites[(frameCount / moveFrameSpeed) % moveSprites.length];
        }

        // 描画サイズもここで指定（必要に応じて変更）
        int drawWidth = 300;
        int drawHeight = 300;
        g.drawImage(sprite, x - cameraX, y, drawWidth, drawHeight, null);

        // HPバー描画
        g.setColor(Color.MAGENTA);
        int barWidth = 200, hpWidth = (int)(barWidth * ((double)hp / maxHp));
        g.fillRect(x - barWidth/2 - cameraX, y - 40, hpWidth, 10);
        g.setColor(Color.BLACK);
        g.drawRect(x - barWidth/2 - cameraX, y - 40, barWidth, 10);

        // 弾・攻撃エフェクト描画
        for(Projectile p : projectiles) p.draw(g, cameraX);
        for(AttackEffect e : effects) e.draw(g, cameraX);
    }

    public Clip getBGM() { return bgm; }
    
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles); // List<Projectile> を返す
    }

    public int getX() { return x; }
    public int getY() { return y; }

    
    
    // 　上のやつの差し替え
    public Rectangle getHitBox() {
    int scaledW = 300;
    int scaledH = 300;
    return new Rectangle(x, y, scaledW, scaledH);
    }

        

}
    
