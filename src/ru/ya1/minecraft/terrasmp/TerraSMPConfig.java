package ru.ya1.minecraft.terrasmp;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class TerraSMPConfig {
	private static FileConfiguration database = null;
	private static File databaseFile = null;
	public static void initDB(File dataFolder){
	    if (databaseFile == null){
	    	databaseFile = new File(dataFolder, "config.yml");
	    }
		database = YamlConfiguration.loadConfiguration(databaseFile);
	}
	
	public static FileConfiguration getDB(){
		return database;
	}
	
	public static long getLastSaveTime(){
		return database.getLong("saves.lastsave");
	}
	
	public static void updateLastSaveTime(){
		long timestamp = System.currentTimeMillis()/1000;
		database.set("saves.lastsave",timestamp);
	}
	
	public static void saveDB(){
	    if (database == null || databaseFile == null) {
	        return;
	    }
	    try {
	        updateLastSaveTime();
	        getDB().save(databaseFile);
	        System.out.println("[TerraSMP] Saving config... OK");
	    } catch (IOException ignored) {
	        System.out.println("[TerraSMP] Saving config... FAIL");
	    }
	}
}
