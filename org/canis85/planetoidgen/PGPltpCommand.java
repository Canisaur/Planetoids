/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.canis85.planetoidgen;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
* Handler for the /pos sample command.
* @author SpaceManiac
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

        player.teleport(plugin.getServer().getWorld(worldName).getSpawnLocation());

        return true;
    }
}