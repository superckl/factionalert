package me.superckl.factionalert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.superckl.factionalert.commands.AlertsCommand;
import me.superckl.factionalert.commands.FACommand;
import me.superckl.factionalert.commands.ReloadCommand;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.massivecraft.factions.struct.Relation;

public class FactionAlert extends JavaPlugin{


	@Getter
	@Setter(onParam = @_({@NonNull}))
	private String[] configEntries = new String[] {"Teleport", "Move"};
	@Getter
	@Setter(onParam = @_({@NonNull}))
	private Map<String, FACommand> baseCommands = new HashMap<String, FACommand>();
	@Getter
	private Scoreboard scoreboard;
	@Getter
	private FactionListeners listeners;
	@Getter
	private NameplateManager manager;
	@Getter
	private VersionChecker versionChecker;

	@Override
	public void onEnable(){
		this.saveDefaultConfig();
		if(this.getConfig().getBoolean("Version Check")){
			this.getLogger().info("Starting version check...");
			this.versionChecker = VersionChecker.start(0.31d, this);
			this.getServer().getPluginManager().registerEvents(this.versionChecker, this);
		}
		this.getLogger().info("Registering scoreboard");
		if(this.checkScoreboardConflicts(1))
			this.getLogger().warning("Other plugins have registered scoreboards! Conflicts may occur if nameplates are modified.");
		this.scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		this.getLogger().info("Instantiating commands...");
		this.fillCommands();
		this.getLogger().info("Reading configuration...");
		this.readConfig();
		final YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "excludes.yml"));
		if(config != null){
			this.listeners.getDeath().setExcludes(new HashSet<String>(config.getStringList("death")));
			this.listeners.getMove().setExcludes(new HashSet<String>(config.getStringList("move")));
			this.listeners.getTeleport().setExcludes(new HashSet<String>(config.getStringList("teleport")));
		}
		this.getLogger().info("FactionAlert enabled!");
	}


	@Override
	public void onDisable(){
		try {
			this.listeners.saveExcludes(new File(this.getDataFolder(), "excludes.yml"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkScoreboardConflicts(final int tolerance){
		try {
			final Field f = this.getServer().getScoreboardManager().getClass().getDeclaredField("scoreboards");
			if(f == null){
				this.getLogger().warning("Failed to check for scoreboard conflicts!");
				return false;
			}
			f.setAccessible(true);
			final Collection<?> boards = (Collection<?>) f.get(this.getServer().getScoreboardManager());
			if(boards.size() > tolerance)
				return true;
		} catch (final Throwable t) {
			this.getLogger().warning("Failed to check for scoreboard conflicts!");
			t.printStackTrace();
		}
		return false;
	}

	public void fillCommands(){
		this.baseCommands.clear();
		final FACommand[] commands = {new AlertsCommand(this), new ReloadCommand(this)};
		for(final FACommand command:commands)
			for(final String alias:command.getAliases())
				this.baseCommands.put(alias, command);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
		FACommand faCommand;
		if((args.length <= 0) || ((faCommand = this.baseCommands.get(args[0].toLowerCase())) == null)){
			sender.sendMessage(ChatColor.RED+"Invalid arguments");
			return false;
		}
		return faCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	public void readConfig(){
		final FileConfiguration c = this.getConfig();
		final SimpleAlertGroup[] alertGroups = new SimpleAlertGroup[this.configEntries.length];
		for(int i = 0; i < this.configEntries.length; i++){
			final String entry = this.configEntries[i];
			final boolean enabled = c.getBoolean(entry.concat(".Enabled"));
			final List<String> typeStrings = c.getStringList(entry.concat(".Types"));
			final List<Relation> types = new ArrayList<Relation>();
			for(final String typeString:typeStrings){
				final Relation relation = Relation.valueOf(typeString);
				if(relation == null){
					this.getLogger().warning("Failed to read type ".concat(typeString).concat(" for ").concat(entry));
					continue;
				}
				types.add(relation);
			}
			final String enemy = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Enemy Alert Message")));
			final String ally = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Ally Alert Message")));
			final String neutral = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".Neutral Alert Message")));
			final int timeout = c.getInt(entry.concat(".Cooldown"), 0);
			alertGroups[i] = new SimpleAlertGroup(enabled, enemy, ally, neutral, types, timeout, this);
		}
		final boolean enabled = c.getBoolean("Member Death.Enabled");
		final String alert = ChatColor.translateAlternateColorCodes('&', c.getString("Member Death.Member Alert Message"));
		final int timeout = c.getInt("Member Death.Cooldown", 0);
		final FactionSpecificAlertGroup death = new FactionSpecificAlertGroup(enabled, alert, timeout, this);
		this.listeners = new FactionListeners(alertGroups[0], alertGroups[1], death);
		this.getServer().getPluginManager().registerEvents(this.listeners, this);

		final boolean prefix = c.getBoolean("Faction Nameplate.Prefix.Enabled");
		final String prefixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Prefix.Format"));
		final boolean suffix = c.getBoolean("Faction Nameplate.Suffix.Enabled");
		final String suffixFormat = ChatColor.translateAlternateColorCodes('&', c.getString("Faction Nameplate.Suffix.Format"));
		if(suffix || prefix){
			this.manager = new NameplateManager(this.scoreboard, suffix, prefix, suffixFormat, prefixFormat);
			this.getServer().getPluginManager().registerEvents(this.manager, this);
		}
	}

}