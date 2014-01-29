package me.superckl.factionalert;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Rel;

public class FactionAlert extends JavaPlugin{
	
	private final String[] configEntries = new String[] {"Teleport", "Move"};
	
	@Override
	public void onEnable(){
		this.saveDefaultConfig();
		AlertGroup[] groups = this.readConfig();
		this.getServer().getPluginManager().registerEvents(new FactionListeners(groups[0], groups[1]), this);
		this.getLogger().info("FactionAlert enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(!sender.hasPermission("factionalert.manage")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		if(args.length != 1 || !args[0].equalsIgnoreCase("reload")){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		this.reloadConfig();
		HandlerList.unregisterAll(this);
		AlertGroup[] groups = this.readConfig();
		this.getServer().getPluginManager().registerEvents(new FactionListeners(groups[0], groups[1]), this);
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}
	
	public AlertGroup[] readConfig(){
		FileConfiguration c = this.getConfig();
		AlertGroup[] alertGroups = new AlertGroup[this.configEntries.length];
		for(int i = 0; i < this.configEntries.length; i++){
			String entry = this.configEntries[i];
			boolean enabled = c.getBoolean(entry.concat(".Enabled"));
			List<String> typeStrings = c.getStringList(entry.concat(".Types"));
			List<Rel> types = new ArrayList<Rel>();
			for(String typeString:typeStrings){
				Rel relation = Rel.valueOf(typeString);
				if(relation == null){
					this.getLogger().warning("Failed to read type ".concat(typeString).concat(" for ").concat(entry));
					continue;
				}
				types.add(relation);
			}
			List<String> receiverStrings = c.getStringList(entry.concat(".Receivers"));
			List<Rel> receivers = new ArrayList<Rel>();
			for(String receiverString:receiverStrings){
				Rel relation = Rel.valueOf(receiverString);
				if(relation == null){
					this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat(entry));
					continue;
				}
				receivers.add(relation);
			}
			String enemy = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Enemy Alert Message")));
			String ally = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Ally Alert Message")));
			String neutral = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Neutral Alert Message")));
			String truce = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Truce Alert Message")));
			alertGroups[i] = new AlertGroup(enabled, enemy, ally, neutral, truce, types, receivers);
		}
		return alertGroups;
	}
	
}