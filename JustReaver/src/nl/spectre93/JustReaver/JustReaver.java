package nl.spectre93.JustReaver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class JustReaver extends JavaPlugin implements Listener{
	
	protected static Logger log = Bukkit.getLogger();
	File openRewards = new File(getDataFolder() + File.separator + "openRewards.dat");
	File redeemers = new File(getDataFolder() + File.separator + "redeemers.dat");
	File configFile = new File(getDataFolder() + File.separator + "config.yml");
	
	public void onEnable(){			
		setupConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){ 
		HandlerList.unregisterAll(); 
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(final PlayerLoginEvent evt){
		final int rew = getRewardsOpen(evt.getPlayer().getUniqueId());
		if(rew <= 0) return;
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				if(rew == 1){
					evt.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You have " + rew + " reward waiting for you. Use /refreward to claim it!");
				}else{
					evt.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You have " + rew + " rewards waiting for you. Use /refreward to claim them!");
				}
			}
		}, 1L);
		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Only players can use this command.");
			return false;
		}
		
		Player p = (Player)sender;
		if (command.getName().equalsIgnoreCase("refget")){
			p.sendMessage("Your unique code: " + ChatColor.GOLD + p.getUniqueId());
			if(getRewardsOpen(p.getUniqueId()) == -1)
				setRewardsOpen(p.getUniqueId(), 0);
			return true;
		}else if (command.getName().equalsIgnoreCase("refcredit")){
			if(args.length != 1) return false;
			UUID id = null;
			try {
				id = UUID.fromString(args[0]);
			} catch (Exception e) {
				p.sendMessage(ChatColor.RED + "Not a valid code!");
				return true;
			}
			
			if(getRedeemedBefore(p.getUniqueId()))
				p.sendMessage(ChatColor.RED + "You've already credited someone for referring you.");
			else if(p.getUniqueId().equals(hasRecruited(id)))
				p.sendMessage(ChatColor.RED + "That's the person who referred you!");
			else if(p.getUniqueId().equals(id))
				p.sendMessage(ChatColor.RED + "Haha nice try, but you can't refer yourself.");
			else if(getRewardsOpen(id) == -1)
				p.sendMessage(ChatColor.RED + "Either you've mistyped something, or the player with that UUID never requested it on this server.");
			else{
				addPersonWhoRedeemed(p.getUniqueId(), id);
				p.sendMessage(ChatColor.AQUA + "Thanks for using the referral system! Find your own unique code by typing /refget!");
				setRewardsOpen(id, getRewardsOpen(id) + 1);  //Give the player te right to claim a reward	
				
				/* TODO 1.7.9
				Player credited = getServer().getPlayer(id); //Send a message if the player is online
				if(credited != null)
					credited.sendMessage(ChatColor.LIGHT_PURPLE + "Someone just gave you credit for recruiting them!\n" +
															"Use /refreward to claim your reward(s)!");
				*/	
			}
			return true;
		}else if(command.getName().equalsIgnoreCase("refreward")){
			if(getRewardsOpen(p.getUniqueId()) < 1)
				p.sendMessage(ChatColor.LIGHT_PURPLE + "You don't have any rewards to be collected.");
			else
				reward(p);
			return true;
		}	
		return false;
	}
	
	private void reward(Player p){
		Material m = Material.matchMaterial(getConfig().getString("reward"));
		if(m == null){
			p.sendMessage(ChatColor.RED + "The reward is not correctly specified! Please contact your server administrator.");
			return;
		}

		if(p.getInventory().firstEmpty() == -1){
			p.sendMessage(ChatColor.RED + "Please make sure you have at least ONE free inventory space.");
			return;
		}
		int rewOpen = getRewardsOpen(p.getUniqueId());
		setRewardsOpen(p.getUniqueId(), rewOpen - 1);
		p.getInventory().addItem(new ItemStack(m));
		p.sendMessage(ChatColor.GOLD + "Congratz! One " + m.name() + " has been added to your inventory!");
		p.sendMessage(ChatColor.LIGHT_PURPLE + "You have " + (rewOpen - 1) + " rewards left to be claimed.");
	}
	
	private void setupConfig(){ 		
		if(!(configFile).exists()){
			getConfig().options().header("JustReaver, Made by Spectre93.");
			saveConfig();
		}
	
		if(!getConfig().contains("reward")) {
			getConfig().set("reward", "Diamond"); 
			saveConfig();
		}
		
		if(!openRewards.exists()){
			writeOpenRewardsMap(new HashMap<UUID, Integer>());
			
		}
		
		if(!redeemers.exists()){
			writeRedeemMap(new HashMap<UUID, UUID>());
		}
	}
	
	public int getRewardsOpen(UUID id){
		Integer res = readOpenRewardsMap().get(id);
		if(res == null)
			res = -1;
		return res;
	}
	
	private void setRewardsOpen(UUID id, int number){
		HashMap<UUID, Integer> map = readOpenRewardsMap();
		map.put(id, number);
		writeOpenRewardsMap(map);
	}
	
	private void addPersonWhoRedeemed(UUID recruiter, UUID recruit) {
		HashMap<UUID, UUID> map = readRedeemMap();
		map.put(recruiter, recruit);
		writeRedeemMap(map);
	}
	
	private boolean getRedeemedBefore(UUID id){
		return readRedeemMap().containsKey(id);
	}
	
	private UUID hasRecruited(UUID id){
		return readRedeemMap().get(id);
	}
	
	
	private HashMap<UUID, Integer> readOpenRewardsMap(){
		HashMap<UUID, Integer> temp = new HashMap<UUID, Integer>();
		
		try{
		    FileReader readfile = new FileReader(openRewards);
		    Scanner sc = new Scanner(readfile);
		    
		    while(sc.hasNext()){
		    	temp.put(UUID.fromString(sc.next()), sc.nextInt());
		    }
		    sc.close();
			readfile.close();
		}
		catch (IOException iox){
		    log.severe("Can't find file: " + openRewards);
		}
		return temp;
	}
	
	private HashMap<UUID, UUID> readRedeemMap(){
		HashMap<UUID, UUID> temp = new HashMap<UUID, UUID>();
		
		try{
		    FileReader readfile = new FileReader(redeemers);
		    Scanner sc = new Scanner(readfile);
		    
		    while(sc.hasNext()){
		    	temp.put(UUID.fromString(sc.next()), UUID.fromString(sc.next()));
		    }
	
		    sc.close();
			readfile.close();
		}
		catch (IOException iox){
		    log.severe("Can't find file: " + redeemers);
		}
		return temp;
	}
	
	synchronized private void writeRedeemMap(HashMap<UUID, UUID> map){
		try {
			PrintWriter out = new PrintWriter(new FileWriter(redeemers));
			
			for(UUID id : map.keySet()){
				out.println(id + " " + map.get(id));
			}
			
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	synchronized private void writeOpenRewardsMap(HashMap<UUID, Integer> map){
		try {
			PrintWriter out = new PrintWriter(new FileWriter(openRewards));
			
			for(UUID id : map.keySet()){
				out.println(id + " " + map.get(id));
			}
			
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
