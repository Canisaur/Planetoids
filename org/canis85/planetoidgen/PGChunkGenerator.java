package org.canis85.planetoidgen;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

/**
 * Generates a Planetoids world.
 *
 * Planetoids are generated in "systems" that are (by default) 100x100 chunks (1600x1600 blocks) in size.
 *
 * @author Canis85
 */
public class PGChunkGenerator extends ChunkGenerator {

   private Map<Material, Float> allowedShells;
   private Map<Material, Float> allowedCores;
   private Map<Point, List<Planetoid>> cache;
   private static final int SYSTEM_SIZE = 50;
   private long seed;   //Seed for generating planetoids
   private int density; //Number of planetoids it will try to create per "system"
   private int minSize; //Minimum radius
   private int maxSize; //Maximum radius
   private int minDistance; //Minimum distance between planets, in blocks
   private int floorHeight; //Floor height
   private Material floorBlock; //BlockID for the floor
   private int maxShellSize;  //Maximum shell thickness
   private int minShellSize;  //Minimum shell thickness, should be at least 3
   private Plugin plugin;     //ref to plugin
   private boolean bedrockFloor; //if true and floorHeight > 0, the very bottom will be bedrock

   private void loadAllowedBlocks() {
      allowedCores = new EnumMap<Material, Float>(Material.class);
      allowedShells = new EnumMap<Material, Float>(Material.class);
      for (String s : plugin.getConfig().getStringList("planetoids.planets.blocks.cores")) {
         String[] sSplit = s.split("-");
         Material newMat = Material.matchMaterial(sSplit[0]);
         if (newMat.isBlock()) {
            if (sSplit.length == 2) {
               allowedCores.put(newMat, Float.valueOf(sSplit[1]));
            } else {
               allowedCores.put(newMat, 1.0f);
            }
         }
      }

      for (String s : plugin.getConfig().getStringList("planetoids.planets.blocks.shells")) {
         String[] sSplit = s.split("-");
         Material newMat = Material.matchMaterial(sSplit[0]);
         if (newMat.isBlock()) {
            if (sSplit.length == 2) {
               allowedShells.put(newMat, Float.valueOf(sSplit[1]));
            } else {
               allowedShells.put(newMat, 1.0f);
            }
         }
      }
   }

   public PGChunkGenerator(Plugin plugin) {
      this.plugin = plugin;
      this.seed = (long) plugin.getConfig().getDouble("planetoids.seed", plugin.getServer().getWorlds().get(0).getSeed());
      this.density = plugin.getConfig().getInt("planetoids.planets.density", 750);
      minSize = plugin.getConfig().getInt("planetoids.planets.minSize", 4);
      maxSize = plugin.getConfig().getInt("planetoids.planets.maxSize", 20);
      minDistance = plugin.getConfig().getInt("planetoids.planets.minDistance", 10);
      floorBlock = Material.matchMaterial(plugin.getConfig().getString("planetoids.planets.floorBlock", "GRASS"));
      this.floorHeight = plugin.getConfig().getInt("planetoids.planets.floorHeight", 0);
      minShellSize = plugin.getConfig().getInt("planetoids.planets.minShellSize", 3);
      maxShellSize = plugin.getConfig().getInt("planetoids.planets.maxShellSize", 5);
      bedrockFloor = plugin.getConfig().getBoolean("planetoids.planets.bedrock", true);

      loadAllowedBlocks();

      cache = new HashMap<Point, List<Planetoid>>();
   }

   @Override
   public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
      world.setBiome(x, z, Biome.SKY);
      int height = world.getMaxHeight();
      short[][] retVal = new short[height / 16][];

      int sysX;
      if (x >= 0) {
         sysX = x / SYSTEM_SIZE;
      } else {
         sysX = (int) Math.ceil((-x) / (SYSTEM_SIZE + 1));
         sysX = -sysX;
      }

      int sysZ;
      if (z >= 0) {
         sysZ = z / SYSTEM_SIZE;
      } else {
         sysZ = (int) Math.ceil((-z) / (SYSTEM_SIZE + 1));
         sysZ = -sysZ;
      }

      //check if the "system" this chunk is in is cached
      List<Planetoid> curSystem = cache.get(new Point(sysX, sysZ));

