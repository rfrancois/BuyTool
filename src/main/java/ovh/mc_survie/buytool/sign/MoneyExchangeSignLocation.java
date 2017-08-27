package ovh.mc_survie.buytool.sign;

import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ovh.mc_survie.buytool.BuyTool;

public class MoneyExchangeSignLocation extends SignLocation {
	
	protected double emeraldPrice;

	public MoneyExchangeSignLocation(int x, int y, int z) {
		super(x, y, z);
	}
	
	@JsonCreator
	public MoneyExchangeSignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	protected double getEmeraldPrice(BuyTool plugin, Player player) {
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

}
