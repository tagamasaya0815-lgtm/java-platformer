package game;

import java.awt.Color;

public class HomingProjectile extends Projectile {
   int targetX;
   int targetY;
   double speed;

   public HomingProjectile(int var1, int var2, int var3, int var4, double var5) {
      super(var1, var2, 0, 0);
      this.targetX = var3;
      this.targetY = var4;
      this.speed = var5;
      this.color = Color.ORANGE;
   }

   public void update() {
      double var1 = (double)(this.targetX - this.x);
      double var3 = (double)(this.targetY - this.y);
      double var5 = Math.sqrt(var1 * var1 + var3 * var3);
      if (var5 == 0.0) {
         var5 = 1.0;
      }

      this.dx = (int)(var1 / var5 * this.speed);
      this.dy = (int)(var3 / var5 * this.speed);
      this.x += this.dx;
      this.y += this.dy;
      --this.lifeTime;
      if (this.lifeTime <= 0) {
         this.alive = false;
      }
   }
}
