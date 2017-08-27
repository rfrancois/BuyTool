package ovh.mc_survie.buytool.sign;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ovh.mc_survie.buytool.BuyTool;

public class GetMoneySignLocation extends SignLocation {
	
	private double emeraldPrice;
	
	@JsonCreator
	public GetMoneySignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	public GetMoneySignLocation(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public void onSignCreated(SignChangeEvent event) {
		event.setLine(0, "§0Echanger");
		event.setLine(1, "§a1 émeraude");
		event.setLine(2, "§0contre");
		event.setLine(3, "§c"+ emeraldPrice +"€");
	}
	
	@Override
	public boolean setPrice(String[] lines, BuyTool plugin, Player player) {
		getEmeraldPrice(plugin, player);
		if(emeraldPrice <= -1) {
			return false;
		}
		price = 0;
		return true;
	}
	
	private double getEmeraldPrice(BuyTool plugin, Player player) {
		emeraldPrice = -1;
		try {
			emeraldPrice = Integer.parseInt(plugin.getConfig().getString("emerald-price"));
		}
		catch(Exception e) {
			player.sendMessage("§cErreur : le prix en éméraude n'a pas été défini dans config.yml.");
			return -1;
		}
		return emeraldPrice;
	}
	
	@Override
	public boolean onSignClicked(BuyTool plugin, Player player) {
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		if(itemStack.getType() != Material.EMERALD) {
			player.sendMessage("§cVous devez vous munir d'émeraude(s) pour échanger de l'argent");
			return false;
		}
		
		getEmeraldPrice(plugin, player);
		if(emeraldPrice <= -1) {
			return false;
		}
		
		Economy econ = plugin.getVault().getEcononomy();
		// Nombres d'émeraudes * prix d'une émreaude
		EconomyResponse r = econ.depositPlayer(player, itemStack.getAmount()*emeraldPrice);
		if(r.transactionSuccess()) {
			itemStack.setAmount(0);
			player.sendMessage(String.format("%s ont été ajouté à compte. Vous avez maintenant %s", econ.format(r.amount), econ.format(r.balance)));
			return true;
		}
		else {
			player.sendMessage(String.format("§4Une erreur s'est produite : %s", r.errorMessage));
			return false;
		}
	}

}
