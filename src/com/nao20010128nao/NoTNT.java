package com.nao20010128nao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class NoTNT extends JavaPlugin implements Listener {
	public static final int HASH_BAN_TNT = "banTNT".hashCode();
	public static final int HASH_DELETE_TNT = "deleteTNTs".hashCode();
	NoTNTConfig config;

	public NoTNT() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public NoTNT(JavaPluginLoader loader, PluginDescriptionFile description,
			File dataFolder, File file) {
		super(loader, description, dataFolder, file);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public void onEnable() {
		// TODO 自動生成されたメソッド・スタブ
		getDataFolder().mkdir();// @mkdir(getDataFolder());
		getServer().getPluginManager().registerEvents(this, this);// getServer().getPluginManager().registerEvents(this,
																	// this);
		getLogger().info(
				"Server class name: " + getServer().getClass().getName());
		getLogger().info(
				"PluginManager class name: "
						+ getServer().getPluginManager().getClass().getName());
		/*
		 * if(file_exists(getDataFolder()."/config.yml")){
		 * config=yaml_parse_file(getDataFolder()."/config.yml"); }else{
		 * config=array("banTNT"=>true,"deleteTNTs"=>true); }
		 */
		config = new NoTNTConfig();
		if (new File(getDataFolder(), "config.yml").exists()) {
			config.loadBukkitFormat(new File(getDataFolder(), "config.yml"));
		}
	}

	@Override
	public void onDisable() {
		// TODO 自動生成されたメソッド・スタブ
		config.saveBukkitFormat(new File(getDataFolder(), "config.yml"));
	}

	class NoTNTConfig {
		boolean banTNT = true, deleteTNTs = true;

		public void loadBukkitFormat(File file) {
			if (!file.exists())
				return;
			BufferedReader load = null;
			try {
				load = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = load.readLine()) != null) {
					String[] d = line.split("\\:");
					d[0] = d[0].replace(" ", "");
					try {
						switch (d[0]) {
						case "banTNT":
							banTNT = Boolean.parseBoolean(d[1]);
							break;
						case "deleteTNTs":
							deleteTNTs = Boolean.parseBoolean(d[1]);
							break;
						}
					} catch (Exception e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} finally {
				try {
					load.close();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}

		public void saveBukkitFormat(File file) {
			FileWriter save = null;
			try {
				save = new FileWriter(file);
				save.write("banTNT:" + banTNT + "\n");
				save.write("deleteTNTs:" + deleteTNTs + "\n");
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} finally {
				try {
					save.close();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
	}

	/* Events */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		String username = player.getName();
		if (config.banTNT & event.getBlock().getType() == Material.TNT) {
			event.setCancelled(true);
			getLogger().info("[NoTNT] §cTNT has placed by " + username);
			player.sendMessage("§cYou can't place TNTs!");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (config.deleteTNTs & event.getBlock().getType() == Material.TNT) {
			getLogger().info(
					"[NoTNT] §aA TNT was broken. Deleting TNTs around it...");
			player.sendMessage("§aYou broke a TNT. Deleting TNTs around it...");
			removeTNTrescursive(player.getEyeLocation().getWorld().getName(),
					block.getX(), block.getY(), block.getZ(), 0);
			getLogger().info("[NoTNT] §aComplete!");
			player.sendMessage("§aComplete!");
		}
	}

	public void removeTNTrescursive(String worldName, int x, int y, int z,
			int nest) {
		World world = getServer().getWorld(worldName);
		if (world == null) {
			return;
		}
		Location vector = new Location(world, x, y, z);
		Block block = vector.getBlock();
		if (block.getType() == Material.TNT) {
			block.setType(Material.AIR);
		} else {
			return;
		}
		if (nest >= 40) {
			return;
		}

		for (int a = -5; a < 5; a++) {
			for (int b = -5; b < 5; b++) {
				for (int c = -5; c < 5; c++) {
					removeTNTrescursive(worldName, x + a, y + b, z + c,
							nest + 1);
				}
			}
		}
	}

	/* Commands Part */
	private boolean checkPerm(CommandSender sender) {
		if (!(sender.isOp() | sender.hasPermission("nt.commmand"))) {
			sender.sendMessage("§cYou don't have permission to use this command.");
			return false;
		}
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		if ("tnt".equals(label)) {
			return tntCmd(sender, label, args);
		} else if ("deltnt".equals(label)) {
			return deltntCmd(sender, label, args);
		} else {
			return false;
		}

	}

	public boolean tntCmd(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			if (!checkPerm(sender))
				return true;
			sender.sendMessage("§a[NoTNT] Usage: /tnt <on|off|true|false|enable|disable>");
			return false;
		}
		switch (args[0]) {
		case "on":
		case "true":
		case "enable":
			config.banTNT = true;
			sender.sendMessage("§aNoTNT has enabled!");
			break;
		case "off":
		case "false":
		case "disable":
			config.banTNT = false;
			sender.sendMessage("§cNoTNT has disabled!");
			break;
		default:
			sender.sendMessage("§a[NoTNT] Usage: /tnt <on|off|true|false|enable|disable>");
			break;
		}
		return true;
	}

	public boolean deltntCmd(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			if (!checkPerm(sender))
				return true;
			sender.sendMessage("§a[NoTNT] Usage: /del <on|off|true|false|enable|disable>");
			return false;
		}
		switch (args[0]) {
		case "on":
		case "true":
		case "enable":
			config.deleteTNTs = true;
			sender.sendMessage("§aNoTNT can delete TNTs using TNT removing engine!");
			break;
		case "off":
		case "false":
		case "disable":
			config.deleteTNTs = false;
			sender.sendMessage("§cNoTNT can't delete TNTs using TNT removing engine!");
			break;
		default:
			sender.sendMessage("§a[NoTNT] Usage: /deltnt <on|off|true|false|enable|disable>");
			break;
		}
		return true;
	}
}
