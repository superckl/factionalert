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
		this.getServer().getPluginManager().registerEvents(this.readConfig(), this);
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
		this.getServer().getPluginManager().registerEvents(this.readConfig(), this);
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}
	
	public FactionListeners readConfig(){
		FileConfiguration c = this.getConfig();
		SimpleAlertGroup[] alertGroups = new SimpleAlertGroup[this.configEntries.length];
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
			alertGroups[i] = new SimpleAlertGroup(enabled, enemy, ally, neutral, truce, types, receivers);
		}
		boolean enabled = c.getBoolean("Member Death.Enabled");
		List<String> receiverStrings = c.getStringList("Member Death.Receivers");
		List<Rel> receivers = new ArrayList<Rel>();
		for(String receiverString:receiverStrings){
			Rel relation = Rel.valueOf(receiverString);
			if(relation == null){
				this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat("Member Death"));
				continue;
			}
			receivers.add(relation);
		}
		String leader = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Leader Alert Message"));
		String officer = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Officer Alert Message"));
		String member = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Member Alert Message"));
		String recruit = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Recruit Alert Message"));
		FactionSpecificAlertGroup death = new FactionSpecificAlertGroup(enabled, leader, officer, recruit, member, receivers);
		return new FactionListeners(alertGroups[0], alertGroups[1], death);
	}
	
}