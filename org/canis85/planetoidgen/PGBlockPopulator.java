package org.canis85.planetoidgen;

import java.util.List;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

/**
 * Finishes Planetoids chunks after they're generated.  Does stuff like
 * coloring wool, adding grass, etc.
 *
 * @author Canis85
 */
public class PGBlockPopulator extends BlockPopulator {

   Plugin plugin;
   PGChunkGenerator chunkGen;

   public PGBlockPopulator(Plugin plugin, PGChunkGenerator chunkGen) {
      this.plugin = plugin;
      this.chunkGen = chunkGen;
   }

   @Override
   public void populate(World world, Random random, Chunk chunk) {
      List<Planetoid> curSystem = chunkGen.getSystem(world, chunk.getX(), chunk.getZ());

      //figure out the chunk's position in the "system"
      int chunkXPos;
      if (chunk.getX() >= 0) {
         chunkXPos = (chunk.getX() % PGChunkGenerator.SYSTEM_SIZE) * 16;
      } else {
         chunkXPos = ((-chunk.getX()) % PGChunkGenerator.SYSTEM_SIZE) * 16;
         if (chunkXPos == 0) {
            chunkXPos = PGChunkGenerator.SYSTEM_SIZE * 16;
         }
         chunkXPos = (PGChunkGenerator.SYSTEM_SIZE) * 16 - chunkXPos;
         //chunkXPos = SYSTEM_SIZE * 16 + ((x % SYSTEM_SIZE) * 16);
      }
      int chunkZPos;
      if (chunk.getZ() >= 0) {
         chunkZPos = (chunk.getZ() % PGChunkGenerator.SYSTEM_SIZE) * 16;
      } else {
         chunkZPos = ((-chunk.getZ()) % PGChunkGenerator.SYSTEM_SIZE) * 16;
         if (chunkZPos == 0) {
            chunkZPos = PGChunkGenerator.SYSTEM_SIZE * 16;
         }
         chunkZPos = (PGChunkGenerator.SYSTEM_SIZE) * 16 - chunkZPos;
         //chunkZPos = SYSTEM_SIZE * 16 + ((z % SYSTEM_SIZE) * 16);
      }


      //Go through the Planetoids and set custom data values as needed
      //Go through the current system's planetoids and fill in this chunk as needed.
      for (Planetoid curPl : curSystem) {
         //Find planet's center point relative to this chunk.
         int relCenterX = curPl.xPos - chunkXPos;
         int relCenterZ = curPl.zPos - chunkZPos;

         //Set shell MaterialData
         if (curPl.shellBlkMat != 0) {
            for (int curX = -curPl.radius; curX <= curPl.radius; curX++) {
               int blkX = curX + relCenterX;
               if (blkX >= 0 && blkX < 16) {
                  //Figure out radius of this circle
                  int distFromCenter = Math.abs(curX);
                  int radius = (int) Math.ceil(Math.sqrt((curPl.radius * curPl.radius) - (distFromCenter * distFromCenter)));
                  for (int curZ = -radius; curZ <= radius; curZ++) {
                     int blkZ = curZ + relCenterZ;
                     if (blkZ >= 0 && blkZ < 16) {
                        int zDistFromCenter = Math.abs(curZ);
                        int zRadius = (int) Math.ceil(Math.sqrt((radius * radius) - (zDistFromCenter * zDistFromCenter)));
                        for (int curY = -zRadius; curY <= zRadius; curY++) {
                           int blkY = curPl.yPos + curY;
                           chunk.getBlock(blkX, blkY, blkZ).setData(curPl.shellBlkMat);
                        }
                     }
                  }
               }
            }
         }

         //Set core MaterialData
         int coreRadius = curPl.radius - curPl.shellThickness;
         if (coreRadius > 0 && curPl.coreBlkMat != 0) {
            for (int curX = -coreRadius; curX <= coreRadius; curX++) {
               int blkX = curX + relCenterX;
               if (blkX >= 0 && blkX < 16) {
                  //Figure out radius of this circle
                  int distFromCenter = Math.abs(curX);
                  int radius = (int) Math.ceil(Math.sqrt((coreRadius * coreRadius) - (distFromCenter * distFromCenter)));
                  for (int curZ = -radius; curZ <= radius; curZ++) {
                     int blkZ = curZ + relCenterZ;
                     if (blkZ >= 0 && blkZ < 16) {
                        int zDistFromCenter = Math.abs(curZ);
                        int zRadius = (int) Math.ceil(Math.sqrt((radius * radius) - (zDistFromCenter * zDistFromCenter)));
                        for (int curY = -zRadius; curY <= zRadius; curY++) {
                           int blkY = curPl.yPos + curY;
                           chunk.getBlock(blkX, blkY, blkZ).setData(curPl.coreBlkMat);
                        }
                     }
                  }
               }
            }
         }
      }

      //Set floor MaterialData
      byte floorBlockData = (byte)plugin.getConfig().getInt("planetoids.planets.floorBlockData");
      boolean bedrockFloor = plugin.getConfig().getBoolean("planetoids.planets.bedrock");
      if (floorBlockData != 0) {
         for (int i = 0; i < plugin.getConfig().getInt("planetoids.planets.floorHeight"); i++) {
            for (int j = 0; j < 16; j++) {
               for (int k = 0; k < 16; k++) {
                  if (i != 0 || !bedrockFloor) {
                     chunk.getBlock(j, i, k).setData(floorBlockData);
                  }
               }
            }
         }
      }
   }
}
