package ovh.mc_survie.buytool.sign;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ovh.mc_survie.buytool.BuyTool;

public class SignMessageTask extends BukkitRunnable {

	private BuyTool plugin;
	private TPSignLocation tpSignLocation;
	private Player player;
	private boolean isCancelled = false;

	public SignMessageTask(BuyTool plugin, TPSignLocation tpSignLocation, Player player) {
		super();
		this.plugin = plugin;
		this.tpSignLocation = tpSignLocation;
		this.player = player;
	}

	public SignMessageTask cancelTask() {
		isCancelled = true;
		return new SignMessageTask(plugin, tpSignLocation, player);
	}

	public void run() {
		if(isCancelled) {
			isCancelled = false;
			return;
		}
		tpSignLocation.delayEnded(plugin, player);
	}

	public void runTaskLater(long delay) {
		super.runTaskLater(plugin, delay);
	}
}
