package ovh.mc_survie.buytool.sign;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ovh.mc_survie.buytool.BuyTool;

public class MoneyStonesSignLocation extends MoneyExchangeSignLocation {

	@JsonCreator
	public MoneyStonesSignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	public MoneyStonesSignLocation(int x, int y, int z) {
		super(x, y, z);
	}
	
	@Override
	public void onSignCreated(SignChangeEvent event) {
		event.setLine(0, "§0Echanger");
		event.setLine(1, "§c"+ price +"€");
		event.setLine(2, "§0contre");
		event.setLine(3, "§a1 émeraude");
	}
	
	@Override
	public boolean setPrice(String[] lines, BuyTool plugin, Player player) {		
		if(super.setPrice(lines, plugin, player, "emerald-price")) {
			return true;
		}
		return false;
	}
	
	public boolean onSignClicked(BuyTool plugin, Player player) {
        if(super.onSignClicked(plugin, player)) {
        	plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " emerald 1");
        	return true;
        }
        return false;
	}
}
