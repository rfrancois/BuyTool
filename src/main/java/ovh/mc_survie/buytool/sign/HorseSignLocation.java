package ovh.mc_survie.buytool.sign;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ovh.mc_survie.buytool.BuyTool;

public class HorseSignLocation extends SignLocation {
	@JsonIgnoreProperties(ignoreUnknown=true)
	
	@JsonCreator
	public HorseSignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	public HorseSignLocation(int x,  int y, int z) {
		super(x, y, z);
	}
	
	public void onSignCreated(SignChangeEvent event) {
		event.setLine(0, "§cAcheter");
		event.setLine(1, "un §2cheval");
		event.setLine(2, "dressé pour");
		event.setLine(3, price+"€");
	}
	
	public boolean onSignClicked(BuyTool plugin, Player player) {
        if(super.onSignClicked(plugin, player)) {
        	player.sendMessage("§aVoici un cheval");
        	plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon horse "+player.getLocation().getX()+" "+player.getLocation().getY()+" "+player.getLocation().getZ()+" {Tame:1, Variant:"+(1+(int)(Math.random()*1030))+"}");
        	return true;
        }
        return false;
	}
	
	public boolean setPrice(String[] lines, BuyTool plugin, Player player) {
		if(super.setPrice(lines, plugin,player,"tamed-horse-price")) {
			return true;
		}
		return false;
	}
}
