package ovh.mc_survie.buytool;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class HorseSignLocation extends SignLocation {
	@JsonIgnoreProperties(ignoreUnknown=true)
	
	@JsonCreator
	public HorseSignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		super(x, y, z, price);
	}
	
	public void onSignedClicked(BuyTool plugin, PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Economy econ = plugin.getVault().getEcononomy();
		EconomyResponse r = econ.withdrawPlayer(player, price);
        if(r.transactionSuccess()) {
        	player.sendMessage("§aVoici un cheval");
        	plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon horse "+player.getLocation().getX()+" "+player.getLocation().getY()+" "+player.getLocation().getZ()+" {Tame:1, SaddleItem:{id:saddle,Count:1}}");
            player.sendMessage(String.format("%s ont été pris sur votre compte. Vous avez maintenant %s", econ.format(r.amount), econ.format(r.balance)));
        } else {
            player.sendMessage(String.format("§4Une erreur s'est produite : %s", r.errorMessage));
        }
	}
}
