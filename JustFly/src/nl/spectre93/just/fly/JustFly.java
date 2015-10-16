package nl.spectre93.just.fly;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class JustFly extends JavaPlugin{

	public void onEnable(){	}
	public void onDisable(){ }
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(!(sender instanceof Player)) {sender.sendMessage("Only players can use this command."); return true;}
		Player player = (Player) sender;
		if(!getServer().getAllowFlight()) {player.sendMessage("Flying is not enabled in this server."); return true;}
		if(!player.hasPermission("just.fly")) {player.sendMessage("You don't have permission to use this command."); return true;}
		player.setAllowFlight(!player.getAllowFlight()); return true;
	}
}