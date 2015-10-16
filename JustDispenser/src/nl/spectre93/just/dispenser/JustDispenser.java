package nl.spectre93.just.dispenser;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JustDispenser extends JavaPlugin implements Listener{

	public void onEnable(){				
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable(){
		HandlerList.unregisterAll();
	}
	
	@EventHandler
	public void onProjectileLaunchEvent(ProjectileLaunchEvent evt){	
		
		Location loc = evt.getEntity().getLocation();
		Location temp = evt.getEntity().getLocation();
		
		double x = evt.getEntity().getVelocity().toBlockVector().getX();
		double z = evt.getEntity().getVelocity().toBlockVector().getZ();
				
		if(Math.abs(x) < Math.abs(z)){
			if(z < 0){
				temp.setZ(temp.getZ() + 1);
			}else{
				temp.setZ(temp.getZ() - 1);
			}
		}else{
			if(x < 0){
				temp.setX(temp.getX() + 1);
			}else{
				temp.setX(temp.getX() - 1);
			}
		}
		
		if(!temp.getWorld().getBlockAt(temp).getType().equals(Material.DISPENSER)){
			return;
		}
			
		if(evt.getEntity().getWorld().getBlockAt(loc).getType().equals(Material.AIR)){
			return;
		}
		
		if(Math.abs(x) < Math.abs(z)){
			if(z < 0){
				loc.setZ(loc.getZ() - 1);
			}else{
				loc.setZ(loc.getZ() + 1);
			}
		}else{
			if(x < 0){
				loc.setX(loc.getX() - 1);
			}else{
				loc.setX(loc.getX() + 1);
			}
		}
		
		if(evt.getEntity().getWorld().getBlockAt(loc).getType().equals(Material.AIR)){
			evt.getEntity().teleport(loc);
		}	
	}
}
