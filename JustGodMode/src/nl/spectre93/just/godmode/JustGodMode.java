package nl.spectre93.just.godmode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class JustGodMode extends JavaPlugin implements Listener{
	
	private static Map<String, UUID> UUIDmap;

	public void onEnable() {	
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public synchronized Player getPlayer(final String s) throws NullPointerException{
		getServer().getScheduler().runTask(this, new Runnable() {
			public void run() {
				UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(s));
				try {
					UUIDmap = fetcher.call();
				} catch (Exception e) {	}
			}
		});
		return getServer().getPlayer(UUIDmap.get(s));		
	}
    
    public void onDisable() {
		HandlerList.unregisterAll();
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, final String[] args) {
    	if (command.getName().equalsIgnoreCase("god")) {
    		if(args.length > 1) {
    			sender.sendMessage("Correct usage:");
    			return false;
    		}
    		if(args.length == 1){
    			if(sender.hasPermission("just.god.other")){						
						try{	
							toggleGodMode(getPlayer(args[0]));
						} catch(Exception e){
							sender.sendMessage(ChatColor.RED + "Can't find " + args[0] + ".");
						}
	    			return true;
    			}else sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
    		}
    		else if(sender instanceof Player){
    			Player player = (Player)sender;
    			if(player.hasPermission("just.god")){
    				toggleGodMode(player);
    			}else player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
    		}else sender.sendMessage(ChatColor.RED + "This command has to be called by a player!");
    	}
    	return true;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent evt){
    	UUIDmap.put(evt.getPlayer().getName(), evt.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDamage(EntityDamageEvent evt) {
		if(evt.getEntity() instanceof Player){
			Player player = (Player) evt.getEntity();
			if(isGod(player)){
	    		evt.setCancelled(true);
				player.setFireTicks(0);
				player.removePotionEffect(PotionEffectType.BLINDNESS);
				player.removePotionEffect(PotionEffectType.CONFUSION);
				player.removePotionEffect(PotionEffectType.POISON);
				player.removePotionEffect(PotionEffectType.WITHER);
				player.removePotionEffect(PotionEffectType.WEAKNESS);
				player.removePotionEffect(PotionEffectType.SLOW);
				getLogger().finest("[" + player.getDisplayName() + "] Damage by " + evt.getCause().name() + " cancelled.");
			}
		}
    }

	public boolean isGod(Player player){
		return getMetadata(player, "Godmode");
    }
    
    public void toggleGodMode(Player player){
    	setMetadata(player, "Godmode", !isGod(player));
		getLogger().fine("[" + player.getDisplayName() + "] Toggled godmode " + isGod(player));
		if(isGod(player)) player.sendMessage(ChatColor.GOLD + "Godmode activated!");
		else player.sendMessage("Godmode deactivated.");
    }
    
    public void setMetadata(Player player, String key, Object value){
		  player.setMetadata(key,new FixedMetadataValue(this,value));
	}
	
	public Boolean getMetadata(Player player, String key){
		List<MetadataValue> values = player.getMetadata(key);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
				if(value.value() != null){
					return value.asBoolean();
				}else{
					return false;
				}
			}
		}
		return false;
	}
}