package ru.ya1.minecraft.terrasmp;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

public class TerraSMPThread {
	private static List<Biome> windyBiomes  = Arrays.asList(Biome.OCEAN);
	private static List<Biome> hotBiomes    = Arrays.asList(Biome.DESERT,Biome.DESERT_HILLS,Biome.HELL,Biome.JUNGLE,Biome.JUNGLE_HILLS);
	private static List<Biome> coldBiomes   = Arrays.asList(Biome.TAIGA, Biome.TAIGA_HILLS,Biome.FROZEN_OCEAN,Biome.FROZEN_RIVER,Biome.ICE_MOUNTAINS,Biome.ICE_PLAINS);
	private static List<Material> ironArmor = Arrays.asList(Material.IRON_HELMET,Material.IRON_CHESTPLATE,Material.IRON_LEGGINGS,Material.IRON_BOOTS);
	private static List<Material> diamArmor = Arrays.asList(Material.DIAMOND_HELMET,Material.DIAMOND_CHESTPLATE,Material.DIAMOND_LEGGINGS,Material.DIAMOND_BOOTS);
	private static List<Material> goldArmor = Arrays.asList(Material.GOLD_HELMET,Material.GOLD_CHESTPLATE,Material.GOLD_LEGGINGS,Material.GOLD_BOOTS);
	private static List<Material> leatArmor = Arrays.asList(Material.LEATHER_HELMET,Material.LEATHER_CHESTPLATE,Material.LEATHER_LEGGINGS,Material.LEATHER_BOOTS);
	
	public static void runTimer(){
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(TerraSMP.plugin, new Runnable() {
            @Override
            public void run() {
            	playerTrigger();
            	saveConfig();
            }
        }, 0L, 20L);
	}
	
	private static void saveConfig(){
		long timestamp = System.currentTimeMillis()/1000;
		if(TerraSMPConfig.getLastSaveTime()+3600 < timestamp) TerraSMPConfig.saveDB();
	}
	
	private static void playerTrigger(){
		if(Bukkit.getServer().getOnlinePlayers().length == 0) return;
		Player[] playersOnline = Bukkit.getServer().getOnlinePlayers();
		for(Player player : playersOnline){
			String name = player.getName();
			if(!TerraSMPPlayer.playerExists(name)) TerraSMPPlayer.createPlayer(name);
			if(!(player.getGameMode() == GameMode.CREATIVE)) updateStats(player);
		}
	}
	
