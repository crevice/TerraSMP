package ru.ya1.minecraft.terrasmp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

public class TerraSMPRents {
	private static FileConfiguration db = TerraSMPConfig.getDB();

	public static boolean rentExists(String playername, String subzone){
		if(db.get("rents."+playername+"."+subzone+".active") != null){
			return true;
		}
		return false;
	}
	
	public static void updateRent(String playername, String rentername, String residence, String subzone, long time, boolean active){
		db.set("rents."+playername+"."+subzone+".active", active);
		db.set("rents."+playername+"."+subzone+".resname", residence);
		db.set("rents."+playername+"."+subzone+".time", time);
		db.set("rents."+playername+"."+subzone+".renter", rentername);

	}
	
	public static void setActive(String playername, String subzone, boolean active){
		db.set("rents."+playername+"."+subzone+".active", active);
	}
	
	public static String getResidenceName(String playername, String subzone){
		return db.getString("rents."+playername+"."+subzone+".resname");
	}
	
	public static long getTime (String playername, String subzone){
		return db.getLong("rents."+playername+"."+subzone+".time");
	}
	
	public static String getRenter (String playername, String subzone){
		return db.getString("rents."+playername+"."+subzone+".renter");
	}
	
	public static boolean isActive (String playername, String subzone){
		return db.getBoolean("rents."+playername+"."+subzone+".active");
	}
	
	public static boolean isOutdated (String playername, String subzone){
		long timestamp      = System.currentTimeMillis()/1000;
		long renttime       = db.getLong("rents."+playername+"."+subzone+".time");
		if(timestamp > renttime) return true;
		return false;
	}
	
	public static String getPlayerRentsList(String rentor){
		Set<String> rents = TerraSMPRents.getRentsFromPlayer(rentor);
		String list = "";
		if(rents != null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			list+="§eАренды игрока "+rentor+":§f\n";
			for (String subzone : rents){
				String rentFrom = TerraSMPRents.getRenter(rentor, subzone);
				String resName  = TerraSMPRents.getResidenceName(rentor, subzone);
				System.out.println(resName);
				String color 	= "§a";
				if(!isActive(rentor,subzone)) color="§c";
				long unixLeaseTime = TerraSMPRents.getTime(rentor, subzone);
				Date date = new Date(unixLeaseTime*1000L);
				list+=color+" - "+resName+"."+subzone+" арендован у "+rentFrom+ " до "+sdf.format(date)+"§f\n";
			}
		}else{
			list="§c[rents] Нет активных аренд.";
		}
		return list;
	}
	
	public static Set<String> getRents(){
		return db.getConfigurationSection("rents").getKeys(false);
	}
	public static Set<String> getRentsFromPlayer(String name){
		if(db.getConfigurationSection("rents."+name) != null){
			return db.getConfigurationSection("rents."+name).getKeys(false);
		}
		return null;
	}
}
