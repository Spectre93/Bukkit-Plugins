package nl.spectre93.just.commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class JustCommands extends JavaPlugin implements Listener{
	
	final JustCommands plugin;
	
	public JustCommands(){
		plugin = this;
	}

	public void onEnable(){		
		getServer().getPluginManager().registerEvents(this, this);
		
//		String configFile = getDataFolder() + File.separator + "config.yml"; 
//		
//		if(!(new File(configFile)).exists()){
//			getConfig().options().header("JustSayNote, Made by Spectre93.");
//			this.saveConfig();
//		}
		
	}
	
	public void onDisable(){
		HandlerList.unregisterAll();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(command.getName().equalsIgnoreCase("rain")) arrowRain((Player)sender);
		if(command.getName().equalsIgnoreCase("explo")) explosion((Player)sender);
		if(command.getName().equalsIgnoreCase("magnet")) magnet((Player)sender, 50);
		if(command.getName().equalsIgnoreCase("cle")) clearEntities((Player)sender, 50);
		return true;
	}
	
	private void magnet(final Player player, final int radius){
		getServer().getScheduler().runTask(this, new BukkitRunnable(){			
			@Override
			public void run(){
				List<Entity> list = player.getNearbyEntities(radius, radius, radius);
				for(Entity e : list){
					if(e.getType().equals(EntityType.DROPPED_ITEM)){
						e.setVelocity(player.getLocation().subtract(e.getLocation()).multiply(0.5).toVector());
					}
				}
			}
		});
	}
	
	private void clearEntities(Player player, int radius){
		Iterator<Entity> it = player.getNearbyEntities(radius, radius, radius).listIterator();
		while(it.hasNext()){
			Entity t = it.next();
			if(!(t instanceof Player)) t.remove();
		}
	}
	
	private void explosion(Player player) {
		player.getWorld().createExplosion(player.getLocation(), 10);
	}

	private void arrowRain(Player player) {	
		for(int i = 0; i < 100; i++) player.getWorld().spawnArrow(player.getLocation().getBlock()
				.getLocation().add(new Vector(0, 50, 0)), new Vector(0, -1, 0), 1F, 10F);		
	}		
}
