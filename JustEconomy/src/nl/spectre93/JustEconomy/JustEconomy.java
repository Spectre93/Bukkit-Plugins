package nl.spectre93.JustEconomy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class JustEconomy extends JavaPlugin {
	
	
	private File playerMoneyFile = new File(getDataFolder() + File.separator + "playerMoney.dat");
	private File configFile = new File(getDataFolder() + File.separator + "config.yml");
	protected static HashMap<UUID, Integer> playerMoneyMap = new HashMap<UUID, Integer>();
	private int minimum;
	public String currencyPlural = "euros"; 

	public void onEnable() {
		initConfig();
		if(getConfig().getBoolean("monster-money")) new MobMoney(this);
		if(getConfig().getBoolean("shopsigns")) new ShopSigns(this);
	}

	public void onDisable() {
		writePlayerMoneyMap(playerMoneyMap);
		HandlerList.unregisterAll();
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		//balance/money
		return true;
	}
	
	public boolean hasEnoughMoney(Player p, int amount){
		if(amount == 0) return true;
		Integer oldAmount = playerMoneyMap.get(p.getUniqueId());
		if(oldAmount == null) oldAmount = 0;
		boolean hasEnough = (oldAmount - amount >= minimum);
		if(!hasEnough)
			p.sendMessage(ChatColor.BLUE + "You don't have enough money for that.");
		return hasEnough;
	}

	public void adjustMoney(Player p, int amount) {
		if(amount == 0) return;
		Integer oldAmount = playerMoneyMap.get(p.getUniqueId());
		if(oldAmount == null) oldAmount = 0;
		int newAmount = oldAmount + amount;
		playerMoneyMap.put(p.getUniqueId(), newAmount);
		p.sendMessage(ChatColor.BLUE + "New balance: " + newAmount + ".");
	}

	private void initConfig() {
		if (!configFile.exists()) {
			getConfig().options().header("Config file for the plugin JustEconomy - made by Spectre93.\n");
			saveConfig();
		}
		
		if(!getConfig().contains("monster-money")) {getConfig().set("monster-money", true); saveConfig();}
		if(!getConfig().contains("moneyForMobKillInCreativeMode")) {getConfig().set("moneyForMobKillInCreativeMode", false); saveConfig();}
		if(!getConfig().contains("shopsigns")) {getConfig().set("shopsigns", true); saveConfig();}
		if(!getConfig().contains("minimum-money")) {getConfig().set("minimum-money", -100); saveConfig();}
		minimum = getConfig().getInt("minimum-money");
		
		
		if(!playerMoneyFile.exists()){
			writePlayerMoneyMap(new HashMap<UUID, Integer>());		
		}
		
		playerMoneyMap = readPlayerMoneyMap();
	}	
	
	private HashMap<UUID, Integer> readPlayerMoneyMap(){
		HashMap<UUID, Integer> temp = new HashMap<UUID, Integer>();
		
		try{
		    FileReader readfile = new FileReader(playerMoneyFile);
		    Scanner sc = new Scanner(readfile);
		    
		    while(sc.hasNext()){
		    	temp.put(UUID.fromString(sc.next()), sc.nextInt());
		    }
		    sc.close();
			readfile.close();
		}
		catch (IOException iox){
		    getLogger().severe("Can't find file: " + playerMoneyFile);
		}
		return temp;
	}
	
	synchronized private void writePlayerMoneyMap(HashMap<UUID, Integer> map){
		try {
			PrintWriter out = new PrintWriter(new FileWriter(playerMoneyFile));
			
			for(UUID id : map.keySet()){
				out.println(id + " " + map.get(id));
			}
			
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
