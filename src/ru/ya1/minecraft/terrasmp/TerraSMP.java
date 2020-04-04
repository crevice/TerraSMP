package ru.ya1.minecraft.terrasmp;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;

public class TerraSMP extends JavaPlugin{
	public static Plugin plugin;
	public static String plugindir;
	private File dataFolder;
	public void onEnable(){
		plugin = this;
        dataFolder = getDataFolder();
        plugindir = dataFolder.getPath();
        TerraSMPConfig.initDB(dataFolder);
		Bukkit.getServer().getPluginManager().registerEvents(new TerraSMPListener(), this);
        this.getCommand("status").setExecutor(new TerraSMPCommand());
        this.getCommand("rents").setExecutor(new TerraSMPCommand());
        this.getCommand("debug").setExecutor(new TerraSMPCommand());
		TerraSMPThread.runTimer();
		updateRents();
	}
	
	public void onDisable(){
		TerraSMPConfig.saveDB();
	}
	
	//Костыль тысячилетия
	public static boolean ifNPE(String residence, String subzone){
		try{
			ResidenceManager rmanager = Residence.getResidenceManager();
			rmanager.getByName(residence).getSubzone(subzone);
			return false;
		} catch(Exception e){
		    return true;
		}
	}
	
	//Обновляем аренды субзон, должно выполнятся после каждого ребута.
	private static void updateRents(){
		System.out.println("[TerraSMP] Updating rents...");
		long timestamp      = System.currentTimeMillis()/1000;
		int i = 0;
		int j = 0;
		Set<String> rents = TerraSMPRents.getRents();
		for (String name : rents){
			Set<String> residences = TerraSMPRents.getRentsFromPlayer(name);
			for (String subzone : residences){
				if(TerraSMPRents.isActive(name, subzone)){
					if(TerraSMPRents.isOutdated(name, subzone)){
						if(TerraSMPRents.getTime(name, subzone)+86400 < timestamp){
							TerraSMPConfig.getDB().set("rents."+name+"."+subzone,null);
						}else{
							String residence = TerraSMPRents.getResidenceName(name, subzone);
							ResidenceManager rmanager = Residence.getResidenceManager();
							if(!ifNPE(residence,subzone)){
								String realOwner = rmanager.getByName(residence).getPermissions().getOwner();
								rmanager.getByName(residence).getSubzone(subzone).getPermissions().setOwner(realOwner, true);
							}
							TerraSMPRents.setActive(name, subzone, false);
						}
						i++;
					}
				}
				j++;
			}
		}
		System.out.println(" - Total rents: " + j);
		System.out.println(" - Expired today: " + i);
	}
}
