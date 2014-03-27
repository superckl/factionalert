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
import me.superckl.factionalert.commands.AlertsCommandInjection;
import me.superckl.factionalert.commands.FACommand;
import me.superckl.factionalert.commands.ReloadCommand;
import me.superckl.factionalert.commands.SaveCommand;
import me.superckl.factionalert.groups.FactionSpecificAlertGroup;
import me.superckl.factionalert.groups.SimpleAlertGroup;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;

public class FactionAlert extends JavaPlugin{

	@Getter
	@Setter(onParam = @_({@NonNull}))
	private String[] configEntries = new String[] {"Teleport", "Move", "Combat"};
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
	private boolean cmdInjected = false;

	@Override
	public void onEnable(){
		this.saveDefaultConfig();
		if(this.getConfig().getBoolean("Version Check")){
			this.getLogger().info("Starting version check...");
			this.versionChecker = VersionChecker.start(0.4d, this);
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
		final YamlConfiguration excludes = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "excludes.yml"));
		if(excludes != null){
			this.listeners.getDeath().setExcludes(new HashSet<String>(excludes.getStringList("death")));
			this.listeners.getMove().setExcludes(new HashSet<String>(excludes.getStringList("move")));
			this.listeners.getTeleport().setExcludes(new HashSet<String>(excludes.getStringList("teleport")));
			this.listeners.getCombat().setExcludes(new HashSet<String>(excludes.getStringList("combat")));
		}
		this.getLogger().info("FactionAlert enabled!");
	}

	@Override
	public void onDisable(){
		try {
			if(this.listeners != null)
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
		final FACommand[] commands = {new AlertsCommand(this), new ReloadCommand(this), new SaveCommand(this)};
		for(final FACommand command:commands)
			for(final String alias:command.getAliases())
				this.baseCommands.put(alias, command);
		if(this.cmdInjected)
			return;
		this.getLogger().info("Injecting alerts command...");
		final Factions f = (Factions) this.getServer().getPluginManager().getPlugin("Factions");
		if(f == null){
			this.getLogger().severe("Factions doesn't exist but passed the dependency??? What kind of rig are you running here?");
			return;
		}
		f.getOuterCmdFactions().addSubCommand(new AlertsCommandInjection((AlertsCommand) commands[0]));
		this.cmdInjected = true;
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
			final List<Rel> types = new ArrayList<Rel>();
			for(final String typeString:typeStrings){
				final Rel relation = typeString.equalsIgnoreCase("none") ? null:Rel.valueOf(typeString);
				if((relation == null) && !typeString.equalsIgnoreCase("none")){
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
			final String none = ChatColor.translateAlternateColorCodes('&', c.getString(entry.concat(".None Alert Message")));
			final int timeout = c.getInt(entry.concat(".Cooldown"), 0);
			alertGroups[i] = new SimpleAlertGroup(enabled, enemy, ally, neutral, truce, none, types, receivers, timeout, this);
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
		final int timeout = c.getInt("Member Death.Cooldown", 0);
		final FactionSpecificAlertGroup death = new FactionSpecificAlertGroup(enabled, leader, officer, recruit, member, receivers, timeout, this);
		this.listeners = new FactionListeners(alertGroups[0], alertGroups[1], alertGroups[2], death);
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