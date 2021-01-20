package com.mojang.minecraft.level.blocks;

import com.mojang.minecraft.entities.PrimedTnt;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.entities.particle.ParticleManager;

public final class TNTBlock extends Block {

   public TNTBlock(int var1, int var2) {
      super(46, 8);
   }

   protected final int getTextureId(int texture) {
      return texture == 0?this.textureId + 2:(texture == 1?this.textureId + 1:this.textureId);
   }

   public final int getDropCount() {
      return 0;
   }

   public final void explode(Level level, int x, int y, int z) {
      PrimedTnt tntEntity = new PrimedTnt(level, x + 0.5F, y + 0.5F, z + 0.5F);
      tntEntity.life = random.nextInt(tntEntity.life / 4) + tntEntity.life / 8;
      level.addEntity(tntEntity);
   }

   public final void spawnBreakParticles(Level level, int x, int y, int z, ParticleManager particleManager) {
      level.addEntity(new PrimedTnt(level, x + 0.5F, y + 0.5F, z + 0.5F));
      super.spawnBreakParticles(level, x, y, z, particleManager);
   }
}
