package org.canis85.planetoidgen;

import java.io.File;
import org.bukkit.World.Environment;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sample plugin for Bukkit
 *
 * @author Dinnerbone
 */
public class PlanetoidGen extends JavaPlugin {

   // NOTE: There should be no need to define a constructor any more for more info on moving from
   // the old constructor see:
   // http://forums.bukkit.org/threads/too-long-constructor.5032/
   
   public void onDisable() {
      // TODO: Place any custom disable code here

      // NOTE: All registered events are automatically unregistered when a plugin is disabled

      // EXAMPLE: Custom code, here we just output some info so we can check all is well
      PluginDescriptionFile pdfFile = this.getDescription();
      System.out.println(pdfFile.getName() + " unloaded.");
   }

   public void onEnable() {
      // Register our commands
      getCommand("pltp").setExecutor(new PGPosCommand(this));

      // Try to load settings files
      File settingsFile = new File("plugins" + File.separator + "Planetoids" + File.separator + "settings.properties");
      if (!settingsFile.exists()) {
         //create default settings file

      }

      // EXAMPLE: Custom code, here we just output some info so we can check all is well
      getServer().createWorld("Planetoids", Environment.NORMAL, new PGChunkGenerator(43l));
      PluginDescriptionFile pdfFile = this.getDescription();
      System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
   }
}
