package ovh.mc_survie.buytool.sign;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ovh.mc_survie.buytool.BuyTool;

public class TPSignTask extends BukkitRunnable {
	
	private TPSignLocation tpSignLocation;
	private int teleport;
	private BuyTool plugin;
	private Player player;

	public TPSignTask(TPSignLocation tpSignLocation, int teleport, BuyTool plugin, Player player) {
		super();
		this.tpSignLocation = tpSignLocation;
		this.teleport = teleport;
		this.plugin = plugin;
		this.player = player;
	}

	public void run() {
		// TODO Auto-generated method stub
		tpSignLocation.teleport(plugin, player, teleport);
	}

}
