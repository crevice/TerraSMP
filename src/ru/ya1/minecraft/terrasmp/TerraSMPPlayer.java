package ru.ya1.minecraft.terrasmp;

import org.bukkit.configuration.file.FileConfiguration;

public class TerraSMPPlayer {
	private static FileConfiguration db = TerraSMPConfig.getDB();
	
	public static boolean playerExists(String playername){
		if(db.get("players."+playername) != null){
			return true;
		}
		return false;
	}
	
	public static void createPlayer(String playername){
		if(playername != null){
			db.set("players."+playername+".energy", 				10000);
			db.set("players."+playername+".saturation", 			1800);
			db.set("players."+playername+".heat", 					100);
			db.set("players."+playername+".sickness", 				0);
			db.set("players."+playername+".bleeding", 				false);
			db.set("players."+playername+".brokenleg", 			false);
			db.set("players."+playername+".msg.energy",    		0);
			db.set("players."+playername+".msg.saturation",		0);
			db.set("players."+playername+".msg.heat",    			0);
			db.set("players."+playername+".msg.sick",	    		0);
			db.set("players."+playername+".msg.bleeding",  		0);
			db.set("players."+playername+".msg.broken",	  		0);
			db.set("players."+playername+".dmg.bleeding",    		0);
			db.set("players."+playername+".dmg.regen",    			0);
			db.set("players."+playername+".dmg.sick",    			0);
		}
		
	}
	//--------------------GET----------------------//
	public static Double getEnergy(String playername){
		return db.getDouble("players."+playername+".energy");
	}
	
	public static Double getSaturation(String playername){
		return db.getDouble("players."+playername+".saturation");
	}
	
	public static Double getHeat(String playername){
		return db.getDouble("players."+playername+".heat");
	}
	
	public static Double getSickness(String playername){
		return db.getDouble("players."+playername+".sickness");
	}
	
	public static boolean isBleeding(String playername){
		return db.getBoolean("players."+playername+".bleeding");
	}
	
	public static boolean isLegBroken(String playername){
		return db.getBoolean("players."+playername+".brokenleg");
	}
	
	public static long getLastMsg(String playername, String type){
		return db.getLong("players."+playername+".msg."+type);
	}
	
	public static long getLastDmg(String playername, String type){
		return db.getLong("players."+playername+".dmg."+type);
	}
	
	//--------------------SET----------------------//
	public static void setEnergy(String playername,double value){
		if(value > 20000)value=20000;
		if(value < 0) value = 0;
		db.set("players."+playername+".energy", value);
	}
	
	public static void setSaturation(String playername,double value){
		if(value > 3600)value=3600;
		if(value < 0) value = 0;
		db.set("players."+playername+".saturation", value);
	}
	
	public static void setHeat(String playername, double value){
		if(value > 200)value=200;
		if(value < 0) value = 0;
		db.set("players."+playername+".heat", value);
	}
	
	public static void setSickness(String playername, double value){
		if(value > 600)value=600;
		if(value < 0) value = 0;
		db.set("players."+playername+".sickness", value);
	}
	
	public static void setBleeding(String playername, boolean value){
		db.set("players."+playername+".bleeding", value);
	}
	
	public static void setLegBroken(String playername, boolean value){
		db.set("players."+playername+".brokenleg", value);
	}
	
	public static void setLastMsg(String playername, String type, long value){
		db.set("players."+playername+".msg."+type, value);
	}
	
	public static void setLastDmg(String playername, String type, long value){
		db.set("players."+playername+".dmg."+type, value);
	}
	
	public static String getStatusColor(double percent){
		String color = "§7";
		if(percent >= 66){
			color = "§a";
		}else if(percent >= 33){
			color = "§e";
		}else{
			color = "§c";
		}
		return color;
	}
	
	public static String getStatusBar(double max, double value, String type){
		double percent = value * 100 / max;
		String color = "";
		String percentbar = "";	
		switch(type){
			case("positive"):
				for(int i=0; i<=percent; i++){
					if(i > 0) percentbar+="|";
				}
			color = getStatusColor(percent);
			break;
			case("negative"):
				percent = 100 - percent;
				for(int i=0; i<=percent; i++){
					if(i > 0) percentbar+="|";
				}
				color = getStatusColor(percent);
			break;
			case("temperature"):
				if(percent > 60){
					color = "§c";
				}else if(percent < 40){
					color = "§7";
				}else{
					color = "§a";
				}
				for(int i=0; i<=percent; i++){
					if(i > 0) percentbar+="|";
				}
			break;
		
		}
		return color+percentbar;
	}
}
