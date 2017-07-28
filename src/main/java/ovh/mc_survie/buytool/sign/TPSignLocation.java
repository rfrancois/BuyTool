package ovh.mc_survie.buytool.sign;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ovh.mc_survie.buytool.BuyTool;

public class TPSignLocation extends SignLocation implements Listener {
	@JsonIgnoreProperties(ignoreUnknown=true)
	
	private ArrayList<int[]> teleport = new ArrayList<int[]>();
	private boolean isListening = false;
	private int betweenTeleports;
	private long time;
	
	@JsonCreator
	public TPSignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price, @JsonProperty("teleport") ArrayList<int[]> teleport, @JsonProperty("betweenTeleports")  int betweenTeleports) {
		super(x, y, z, price);
		this.teleport = teleport;
		this.betweenTeleports = betweenTeleports;
	}
	
	public TPSignLocation(int x,  int y, int z) {
		super(x, y, z);
	}
	
	public int getBetweenTeleports() {
		return betweenTeleports;
	}

	public ArrayList<int[]> getTeleport() {
		return teleport;
	}

	public void onSignCreated(SignChangeEvent event) {
		event.setLine(0, "§cTéléportation");
		//event.setLine(1, "un §2ane");
		event.setLine(2, "pour");
		event.setLine(3, price+"€");
	}
	
	public boolean onSignClicked(BuyTool plugin, Player player) {
        if(super.onSignClicked(plugin, player)) {
        	player.sendMessage("§atéléporté !");
        	return true;
        }
        return false;
	}
	
	public boolean setPrice(String[] lines, BuyTool plugin) {		
		if(super.setPrice(lines, plugin,"teleport-price")) {
			return true;
		}
		return false;
	}
	
	private int getBetweenTeleportsConfig(BuyTool plugin) {
		int betweenTeleports;
		try {
			betweenTeleports = Integer.parseInt(plugin.getConfig().getString("seconds-between-teleport"));
		}
		catch(NumberFormatException e) {
			return 5;
		}
		catch(Exception e) {
			return 5;
		}
		return betweenTeleports;
	}
	
	public boolean doAction(BuyTool plugin, SignChangeEvent event) {
		int betweenTeleports;
		try {
			this.betweenTeleports = Integer.parseInt(event.getLine(2));
		}
		catch(NumberFormatException e) {
			this.betweenTeleports = 0;
		}
		
		event.getPlayer().sendMessage("§eSélectionnez un endroit où se téléporter dans les 15 secondes à l'aide d'un clic droit.");
		PluginManager pm = plugin.getServer().getPluginManager();//On récupère le PluginManager du serveur
		pm.registerEvents(this, plugin);//On enregistre notre instance de Listener et notre plugin auprès du PluginManager
		isListening = true;
		onSignCreated(event);
		time = System.currentTimeMillis();
		return false;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		EquipmentSlot e = event.getHand(); //Get the hand of the event and set it to 'e'.
        if (!e.equals(EquipmentSlot.HAND)) { //If the event is fired by HAND (main hand)
           return;
        }
		if(!isListening) return;
		Action action = event.getAction();
		if(!action.equals(Action.RIGHT_CLICK_BLOCK)) return;
		if(System.currentTimeMillis() > (time + 15*1000)) { //multiply by 1000 to get milliseconds
			  isListening = false;
			  super.signsLocation.add(this);
			  this.save(event.getPlayer(), SignListener.getDirName(), SignListener.getFileName());
			  return;
		}
		teleport.add(new int[] {event.getClickedBlock().getX(),event.getClickedBlock().getY(),event.getClickedBlock().getZ()});
		event.getPlayer().sendMessage("§aPoint de téléportation fixé. §fVous pouvez sélectionner un autre point ou attendre 15 secondes pour terminer");
		time = System.currentTimeMillis();
	}

}
