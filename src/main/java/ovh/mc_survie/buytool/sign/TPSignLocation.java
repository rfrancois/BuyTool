package ovh.mc_survie.buytool.sign;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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
	private static TPMessageSignTask signMessageTask;
	private static TPSignLocation prevTPSignLocation;

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
		int line = 1;
		if(!event.getLine(3).isEmpty()) {
			event.setLine(line, event.getLine(3));
			line++;
		}
		event.setLine(line, "pour");
		line++;
		event.setLine(line, price+"€");
	}

	public boolean onSignClicked(BuyTool plugin, Player player) {
		if(super.onSignClicked(plugin, player)) {
			teleport(plugin, player, 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean setPrice(String[] lines, BuyTool plugin, Player player) {		
		if(super.setPrice(lines, plugin, player, "teleport-price")) {
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

		Player player = event.getPlayer();
		player.sendMessage("§eSélectionnez un endroit où se téléporter dans les 15 secondes à l'aide d'un clic droit.");
		PluginManager pm = plugin.getServer().getPluginManager();//On récupère le PluginManager du serveur
		pm.registerEvents(this, plugin);//On enregistre notre instance de Listener et notre plugin auprès du PluginManager
		isListening = true;
		onSignCreated(event);
		
		// Si on avait pas fini la création d'un précédent panneau, annuler la création du précédent panneau
		if(signMessageTask != null) signMessageTask.cancelTask();
		if(prevTPSignLocation != null) prevTPSignLocation.isListening = false;
		prevTPSignLocation = this;
		
		signMessageTask = new TPMessageSignTask(plugin, this, player);
		signMessageTask.runTaskLater(300);
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
			return;
		}
		Player player = event.getPlayer();
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		if(itemStack.getType() == Material.SIGN) {
			player.sendMessage("§cVous ne devez pas avoir de panneau en main pour fixer un point de téléportation");
			return;
		}
		teleport.add(new int[] {event.getClickedBlock().getX(),event.getClickedBlock().getY(),event.getClickedBlock().getZ()});
		event.getPlayer().sendMessage("§aPoint de téléportation fixé. §fVous pouvez sélectionner un autre point ou attendre 15 secondes pour terminer");
		signMessageTask = signMessageTask.renewTask();
		signMessageTask.runTaskLater(300);
		time = System.currentTimeMillis();
	}


	public void delayEnded(BuyTool plugin, Player player) {
		isListening = false;
		if(teleport.size() <= 0) {
			player.sendMessage("§eLes 15 secondes sont terminées mais le panneau de télportation n'a pas été initialisé correctement. Veuillez en créer un autre");
			return;
		}
		if(teleport.size() > 1 && betweenTeleports == 0) {
			try {
				betweenTeleports = Integer.parseInt(plugin.getConfig().getString("between-teleports"));
			}
			catch(NumberFormatException e) {
				betweenTeleports = 5;
			}
			catch(Exception e) {
				betweenTeleports = 5;
			}
		}
		player.sendMessage("§aLes 15 secondes sont terminées et le panneau de téléportation a bien été initialisé.");
		super.signsLocation.add(this);
		this.save(player, SignListener.getDirName(), SignListener.getFileName());
	}

	public void teleport(BuyTool plugin, Player player, int teleport) {
		plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tp " + player.getName() + " " + this.teleport.get(teleport)[0] + " " + this.teleport.get(teleport)[1] + " " + this.teleport.get(teleport)[2]);
		if(this.teleport.size() > 1) {
			teleport++;
			if(teleport < this.teleport.size()) {
				new TPSignTask(this, teleport, plugin, player).runTaskLater(plugin, betweenTeleports*20);
				player.sendMessage("§aVous avez été téléporté. Prochaine téléportation dans " + betweenTeleports + " secondes");
			}
			else {
				player.sendMessage("§aVous avez été téléporté");
			}
		}
	}
}
