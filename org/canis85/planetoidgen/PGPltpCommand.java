package org.canis85.planetoidgen;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handler for the /pltp command
 * @author Canis85
 */
public class PGPltpCommand implements CommandExecutor {

   private final PlanetoidGen plugin;
   private String worldName;

   public PGPltpCommand(PlanetoidGen plugin, String worldName) {
      this.plugin = plugin;
      this.worldName = worldName;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
      if (!(sender instanceof Player)) {
         return false;
      }
      Player player = (Player) sender;
      if (player.getWorld().equals(plugin.getServer().getWorld(worldName))) {
         player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
      } else {
         player.teleport(plugin.getServer().getWorld(worldName).getSpawnLocation());
      }

      return true;
   }
}