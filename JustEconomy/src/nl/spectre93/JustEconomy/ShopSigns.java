package nl.spectre93.JustEconomy;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ShopSigns implements Listener{
	
private final JustEconomy plugin;
public final String shopHeader = "[Shop]";
public final String adminShopHeader = "[AdminShop]";
private HashMap<Location, UUID> signMap = new HashMap<Location, UUID>();
	
	public ShopSigns(Plugin plugin) {
		this.plugin = (JustEconomy) plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent evt) {
		Block b = evt.getClickedBlock();
		if(b == null) return;
		if(b.getType().equals(Material.WALL_SIGN)){
			Sign sign = (Sign)b.getState();
			String string = sign.getLine(0);
			if(string != null){
				switch(string){
				case shopHeader:
					shop(evt, false);	break;
				case adminShopHeader:
					shop(evt, true);	break;
				default: return;
				}
			}
		}
	}
	
	private void shop(PlayerInteractEvent evt, boolean adminShop) {
		Sign sign = (Sign) evt.getClickedBlock().getState();
		String[] lines = sign.getLines();
		Player p = evt.getPlayer();
		if(isShopOwner(p, sign)){
			if(p.isSneaking()){
				if(evt.getAction().equals(Action.LEFT_CLICK_BLOCK)){
					String nameOfItemInHand = p.getItemInHand().getType().name();
					if(nameOfItemInHand.length() <= 15){
						sign.setLine(1, nameOfItemInHand);
						sign.update();
					}else{
						p.sendMessage(ChatColor.RED + "Sorry the name of this item is too long and therefore can not be sold :(");
					}
						
					
					
					return;
				}
			}
		}
				
		ItemStack item;
		int price;
		try {
			item = new ItemStack(Material.matchMaterial(lines[1]));
			price = Integer.parseInt(lines[2]);
		} catch (NumberFormatException e) {
			System.out.println("Caught it.");
			return;
		}

		if(evt.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			//check shop inv
			
			//check p money
			if(!plugin.hasEnoughMoney(p, price)) return;
			
			if(p.getInventory().firstEmpty() != -1){
				p.sendMessage(ChatColor.BLUE + "You bought one " + item.getType().toString() + " for " + price + ".");
				plugin.adjustMoney(p, -price);
				p.getInventory().addItem(item);
			}else{
				p.sendMessage(ChatColor.BLUE + "Please have at least one inventory space free.");
			}
			//check p inventory
		} //else 

		//if(evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)){}	
	}

	private boolean isShopOwner(Player p, Sign sign) {
		// TODO Auto-generated method stub
		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent evt) {		
		if(evt.getBlock().getType().equals(Material.WALL_SIGN)){
			if(evt.getBlockAgainst().getType().equals(Material.GRAVEL) || evt.getBlockAgainst().getType().equals(Material.SAND))
				evt.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent evt) {
		if(evt.getBlock().getType().equals(Material.WALL_SIGN)){
			if(!isShopOwner(evt.getPlayer(), (Sign)evt.getBlock().getState()) && !evt.getPlayer().isOp())
				evt.setCancelled(true);
			return;
		}
		
		else if(hasSignOnSides(evt.getBlock())){
			evt.getPlayer().sendMessage(ChatColor.RED + "Sorry, but there are signs attached to this block!");
			evt.setCancelled(true);
			return;
		}
	}

	private boolean hasSignOnSides(Block block) {
		for(int i = -1; i < 2; i++){
			for (int j = -1; j < 2; j++) {
				if(i + j == 0 || i == j) continue;
				int tempX = block.getX() + i;
				int tempZ = block.getZ() + j;
				if(block.getWorld().getBlockAt(tempX, block.getY(), tempZ).getType().equals(Material.WALL_SIGN))
					return true;
			}
		}
		return false;
	}
}
