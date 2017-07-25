package ovh.mc_survie.buytool;

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

public class PluginListener implements Listener {

	private final BuyTool plugin;
	private final String dirName;
	private final String fileName;

	public PluginListener(BuyTool buyTool) {
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
	
	private double getTamedHorsePrice(String[] lines) {
		double price;
		if(!lines[1].isEmpty()) {
			try {
				price = Double.parseDouble(lines[1]);	
			}
			catch(NumberFormatException e) {
				lines[1] = "";
				return getTamedHorsePrice(lines);
			}
		}
		else {
			try {
				price = Double.parseDouble(plugin.getConfig().getString("tamed-horse-price"));
			}
			catch(NumberFormatException e) {
				return -1;
			}
		}
		return price;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		if(!player.isOp()) return;
		String[] lines = event.getLines();
		if(!lines[0].equalsIgnoreCase("[BuyHorse]")) return;
		double price = getTamedHorsePrice(lines);
		if(price <= 0) {
			player.sendMessage("§4Une erreur est survenue : le prix initialisé dans config.yml et dans le panneau ne sont pas des chiffres");
			return;
		}
		event.setLine(0, "§cCheval");
		event.setLine(1, "Acheter un");
		event.setLine(2, "cheval dressé");
		event.setLine(3, "pour "+price+"€");
		player.sendMessage("§a[BuyHorse] §fLe panneau a bien été créé");
		
		new HorseSignLocation(event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ(),price);
		String json;
		try {
			json = SignLocation.toJson();
		} catch (IOException e) {
			player.sendMessage("§4Une erreur est survenue : les données n'ont pas pu être converties en json lors de la création d'un panneau");
			return;
		}
		new Save().createFile(json, dirName, fileName);
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
		signLocation.onSignedClicked(plugin, event);
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
