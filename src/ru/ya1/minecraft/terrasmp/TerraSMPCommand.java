package ru.ya1.minecraft.terrasmp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;

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
								"§7[§cÇÄÎĞ§7]:§f " + TerraSMPPlayer.getStatusBar(600, sickness, "negative") + "\n"+
								"§7[§2ÁÎÄĞ§7]:§f " + TerraSMPPlayer.getStatusBar(20000, energy, "positive") + "\n"+
								"§7[§9ÂÎÄÀ§7]:§f " + TerraSMPPlayer.getStatusBar(3600, water, "positive") + "\n"+
								"§7[§6ÒÅÌÏ§7]:§f " + TerraSMPPlayer.getStatusBar(200, heatlevel, "temperature") + "\n"
						);
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("rents")){
			if(sender instanceof Player){
				Player plr = (Player) sender;
				if(getArg(args,0).equalsIgnoreCase("set")){
					if(plr.hasPermission("terrasmp.rents.admin")){
						if(args.length == 5){
							long timestamp      = System.currentTimeMillis()/1000;
							String rentBy 		= plr.getName();
							String rentTo 		= getArg(args,1);
							String resName 		= getArg(args,2);
							String subZone 		= getArg(args,3);
							int numDays 		= Integer.parseInt(getArg(args,4));
							long nextTime     	= timestamp+(86400*numDays);
							if(TerraSMP.ifNPE(resName, subZone)){
								plr.sendMessage("§c[rents] Ğåçèäåíöèÿ èëè ñóáçîíà íå íàéäåíû.\n[rents] Ïğîâåğüòå ïğàâèëüíîñòü ââîäà.");
								return true;
							}
							if(!TerraSMPPlayer.playerExists(rentTo)){
								plr.sendMessage("§c[rents] Èãğîê íå íàéäåí â áàçå äàííûõ. \n[rents] Îí íè ğàçó íå çàõîäèë íà ñåğâåğ?");
								return true;
							}
							ResidenceManager rmanager = Residence.getResidenceManager();
							boolean drop = true;
							if(rmanager.getByName(resName).getSubzone(subZone).getOwner().equalsIgnoreCase(rentTo)) drop = false;
							rmanager.getByName(resName).getSubzone(subZone).getPermissions().setOwner(rentTo, drop);
							TerraSMPRents.updateRent(rentTo, rentBy, resName, subZone, nextTime, true);
							plr.sendMessage("§a* Âû àğåíäîâàëè ó÷àñòîê "+resName+"."+subZone+" èãğîêó "+rentTo+" íà "+numDays+" äí.");
							if(Bukkit.getPlayerExact(rentTo) != null){
								Bukkit.getPlayerExact(rentTo).sendMessage("§a* "+rentBy+" àğåíäîâàë âàì ó÷àñòîê "+resName+"."+subZone+" íà "+numDays+" äí.");
							}
						}else{
							plr.sendMessage("§c[rents] Íåïğàâèëüíûé ââîä êîìàíäû: \n[rents] /rents set player residence subzone days");
						}
					}else{
						plr.sendMessage("§c[rents] No permissions.");
					}
					return true;
				}else if(getArg(args,0).equalsIgnoreCase("list")){
					if(plr.hasPermission("terrasmp.rents.admin")){
						if(getArg(args,1) != null){
							String rentor = getArg(args,1);
							String rents  = TerraSMPRents.getPlayerRentsList(rentor);
							plr.sendMessage(rents);
						}else{
							plr.sendMessage("§c[rents] Íåïğàâèëüíûé ââîä êîìàíäû: \n[rents] /rents list èãğîê");
						}
					}else{
						plr.sendMessage("§c[rents] No permissions.");
					}
					return true;
				}else{
					String rentor = plr.getName();
					String rents  = TerraSMPRents.getPlayerRentsList(rentor);
					plr.sendMessage(rents);
					return true;
				}
			}
		}
		if(cmd.getName().equalsIgnoreCase("debug")){
			if(sender instanceof Player){
				Player plr = (Player)sender;
				if(plr.hasPermission("terrasmp.debug")){
					if(getArg(args,0).equalsIgnoreCase("sethealth")){
						if(getArg(args,1) != ""){
							int Health = Integer.parseInt(args[1]);
							if(Health > 0){
								plr.setMaxHealth(Health);
								plr.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 100, 100));
							}
						}
					}else if(getArg(args,0).equalsIgnoreCase("setspeed")){
						if(getArg(args,1) != ""){
							float Speed = (float) (0.2*Integer.parseInt(args[1]));
							if(Speed > 0) plr.setWalkSpeed(Speed);
						}
					}else{
						plr.sendMessage("§c[debug] wakaranai!");
					}
				}else{
					plr.sendMessage("§c[debug] no more debug, use /status");
				}
			}
			return true;
		}
		return false;
	}
}
