package game;

import java.awt.Image;
import javax.sound.sampled.Clip;

public class CharacterData {
    public final Image idle;
    public final Image left;
    public final Image[] run;

    public final int moveSpeed;   // 走るスピード(px/frame)
    public final int jumpPower;   // ジャンプの初速(絶対値が大きいほど高く飛ぶ)
    public final int maxJump;     // 何段ジャンプまでOKか

    public final Clip voiceJump;    // ジャンプ時ボイス
    public final Clip voiceAttack;  // 攻撃時ボイス

    public CharacterData(
            Image idle, Image left, Image[] run,
            int moveSpeed, int jumpPower, int maxJump,
            Clip voiceJump, Clip voiceAttack
    ) {
        this.idle = idle;
        this.left = left;
        this.run = run;
        this.moveSpeed = moveSpeed;
        this.jumpPower = jumpPower;
        this.maxJump = maxJump;
        this.voiceJump = voiceJump;
        this.voiceAttack = voiceAttack;
    }
}