      if (curSystem == null) {
         //if not, does it exist on disk?
         File systemFolder = new File(plugin.getDataFolder(), "Systems");
         if (!systemFolder.exists()) {
            systemFolder.mkdir();
         }
         File systemFile = new File(systemFolder, "system_" + sysX + "." + sysZ + ".dat");
         if (systemFile.exists()) {
            try {
               //load and cache
               FileInputStream fis = new FileInputStream(systemFile);
               ObjectInputStream ois = new ObjectInputStream(fis);
               curSystem = (List<Planetoid>) ois.readObject();
               cache.put(new Point(sysX, sysZ), curSystem);
               ois.close();
               fis.close();
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         } else {
            //generate, save, and cache
            curSystem = generatePlanets(sysX, sysZ, world.getMaxHeight());
            try {
               systemFile.createNewFile();
               FileOutputStream fos = new FileOutputStream(systemFile);
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(curSystem);
               oos.flush();
               oos.close();
               fos.flush();
               fos.close();
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            cache.put(new Point(sysX, sysZ), curSystem);
         }
      }

      //figure out the chunk's position in the "system"
      int chunkXPos;
      if (x >= 0) {
         chunkXPos = (x % SYSTEM_SIZE) * 16;
      } else {
         chunkXPos = ((-x) % SYSTEM_SIZE) * 16;
         if (chunkXPos == 0) {
            chunkXPos = SYSTEM_SIZE * 16;
         }
         chunkXPos = (SYSTEM_SIZE) * 16 - chunkXPos;
         //chunkXPos = SYSTEM_SIZE * 16 + ((x % SYSTEM_SIZE) * 16);
      }
      int chunkZPos;
      if (z >= 0) {
         chunkZPos = (z % SYSTEM_SIZE) * 16;
      } else {
         chunkZPos = ((-z) % SYSTEM_SIZE) * 16;
         if (chunkZPos == 0) {
            chunkZPos = SYSTEM_SIZE * 16;
         }
         chunkZPos = (SYSTEM_SIZE) * 16 - chunkZPos;
         //chunkZPos = SYSTEM_SIZE * 16 + ((z % SYSTEM_SIZE) * 16);
      }

      //Go through the current system's planetoids and fill in this chunk as needed.
      for (Planetoid curPl : curSystem) {
         //Find planet's center point relative to this chunk.
         int relCenterX = curPl.xPos - chunkXPos;
         int relCenterZ = curPl.zPos - chunkZPos;

         //Generate shell
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
                        //retVal[(blkX * 16 + blkZ) * 128 + blkY] = (byte) curPl.shellBlk.getId();
                        if (retVal[blkY >> 4] == null) {
                           retVal[blkY >> 4] = new short[4096];
                        }
                        retVal[blkY >> 4][((blkY & 0xF) << 8) | (blkZ << 4) | blkX] = (byte) curPl.shellBlk.getId();
                     }
                  }
               }
            }
         }

         //Generate core
         int coreRadius = curPl.radius - curPl.shellThickness;
         if (coreRadius > 0) {
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
                           //retVal[(blkX * 16 + blkZ) * 128 + blkY] = (byte) curPl.coreBlk.getId();
                           if (retVal[blkY >> 4] == null) {
                              retVal[blkY >> 4] = new short[4096];
                           }
                           retVal[blkY >> 4][((blkY & 0xF) << 8) | (blkZ << 4) | blkX] = (byte) curPl.coreBlk.getId();
                        }
                     }
                  }
               }
            }
         }
      }
      //Fill in the floor
      if (floorHeight > 0 && retVal[0] == null) {
         retVal[0] = new short[4096];
      }
      for (int i = 0; i < floorHeight; i++) {
         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               if (i == 0 && bedrockFloor) {
                  //retVal[j * 2048 + k * 128 + i] = (byte) Material.BEDROCK.getId();
                  retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) Material.BEDROCK.getId();
               } else {
                  //retVal[j * 2048 + k * 128 + i] = (byte) floorBlock.getId();
                  retVal[i >> 4][((i & 0xF) << 8) | (j << 4) | k] = (byte) floorBlock.getId();
               }
            }
         }
      }
      return retVal;
   }

   @Override
   public boolean canSpawn(World world, int x, int z) {
      return true;
   }

   @Override
   public Location getFixedSpawnLocation(World world, Random random) {
      return new Location(world, 7, 77, 7);
   }

   private List<Planetoid> generatePlanets(int x, int z, int height) {
      List<Planetoid> planetoids = new ArrayList<Planetoid>();

      //If x and Z are zero, generate a log/leaf planet close to 0,0
      if (x == 0 && z == 0) {
         Planetoid spawnPl = new Planetoid();
         spawnPl.xPos = 7;
         spawnPl.yPos = 70;
         spawnPl.zPos = 7;
         spawnPl.coreBlk = Material.LOG;
         spawnPl.shellBlk = Material.LEAVES;
         spawnPl.shellThickness = 3;
         spawnPl.radius = 6;
         planetoids.add(spawnPl);
      }

      //if X is negative, left shift seed by one
      if (x < 0) {
         seed = seed << 1;
      } //if Z is negative, change sign on seed.
      if (z < 0) {
         seed = -seed;
      }

      Random rand = new Random(seed);
      for (int i = 0; i
              < Math.abs(x) + Math.abs(z); i++) {
         //cycle generator
         rand.nextDouble();
      }

      for (int i = 0; i < density; i++) {
         //Try to make a planet
         Planetoid curPl = new Planetoid();
         curPl.shellBlk = getBlockType(rand, false, true);
         switch (curPl.shellBlk) {
            case LEAVES:
               curPl.coreBlk = Material.LOG;
               break;
            case ICE:
            case WOOL:
               curPl.coreBlk = getBlockType(rand, true, true);
            default:
               curPl.coreBlk = getBlockType(rand, true, false);
               break;
         }

         curPl.shellThickness = rand.nextInt(maxShellSize - minShellSize) + minShellSize;
         curPl.radius = rand.nextInt(maxSize - minSize) + minSize;

         //Set position, check bounds with system edges
         curPl.xPos = -1;
         while (curPl.xPos == -1) {
            int curTry = rand.nextInt(SYSTEM_SIZE * 16);
            if (curTry + curPl.radius < SYSTEM_SIZE * 16 && curTry - curPl.radius >= 0) {
               curPl.xPos = curTry;
            }
         }
         curPl.yPos = rand.nextInt(height - curPl.radius * 2 - floorHeight) + curPl.radius;
         curPl.zPos = -1;
         while (curPl.zPos == -1) {
            int curTry = rand.nextInt(SYSTEM_SIZE * 16);
            if (curTry + curPl.radius < SYSTEM_SIZE * 16 && curTry - curPl.radius >= 0) {
               curPl.zPos = curTry;
            }
         }

         //Created a planet, check for collisions with existing planets
         //If any collision, discard planet
         boolean discard = false;
         for (Planetoid pl : planetoids) {
            //each planetoid has to be at least pl1.radius + pl2.radius + min distance apart
            int distMin = pl.radius + curPl.radius + minDistance;
            if (distanceSquared(pl, curPl) < distMin * distMin) {
               discard = true;
               break;
            }
         }
         if (!discard) {
            planetoids.add(curPl);
         }
      }
      System.out.println("Made new system with " + planetoids.size() + " planetoids."); //DEBUG
      return planetoids;
   }

   private int distanceSquared(Planetoid pl1, Planetoid pl2) {
      int xDist = pl2.xPos - pl1.xPos;
      int yDist = pl2.yPos - pl1.yPos;
      int zDist = pl2.zPos - pl1.zPos;

      return xDist * xDist + yDist * yDist + zDist * zDist;
   }

   /**
    * Returns a valid block type
    *
    * @param rand random generator to use
    * @param core if true, searching through allowed cores, otherwise allowed shells
    * @param heated if true, will not return a block that gives off heat
    * @return
    */
   private Material getBlockType(Random rand, boolean core, boolean noHeat) {
      Material retVal = null;
      Map<Material, Float> refMap;
      if (core) {
         refMap = allowedCores;
      } else {
         refMap = allowedShells;
      }
      while (retVal == null) {
         int arrayPos = rand.nextInt(refMap.size());
         Material blkID = (Material) refMap.keySet().toArray()[arrayPos];
         float testVal = rand.nextFloat();
         if (refMap.get(blkID) >= testVal) {
            if (noHeat) {
               switch (blkID) {
                  case BURNING_FURNACE:
                  case FIRE:
                  case GLOWSTONE:
                  case JACK_O_LANTERN:
                  case STATIONARY_LAVA:
                     break;
                  default:
                     retVal = blkID;
                     break;
               }
            } else {
               retVal = blkID;
            }
         }
      }
      return retVal;

   }
}
