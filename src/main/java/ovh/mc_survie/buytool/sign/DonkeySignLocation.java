package ovh.mc_survie.buytool.sign;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ovh.mc_survie.buytool.BuyTool;

public class DonkeySignLocation extends SignLocation {
	@JsonIgnoreProperties(ignoreUnknown=true)
	
	@JsonCreator
	public DonkeySignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	public DonkeySignLocation(int x,  int y, int z) {
		super(x, y, z);
	}
	
	public void onSignCreated(SignChangeEvent event) {
		event.setLine(0, "§cAcheter");
		event.setLine(1, "un §2ane");
		event.setLine(2, "dressé pour");
		event.setLine(3, price+"€");
	}
	
	public boolean onSignClicked(BuyTool plugin, Player player) {
        if(super.onSignClicked(plugin, player)) {
        	player.sendMessage("§aVoici un âne");
        	plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon donkey "+player.getLocation().getX()+" "+player.getLocation().getY()+" "+player.getLocation().getZ()+" {Tame:1}");
        	return true;
        }
        return false;
	}
	
	public boolean setPrice(String[] lines, BuyTool plugin) {
		if(super.setPrice(lines, plugin,"tamed-donkey-price")) {
			return true;
		}
		return false;
	}
}
