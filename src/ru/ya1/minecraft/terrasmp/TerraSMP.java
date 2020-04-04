package ru.ya1.minecraft.terrasmp;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
	}
	
	public void onDisable(){
		TerraSMPConfig.saveDB();
	}
}
