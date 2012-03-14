package org.canis85.planetoidgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Sample plugin for Bukkit
 *
 * @author Dinnerbone
 */
public class PlanetoidGen extends JavaPlugin {

   String worldName = null;
   private final Map<String, Object> CONFIG_DEFAULTS = new HashMap<String, Object>();
   private BukkitScheduler scheduler;
   public static World planetoids = null;

   private void loadDefaults() {
      getConfig().options().copyDefaults(true);
      getConfig().addDefault("planetoids.worldname", "Planetoids");
      getConfig().addDefault("planetoids.alwaysnight", Boolean.valueOf(false));
      getConfig().addDefault("planetoids.weather", Boolean.valueOf(false));
      getConfig().addDefault("planetoids.commands.pltp", Boolean.valueOf(true));
      getConfig().addDefault("planetoids.disablemonsters", Boolean.valueOf(true));
      getConfig().addDefault("planetoids.disableanimals", Boolean.valueOf(false));
      getConfig().addDefault("planetoids.seed", getServer().getWorlds().get(0).getSeed());
      getConfig().addDefault("planetoids.planets.density", 750);
      getConfig().addDefault("planetoids.planets.minSize", 4);
      getConfig().addDefault("planetoids.planets.maxSize", 20);
      getConfig().addDefault("planetoids.planets.minDistance", 10);
      getConfig().addDefault("planetoids.planets.minShellSize", 3);
      getConfig().addDefault("planetoids.planets.maxShellSize", 5);
      getConfig().addDefault("planetoids.planets.floorBlock", "GRASS");
      getConfig().addDefault("planetoids.planets.floorHeight", 0);
      getConfig().addDefault("planetoids.planets.bedrock", Boolean.valueOf(false));

      ArrayList<String> cores = new ArrayList<String>();
      ArrayList<String> shells = new ArrayList<String>();

      shells.add(Material.STONE.toString() + "-1.0");
      shells.add(Material.DIRT.toString() + "-1.0");
      shells.add(Material.LEAVES.toString() + "-0.9");
      shells.add(Material.ICE.toString() + "-0.9");
      shells.add(Material.SNOW_BLOCK.toString() + "-0.9");
      shells.add(Material.GLOWSTONE.toString() + "-0.4");
      shells.add(Material.BRICK.toString() + "-0.6");
      shells.add(Material.SANDSTONE.toString() + "-0.8");
      shells.add(Material.OBSIDIAN.toString() + "-0.5");
      shells.add(Material.MOSSY_COBBLESTONE.toString() + "-0.3");
      shells.add(Material.WOOL.toString() + "-0.4");
      shells.add(Material.GLASS.toString() + "-0.9");

      cores.add(Material.PUMPKIN.toString() + "-0.8");
      cores.add(Material.STATIONARY_LAVA.toString() + "-0.8");
      cores.add(Material.STATIONARY_WATER.toString() + "-1.0");
      cores.add(Material.COAL_ORE.toString() + "-1.0");
      cores.add(Material.IRON_ORE.toString() + "-0.8");
      cores.add(Material.DIAMOND_ORE.toString() + "-0.4");
      cores.add(Material.CLAY.toString() + "-0.3");
      cores.add(Material.LAPIS_ORE.toString() + "-0.4");
      cores.add(Material.LOG.toString() + "-1.0");
      cores.add(Material.GOLD_ORE.toString() + "-0.6");
      cores.add(Material.REDSTONE_ORE.toString() + "-0.75");
      cores.add(Material.SAND.toString() + "-1.0");
      cores.add(Material.BEDROCK.toString() + "-0.5");
      cores.add(Material.AIR.toString() + "-1.0");
      cores.add(Material.DIRT.toString() + "-1.0");

      getConfig().addDefault("planetoids.planets.blocks.cores", cores);
      getConfig().addDefault("planetoids.planets.blocks.shells", shells);
      saveConfig();
   }

   @Override
   public void onDisable() {
      if (worldName != null) {
         getServer().unloadWorld(worldName, true);
      }

      PluginDescriptionFile pdfFile = this.getDescription();
      System.out.println(pdfFile.getName() + " unloaded.");
   }

   private boolean loadSettings() {
      loadDefaults();
//      if (plConfigFile.exists()) {
//         planetConfig = YamlConfiguration.loadConfiguration(plConfigFile);
//         planetConfig.setHeader("#Planetoids configuration file");
//         boolean refreshConfig = false;
//         for (String prop : CONFIG_DEFAULTS.keySet()) {
//            if (planetConfig.getProperty(prop) == null) {
//               refreshConfig = true;
//               planetConfig.setProperty(prop, CONFIG_DEFAULTS.get(prop));
//            }
//         }
//         if (refreshConfig) {
//            planetConfig.save();
//         }
//      } else {
//         try {
//            getDataFolder().mkdirs();
//            plConfigFile.createNewFile();
//            planetConfig = new Configuration(plConfigFile);
//            planetConfig.setHeader("#Planetoids configuration file");
//            for (String s : CONFIG_DEFAULTS.keySet()) {
//               planetConfig.setProperty(s, CONFIG_DEFAULTS.get(s));
//            }
//            planetConfig.save(plConfigFile);
//         } catch (Exception ex) {
//            System.err.println("[PLANETOIDS] Problem loading config file:");
//            ex.printStackTrace();
//            return false;
//         }
//      }
      return true;
   }

   @Override
   public void onEnable() {
      boolean settingsLoaded = loadSettings();

      PluginDescriptionFile pdfFile = this.getDescription();
      if (settingsLoaded) {
         worldName = getConfig().getString("planetoids.worldname", "Planetoids");

         if (getConfig().getBoolean("planetoids.commands.pltp", true)) {
            getCommand("pltp").setExecutor(new PGPltpCommand(this, worldName));
         }

         //Create chunk generator
         PGChunkGenerator pgGen = new PGChunkGenerator(this);
         
         WorldCreator wc = new WorldCreator(worldName);
         wc.seed((long) getConfig().getDouble("planetoids.seed", 0.0));
         wc.environment(Environment.NORMAL);
         wc.generator(pgGen);

         planetoids = getServer().createWorld(wc);

         if (!getConfig().getBoolean("planetoids.weather", false)) {
            planetoids.setWeatherDuration(0);
         }
         
         planetoids.setSpawnFlags(!getConfig().getBoolean("planetoids.disablemonsters", false), !getConfig().getBoolean("planetoids.disableanimals", false));

         scheduler = getServer().getScheduler();
         PGRunnable task = new PGRunnable();
         if (getConfig().getBoolean("planetoids.alwaysnight", true)) {
            scheduler.scheduleSyncRepeatingTask(this, task, 60L, 8399L);
         }

         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
      } else {
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " unable to load!  Check settings file.");
      }
   }
}
