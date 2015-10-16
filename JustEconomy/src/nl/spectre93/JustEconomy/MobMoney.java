package nl.spectre93.JustEconomy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class MobMoney implements Listener{
	
private final JustEconomy plugin;

private final File monsterPrices; 
protected static HashMap<String, Integer> mobPriceMap = new HashMap<String, Integer>();
protected static final String[] stringList = { "BAT", "BLAZE", "CAVE_SPIDER",
		"CHICKEN", "COW", "CREEPER", "ENDER_DRAGON", "ENDERMAN", "GHAST",
		"GIANT", "GOLEM", "HORSE", "IRON_GOLEM", "MAGMA_CUBE", "MUSHROOM_COW",
		"OCELOT", "PIG", "PIG_ZOMBIE", "SHEEP", "SILVERFISH", "SKELETON",
		"SLIME", "SNOWMAN", "SPIDER", "SQUID", "VILLAGER", "WITCH",
		"WITHER", "WOLF", "ZOMBIE" };
	
	public MobMoney(Plugin plugin) {
		this.plugin = (JustEconomy) plugin;
		monsterPrices = new File(plugin.getDataFolder() + File.separator + "mobprices.txt");
		initMobMoney();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private void initMobMoney(){		
		if(!monsterPrices.exists()){
			writePrices();
		}
		readPrices();
	}
	
	private void readPrices() {
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		
		try{
		    FileReader readfile = new FileReader(monsterPrices);
		    Scanner sc = new Scanner(readfile);
		    while(sc.hasNext()) temp.put(sc.next(), sc.nextInt());
		    sc.close();
			readfile.close();
		}
		catch (IOException iox){
		    plugin.getLogger().severe("Can't find file: " + monsterPrices);
		}
		mobPriceMap = temp;
	}

	private void writePrices() {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(monsterPrices));
			for(int i = 0; i < stringList.length; i++){
				Integer price = mobPriceMap.get(stringList[i]);
				if(price == null) price = 0;
				out.println(stringList[i] + " " + price);
			}
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public int getKillPrice(String name) {
		int killprice;
		try {
			killprice = mobPriceMap.get(name);
		} catch (Exception e) {
			killprice = 0;
			plugin.getLogger().info(name + " has no price in the config.");
		}
		return killprice;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent evt) {
		Player killer = evt.getEntity().getKiller();
		if(killer == null) return;
		if(killer.getGameMode().equals(GameMode.CREATIVE)) {
			if(!plugin.getConfig().getBoolean("moneyForMobKillInCreativeMode")) return;
		}
			
		String mobName = evt.getEntity().getType().name();
		int price = getKillPrice(mobName);
		killer.sendMessage(ChatColor.BLUE + "Gained " + price + " " + plugin.currencyPlural + " for killing " + mobName + ".");
		if(price != -1)	plugin.adjustMoney(killer, price);
	}
}
