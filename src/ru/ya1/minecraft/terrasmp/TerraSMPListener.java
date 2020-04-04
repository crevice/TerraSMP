package ru.ya1.minecraft.terrasmp;

import java.util.Arrays;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class TerraSMPListener implements Listener{
	private static List<Material> hotFood = Arrays.asList(Material.COOKED_BEEF,Material.COOKED_CHICKEN,Material.COOKED_COD,Material.COOKED_MUTTON,Material.BAKED_POTATO,Material.COOKED_MUTTON,Material.COOKED_PORKCHOP,Material.COOKED_RABBIT,Material.COOKED_SALMON);
	@EventHandler
	public void onHealthChange(EntityRegainHealthEvent event){
		if ((event.getEntity() instanceof Player)){
			if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED){
				event.setCancelled(true);
			}else if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.MAGIC){
				Player plr = (Player) event.getEntity();
				String name = plr.getName();
				double sickness = TerraSMPPlayer.getSickness(name);
				TerraSMPPlayer.setSickness(name, sickness - 200);
			}else if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.MAGIC_REGEN){
				Player plr = (Player) event.getEntity();
				String name = plr.getName();
				double sickness = TerraSMPPlayer.getSickness(name);
				TerraSMPPlayer.setSickness(name, sickness - 5);
			}
		}
	}
	
	@EventHandler
	public void onEat(PlayerItemConsumeEvent event){
		if(event.getItem().getType() == Material.POTION || event.getItem().getType() == Material.MILK_BUCKET){
			String playername = event.getPlayer().getName();
			double water = TerraSMPPlayer.getSaturation(playername);
			double temp  = TerraSMPPlayer.getHeat(playername);
			TerraSMPPlayer.setHeat(playername, temp-5);
			TerraSMPPlayer.setSaturation(playername, water+300);
			if(event.getPlayer().getItemInHand().getDurability() > 0){
			Potion pot = Potion.fromDamage(event.getPlayer().getItemInHand().getDurability());
				if (pot.getType() == PotionType.NIGHT_VISION || pot.getType() == PotionType.STRENGTH) {
					TerraSMPPlayer.setHeat(playername, temp-15);
				}else if(pot.getType() == PotionType.FIRE_RESISTANCE){
					TerraSMPPlayer.setHeat(playername, temp+15);
				}
			}
		}else if(event.getItem().getType() == Material.APPLE || event.getItem().getType() == Material.MELON_STEM){
			String playername = event.getPlayer().getName();
			double water = TerraSMPPlayer.getSaturation(playername);
			TerraSMPPlayer.setSaturation(playername, water+50);
		}else if(hotFood.contains(event.getItem().getType())){
			String playername = event.getPlayer().getName();
			double temp = TerraSMPPlayer.getHeat(playername);
			TerraSMPPlayer.setHeat(playername, temp+5);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if(event.isCancelled()) return;
		if(event.getEntity() instanceof Player){
			Player plr = (Player) event.getEntity();
			if(plr.getGameMode() == GameMode.CREATIVE) return;
			String name = plr.getName();
			if(event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.THORNS || event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_EXPLOSION){
				if(TerraSMPPlayer.getEnergy(name) < 9000){
					int random = (int)(Math.random() * 4);
					if(random == 0){
						long timestamp = System.currentTimeMillis()/1000;
						TerraSMPPlayer.setBleeding(name, true);
						TerraSMPPlayer.setSickness(name, TerraSMPPlayer.getSickness(name)+50);
						TerraSMPThread.sendStatusMessage(plr, "§c* У вас кровотечение.","bleeding",timestamp,60);
					}
				}
			}
			if(event.getCause() == DamageCause.FALL){
				if(event.getDamage() >= 8){
					long timestamp = System.currentTimeMillis()/1000;
					TerraSMPPlayer.setLegBroken(name, true);
					TerraSMPThread.sendStatusMessage(plr, "§c* Вы сломали ногу.","broken",timestamp,60);
				}
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event){
		TerraSMPPlayer.createPlayer(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent event){
		if(event.getEntity() instanceof Player){
			Player plr = (Player) event.getEntity();
			String name = plr.getName();
			double foodcalories = ((event.getFoodLevel() - plr.getFoodLevel())/2)*350;
			double energy       = TerraSMPPlayer.getEnergy(name) + foodcalories;
			TerraSMPPlayer.setEnergy(name, energy);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		Player plr  		= event.getPlayer();
		String name 		= plr.getName();
		if(event.getAction() == Action.RIGHT_CLICK_AIR){
			ItemStack item 		= plr.getInventory().getItemInHand();
			int amount 			= item.getAmount();
			boolean success 	= false;
			if(item.getType() == Material.STICK && item.getDurability() == 8){
				if(TerraSMPPlayer.isLegBroken(name)){
					plr.sendMessage("§a* Вы наложили шину на сломанную ногу.");
					plr.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 900, 0));
					TerraSMPPlayer.setLegBroken(name, false);
					success = true;
				}
			}
			if(item.getType() == Material.PAPER && item.getDurability() == 8){
				if(TerraSMPPlayer.isBleeding(name)){
					plr.sendMessage("§a* Вы перевязали рану.");
					TerraSMPPlayer.setBleeding(name, false);
					success = true;
				}
			}
			if(success == true){
				if(amount > 1){
					item.setAmount(amount-1);
				}else{
					plr.setItemInHand(null);
				}
			}
		}
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			 double saturation = TerraSMPPlayer.getSaturation(name);
			 double sickness = TerraSMPPlayer.getSickness(name);
			 ItemStack item 		= plr.getInventory().getItemInHand();
			 if(item.getType() == Material.AIR){
				 if(saturation < 3600){
					List<Block> lineOfSight = event.getPlayer().getLineOfSight(null, 5);
					 for (Block b : lineOfSight) {
						 if (b.getType() == Material.WATER) {
								TerraSMPPlayer.setSaturation(name, saturation + 200);
								int addsick = 10 + (int)(Math.random() * ((70 - 10) + 1));
								if(plr.getLocation().getY() < 45){
									addsick = 10 + (int)(Math.random() * ((20 - 10) + 1));
								}
								TerraSMPPlayer.setSickness(name, sickness + addsick);
								plr.sendMessage("§b* Вы выпили немного воды из источника.");
								plr.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1, 2);
								break;
						 }
					 }
				 }
			 }
		}
	}
}