	private static void updateStats(Player player){
		String name 			= player.getName();
		Location loc        	= player.getLocation();
		Biome biome         	= player.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
		long timestamp      	= System.currentTimeMillis()/1000;
		float  plrSaturation    = player.getSaturation();
		double plrEnergy 		= TerraSMPPlayer.getEnergy(name);
		double plrSick			= TerraSMPPlayer.getSickness(name);
		double plrWater 		= TerraSMPPlayer.getSaturation(name);
		double plrHeat			= TerraSMPPlayer.getHeat(name);
		double plrHeight    	= loc.getY();
		double biomeTemp   	 	= getBiomeTemperature(biome,plrHeight);
		
		boolean isBleed 		= TerraSMPPlayer.isBleeding(name);
		boolean isBroken		= TerraSMPPlayer.isLegBroken(name);
		
		//Коэффициенты
		double 	sickCoof = 0;
		double  enerCoof = 0;
		double  watrCoof = 1;
		double  armrCoof = getArmorWeight(player);
		double  heatCoof = biomeTemp;
		float   satrCoof = 0;
		
		//Реген ХП от энергии
		if(plrEnergy >= 8000 && plrSick <= 200){
			if(player.getHealth() > 0 && player.getHealth() <20){
				if((TerraSMPPlayer.getLastDmg(name, "regen")+10) < timestamp){
					player.setHealth(player.getHealth()+1);
					enerCoof -= 100;
					satrCoof = 1;
					TerraSMPPlayer.setLastDmg(name, "regen", timestamp);
				}
			}
			sickCoof -=5;
		}
		
		//Перелом ног
		if(isBroken){
			sendStatusMessage(player, "* У вас перелом.","broken",timestamp,120);
			player.setWalkSpeed((float) 0.15);
			if(player.isSprinting()) player.damage(1);
		}else{
			if(player.getWalkSpeed() < 0.2){
				player.setWalkSpeed((float) 0.2);
			}
		}
		
		//Кровотечение
		if(isBleed){
			sendStatusMessage(player, "§c* У вас кровотечение.","bleeding",timestamp,60);
			if((TerraSMPPlayer.getLastDmg(name, "bleeding")+10) < timestamp){
				enerCoof -= 20;
				sickCoof +=1;
				player.damage(2);
				player.setSaturation(plrSaturation-1);
				TerraSMPPlayer.setLastDmg(name, "bleeding", timestamp);
			}
		}
		
		//Смерть от болезней.
		if(plrSick > 180 && plrSick < 300){
			sendStatusMessage(player, "§c* Вы чуствуете себя неважно.","sick",timestamp,120);
			if(TerraSMPPlayer.getLastDmg(name, "sick")+60 < timestamp){
				player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 0));
				TerraSMPPlayer.setLastDmg(name, "sick", timestamp);
			}
			sickCoof +=0.2;
			enerCoof -=5;
			satrCoof+=0.1;
		}else if(plrSick > 300 && plrSick < 500){
			sendStatusMessage(player, "§c* Вы больны.","sick",timestamp,120);
			if(TerraSMPPlayer.getLastDmg(name, "sick")+60 < timestamp){
				player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 900, 0));
				TerraSMPPlayer.setLastDmg(name, "sick", timestamp);
			}
			sickCoof +=0.5;
			enerCoof -=10;
			satrCoof+=0.2;
			watrCoof+=1;
		}else if(plrSick > 500){
			TerraSMPPlayer.setEnergy(name, plrEnergy-20);
			sendStatusMessage(player, "§c* Вы серьёзно больны.","sick",timestamp,120);
			if(TerraSMPPlayer.getLastDmg(name, "sick") +10 < timestamp){
				player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 900, 1));
				TerraSMPPlayer.setLastDmg(name, "sick", timestamp);
				player.damage(1);
			}
			sickCoof +=1;
			enerCoof -=20;
			satrCoof+=0.3;
			watrCoof+=3;
		}

		//Обновление энергии в зависимости от статуса
		if(player.isSprinting()){
			enerCoof-=5;
			satrCoof+=0.25;
		}
		if(player.isSleeping()){
			enerCoof+=10;
			sickCoof -=1;
		}
		if(plrEnergy < 7000 && plrEnergy > 4000){
			sendStatusMessage(player, "§e* Вы чувствуете усталость.","energy",timestamp,240);
		}else if(plrEnergy < 2000){
			sendStatusMessage(player, "§c* Вы сильно устали. Вам нужно отдохнуть.","energy",timestamp,120);
		}
		
		//Обновление температуры
		if(loc.getWorld().isThundering() || loc.getWorld().hasStorm()){
			if(plrHeight > 55) heatCoof-=0.07;
		}
		if(heatCoof == 0 && plrHeat > 100){
			heatCoof=-0.2;
		}else if(heatCoof == 0 && plrHeat < 100){
			heatCoof=0.2;
		}
		
		//Обновление уровня воды
		if(biomeTemp > 0) watrCoof+=2;
		if(plrWater < 1200 && plrWater > 600){
			sendStatusMessage(player, "§e* Вы чувствуете сухость во рту.","water",timestamp,240);
		}else if(plrWater < 600){
			sendStatusMessage(player, "§c* Вы хотите пить. Вам нужно выпить чего-нибудь.","water",timestamp,120);
		}

		//Обновление статуса болезни
		if(plrEnergy <= 500) sickCoof +=0.5;
		if(plrWater <= 200) sickCoof +=0.5;
		if(plrHeat >= 160 || plrHeat <= 40) sickCoof +=0.5;
		
		//Зелья-based коэффициенты
		if(player.hasPotionEffect(PotionEffectType.POISON) || player.hasPotionEffect(PotionEffectType.WITHER)||player.hasPotionEffect(PotionEffectType.HUNGER)) sickCoof +=3;
		if(player.hasPotionEffect(PotionEffectType.NIGHT_VISION) || player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) enerCoof+=5;

		//Броня-based коэффициенты
		if(armrCoof != 0){
			if(armrCoof == 0.2) heatCoof/=2;
			if(armrCoof >= 1){
				enerCoof*=armrCoof;
			}else if(armrCoof <= 1){
				enerCoof/=armrCoof;
			}
		}
		
		//Обновление всех счётчиков
		if(heatCoof != 0) TerraSMPPlayer.setHeat(name, plrHeat+heatCoof);
		if(enerCoof != 0) TerraSMPPlayer.setEnergy(name, plrEnergy+enerCoof);
		if(watrCoof != 0) TerraSMPPlayer.setSaturation(name, plrWater-watrCoof);
		if(heatCoof != 0) TerraSMPPlayer.setSickness(name, plrSick + sickCoof);
		if(satrCoof != 0 && plrSaturation > 0) player.setSaturation(plrSaturation-satrCoof);
	}

	public static double getBiomeTemperature(Biome biome, double height){
		double temperature = 0;
		if(hotBiomes.contains(biome)){
			if(height < 55) return 0;
			temperature=  0.11;
		}else if(coldBiomes.contains(biome)){
			if(height < 45) return 0;
			temperature= -0.11;
		}else if(windyBiomes.contains(biome)){
			temperature= -0.02;
		}
		return temperature;
	}
	
	public static void sendStatusMessage(Player player, String msg, String type, Long timestamp,Integer cooldown){
		String name = player.getName();
		if((TerraSMPPlayer.getLastMsg(name, type)+cooldown) < timestamp){
			player.sendMessage(msg);
			TerraSMPPlayer.setLastMsg(name, type, timestamp);
		}
	}
	
	public static double getArmorWeight(Player plr){
		ItemStack[] armor = plr.getInventory().getArmorContents();
		double weight = 0;
		for(ItemStack item : armor){
			Material type = item.getType();
			if(ironArmor.contains(type)){
				weight+=0.4;
			}else if(diamArmor.contains(type)){
				weight+=0.8;
			}else if(goldArmor.contains(type)){
				weight+=0.2;
			}else if(leatArmor.contains(type)){
				weight+=0.05;
			}
		}
		return weight;
	}
}
