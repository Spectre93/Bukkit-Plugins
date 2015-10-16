package nl.spectre93.just.sponge;

import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SpongeBlock{

	String blockState;
	String world;
	int x , y , z;

	public SpongeBlock(String blockState, String world, int x, int y, int z) {
		this.blockState = blockState;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public SpongeBlock(Block block) {
		String temp = block.getState().toString();
		String temp2 = temp.substring(temp.indexOf('@'));
		this.blockState = temp2;
		
		temp = block.getWorld().toString();
		temp2 = temp.substring((temp.indexOf('=') + 1), (temp.indexOf('}')));
		this.world = temp2;
		
		this.x = block.getX();
		this.y = block.getY();
		this.z = block.getZ();
	}
	
	public static SpongeBlock read(Scanner sc){
    	String blockState = sc.next();
    	String world = sc.next();
    	int x = sc.nextInt();
    	int y = sc.nextInt();
    	int z = sc.nextInt();
    	return new SpongeBlock(blockState, world, x, y, z);
	}
	
	@Override
	public String toString(){
		return "" + blockState + " " + world + " " + x + " " + y + " " + z;
	}

	public World getWorld() {
		return Bukkit.getServer().getWorld(world);
	}
	
	public Location getLocation() {
		return getWorld().getBlockAt(x, y, z).getLocation();
	}
}
