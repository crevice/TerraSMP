package ru.ya1.minecraft.terrasmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TerraSMPCommand implements CommandExecutor {
	public static String getArg(String[] data, int index){
	    try{
	      data[index].length();
	      return data[index];
	    } catch(ArrayIndexOutOfBoundsException e){
	      return "";
	    }
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("status")){
			if(sender instanceof Player){
				Player plr = (Player) sender;
				String playername = plr.getName();
				double energy 		= TerraSMPPlayer.getEnergy(playername);
				double water  		= TerraSMPPlayer.getSaturation(playername);
				double heatlevel 	= TerraSMPPlayer.getHeat(playername);
				double sickness 	= TerraSMPPlayer.getSickness(playername);
				plr.sendMessage("§7|----------------------------------------|\n"+
								"§7[§cЗДОР§7]:§f " + TerraSMPPlayer.getStatusBar(600, sickness, "negative") + "\n"+
								"§7[§2БОДР§7]:§f " + TerraSMPPlayer.getStatusBar(20000, energy, "positive") + "\n"+
								"§7[§9ВОДА§7]:§f " + TerraSMPPlayer.getStatusBar(3600, water, "positive") + "\n"+
								"§7[§6ТЕМП§7]:§f " + TerraSMPPlayer.getStatusBar(200, heatlevel, "temperature") + "\n"
						);
			}
			return true;
		}
		return false;
	}
}
