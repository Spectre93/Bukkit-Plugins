package nl.spectre93.just.regions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class JustRegions {

	public void onEnable(){				
		
	}
	
	public void onDisable(){
		HandlerList.unregisterAll();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		return true;
	}
}
