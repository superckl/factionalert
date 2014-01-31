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
		this.readConfig();
		this.getLogger().info("FactionAlert enabled!");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
		if(!sender.hasPermission("factionalert.manage")){
			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
			return false;
		}
		if((args.length != 1) || !args[0].equalsIgnoreCase("reload")){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		this.reloadConfig();
		HandlerList.unregisterAll(this);
		this.readConfig();
		sender.sendMessage(ChatColor.GREEN+"FactionAlert reloaded.");
		return true;
	}

	public void readConfig(){
		final FileConfiguration c = this.getConfig();
		final SimpleAlertGroup[] alertGroups = new SimpleAlertGroup[this.configEntries.length];
		for(int i = 0; i < this.configEntries.length; i++){
			final String entry = this.configEntries[i];
			final boolean enabled = c.getBoolean(entry.concat(".Enabled"));
			final List<String> typeStrings = c.getStringList(entry.concat(".Types"));
			final List<Rel> types = new ArrayList<Rel>();
			for(final String typeString:typeStrings){
				final Rel relation = Rel.valueOf(typeString);
				if(relation == null){
					this.getLogger().warning("Failed to read type ".concat(typeString).concat(" for ").concat(entry));
					continue;
				}
				types.add(relation);
			}
			final List<String> receiverStrings = c.getStringList(entry.concat(".Receivers"));
			final List<Rel> receivers = new ArrayList<Rel>();
			for(final String receiverString:receiverStrings){
				final Rel relation = Rel.valueOf(receiverString);
				if(relation == null){
					this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat(entry));
					continue;
				}
				receivers.add(relation);
			}
			final String enemy = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Enemy Alert Message")));
			final String ally = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Ally Alert Message")));
			final String neutral = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Neutral Alert Message")));
			final String truce = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Truce Alert Message")));
			alertGroups[i] = new SimpleAlertGroup(enabled, enemy, ally, neutral, truce, types, receivers);
		}
		final boolean enabled = c.getBoolean("Member Death.Enabled");
		final List<String> receiverStrings = c.getStringList("Member Death.Receivers");
		final List<Rel> receivers = new ArrayList<Rel>();
		for(final String receiverString:receiverStrings){
			final Rel relation = Rel.valueOf(receiverString);
			if(relation == null){
				this.getLogger().warning("Failed to read receiver ".concat(receiverString).concat(" for ").concat("Member Death"));
				continue;
			}
			receivers.add(relation);
		}
		final String leader = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Leader Alert Message"));
		final String officer = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Officer Alert Message"));
		final String member = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Member Alert Message"));
		final String recruit = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Recruit Alert Message"));
		final FactionSpecificAlertGroup death = new FactionSpecificAlertGroup(enabled, leader, officer, recruit, member, receivers);
		this.getServer().getPluginManager().registerEvents(new FactionListeners(alertGroups[0], alertGroups[1], death), this);

		final boolean prefix = c.getBoolean("Faction Nameplate.Prefix.Enabled");
		final String prefixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Prefix.Format"));
		final boolean suffix = c.getBoolean("Faction Nameplate.Suffix.Enabled");
		final String suffixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Suffix.Format"));
		if(suffix || prefix)
			this.getServer().getPluginManager().registerEvents(new NameplateManager(this.getServer().getScoreboardManager().getNewScoreboard(), suffix, prefix, suffixFormat, prefixFormat), this);
	}

}