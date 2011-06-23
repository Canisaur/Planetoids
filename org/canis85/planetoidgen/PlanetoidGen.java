package org.canis85.planetoidgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sample plugin for Bukkit
 *
 * @author Dinnerbone
 */
public class PlanetoidGen extends JavaPlugin {

   String worldName = null;

   // NOTE: There should be no need to define a constructor any more for more info on moving from
   // the old constructor see:
   // http://forums.bukkit.org/threads/too-long-constructor.5032/
   public void onDisable() {
      // TODO: Place any custom disable code here
      if (worldName != null) {
         getServer().unloadWorld(worldName, true);
      }

      // NOTE: All registered events are automatically unregistered when a plugin is disabled

      // EXAMPLE: Custom code, here we just output some info so we can check all is well
      PluginDescriptionFile pdfFile = this.getDescription();
      System.out.println(pdfFile.getName() + " unloaded.");
   }

   public void onEnable() {
      Map<Material, Float> cores = new EnumMap<Material, Float>(Material.class);
      Map<Material, Float> shells = new EnumMap<Material, Float>(Material.class);
      Material floor = Material.AIR;
      boolean bedrockFloor = false;
      int floorHeight = 0;
      long seed = 0;
      int density = 0;
      int minPlanetSize = 0;
      int maxPlanetSize = 0;
      int minPlanetDistance = 0;
      int shellSizeMin = 0;
      int shellSizeMax = 0;

      boolean settingsLoaded = false;
      // Try to load settings files
      try {
         File settingsFile = new File("plugins" + File.separator + "Planetoids" + File.separator + "settings.properties");
         if (!settingsFile.exists()) {
            //not found, create default settings file
            settingsFile.getParentFile().mkdirs();
            settingsFile.createNewFile();

            String defaultSettings = ""
                    + "pltp-command-enabled:true\n"
                    + "world-name:Planetoids\n"
                    + "world-seed:" + getServer().getWorlds().get(0).getSeed() + "\n"
                    + "\n# Number of planets that it will ATTEMPT to create\ndensity:15000\n"
                    + "min-planet-size:4\n"
                    + "max-planet-size:20\n"
                    + "min-planet-distance:10\n"
                    + "min-shell-size:3\n"
                    + "max-shell-size:5\n"
                    + "cores:9,11-0.75,12-1.0,14-0.5,15-0.8,16-0.9,17-1.0,21-0.5,46-0.5,56-0.25,73-0.5,79-1.0,82-0.5,86-0.2\n"
                    + "shells:1,2-0.8,3-1.0,17-0.5,24-1.0,35-0.5,45-0.75,48-0.4,49-0.2,79-0.8,80-0.8,87-0.4,89-0.2\n"
                    + "floor-block:9\n"
                    + "floor-height:4\n"
                    + "bedrock-floor:true";
            BufferedWriter bw = new BufferedWriter(new FileWriter(settingsFile));
            bw.write(defaultSettings);
            bw.flush();
            bw.close();
         }

         //Read in settings
         BufferedReader rdr = new BufferedReader(new FileReader(settingsFile));
         while (rdr.ready()) {
            String[] curLine = rdr.readLine().split(":");
            if (curLine[0].equals("pltp-command-enabled")) {
               if (curLine[1].equalsIgnoreCase("true")) {
                  getCommand("pltp").setExecutor(new PGPltpCommand(this));
               }
            } else if (curLine[0].equals("world-name")) {
               worldName = curLine[1];
            } else if (curLine[0].equals("cores")) {
               String[] coreStrings = curLine[1].split(",");
               for (int i = 0; i < coreStrings.length; i++) {
                  String[] curCore = coreStrings[i].split("-");
                  Material curMat = Material.getMaterial(Integer.valueOf(curCore[0]));
                  if (curMat.isBlock()) {
                     if (curCore.length == 2) {
                        cores.put(curMat, Float.valueOf(curCore[1]));
                     } else {
                        cores.put(curMat, 1.0f);
                     }
                  }
               }
            } else if (curLine[0].equals("shells")) {
               String[] shellStrings = curLine[1].split(",");
               for (int i = 0; i < shellStrings.length; i++) {
                  String[] curShell = shellStrings[i].split("-");
                  Material curMat = Material.getMaterial(Integer.valueOf(curShell[0]));
                  if (curMat.isBlock()) {
                     if (curShell.length == 2) {
                        shells.put(curMat, Float.valueOf(curShell[1]));
                     } else {
                        shells.put(curMat, 1.0f);
                     }
                  }
               }
            } else if (curLine[0].equals("floor-block")) {
               floor = Material.getMaterial(Integer.valueOf(curLine[1]));
            } else if (curLine[0].equals("floor-height")) {
               floorHeight = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("bedrock-floor")) {
               if (curLine[1].equalsIgnoreCase("true")) {
                  bedrockFloor = true;
               }
            } else if (curLine[0].equals("world-seed")) {
               seed = Long.valueOf(curLine[1]);
            } else if (curLine[0].equals("density")) {
               density = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("min-planet-size")) {
               minPlanetSize = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("max-planet-size")) {
               maxPlanetSize = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("min-planet-distance")) {
               minPlanetDistance = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("min-shell-size")) {
               shellSizeMin = Integer.valueOf(curLine[1]);
            } else if (curLine[0].equals("max-shell-size")) {
               shellSizeMax = Integer.valueOf(curLine[1]);
            } else {
               if (!curLine[0].trim().isEmpty() && !curLine[0].trim().startsWith("#")) {
                  System.err.println("Unrecognized setting in Planetoids settings file: " + curLine[0]);
               }
            }
         }
         settingsLoaded = true;
      } catch (Exception ex) {
         ex.printStackTrace();
         System.err.println("Planetoids: Settings file error.  Check your syntax?");
      }

      PluginDescriptionFile pdfFile = this.getDescription();
      if (settingsLoaded) {
         int[] planetParams = new int[6];
         planetParams[0] = density;
         planetParams[1] = minPlanetSize;
         planetParams[2] = maxPlanetSize;
         planetParams[3] = minPlanetDistance;
         planetParams[4] = shellSizeMin;
         planetParams[5] = shellSizeMax;
         
         //Create chunk generator
         PGChunkGenerator pgGen = new PGChunkGenerator(seed, cores, shells, planetParams, floor, bedrockFloor, floorHeight);

         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         getServer().createWorld(worldName, Environment.NORMAL, seed, pgGen);
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
      } else {
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " unable to load!");
      }
   }
}
