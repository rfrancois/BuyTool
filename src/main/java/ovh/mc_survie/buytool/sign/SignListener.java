package ovh.mc_survie.buytool.sign;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ovh.mc_survie.buytool.BuyTool;
import ovh.mc_survie.buytool.Save;

public class SignListener implements Listener {

	private final BuyTool plugin;
	private static String dirName;
	private static String fileName;
	
	public static String getDirName() {
		return dirName;
	}

	public static String getFileName() {
		return fileName;
	}

	public SignListener(BuyTool buyTool) {
		this.plugin = buyTool;
		dirName = plugin.getDataFolder().toString() + "/saves";
		fileName = "signs.json";
    	if(SignLocation.getSignsLocation().size() == 0) {
    		String json = new Save().readFromFile(dirName, fileName);
    		if(!json.isEmpty()) {
	    		try {
					SignLocation.fromJSON(json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					plugin.getLogger().severe("Erreur lors de la récupération du json : " + e.toString());
				}
    		}
    	}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		if(!player.isOp()) return;
		String[] lines = event.getLines();
		SignLocation signLocation = null;
		if(lines[0].equalsIgnoreCase("[BuyHorse]")) {
			signLocation = new HorseSignLocation(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ());
		}
		else if(lines[0].equalsIgnoreCase("[BuyDonkey]"))  {
			signLocation = new DonkeySignLocation(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ());
		}
		else if(lines[0].equalsIgnoreCase("[BuyTp]"))  {
			signLocation = new TPSignLocation(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ());
		}
		else if(lines[0].equalsIgnoreCase("[BuyMoney]")) {
			signLocation = new GetMoneySignLocation(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ());
		}
		else {
			return;
		}
		if(!signLocation.setPrice(lines, plugin, player)) {
			return;
		}
		if(!signLocation.doAction(plugin, event)) return;
		signLocation.getSignsLocation().add(signLocation);
		
		signLocation.onSignCreated(event);		
		signLocation.save(player, dirName, fileName);
		player.sendMessage("§a[BuyTool] §fLe panneau a bien été créé");
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
		if(!(event.getClickedBlock().getState() instanceof Sign)) return;
		SignLocation signLocation = SignLocation.isSignLocation(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
		if(signLocation == null) {
			return;
		}
		signLocation.onSignClicked(plugin, event.getPlayer());
	}
	
	@EventHandler
    public void onSignDestroy(BlockBreakEvent event) {
		if(!(event.getBlock().getState() instanceof Sign)) return;
		Player player = event.getPlayer();
		if(!player.isOp()) {
			event.setCancelled(true);
			return;
		}
		if(SignLocation.deleteSign(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ())) {
			try {
				new Save().createFile(SignLocation.toJson(), dirName, fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				player.sendMessage("§4Une erreur est survenue : les données n'ont pas pu être converties en json lors de la suppression d'un panneau");
			}
		}
	}

}
