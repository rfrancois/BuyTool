package ovh.mc_survie.buytool.sign;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import ovh.mc_survie.buytool.BuyTool;
import ovh.mc_survie.buytool.Save;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerator;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
@JsonSubTypes({ @Type(value = HorseSignLocation.class), @Type(value = DonkeySignLocation.class), @Type(value = TPSignLocation.class), @Type(value = GetMoneySignLocation.class)})
public class SignLocation {
	@JsonIgnoreProperties(ignoreUnknown=true)
	
	protected int x;
	protected int y;
	protected int z;
	protected double price;
	protected static ArrayList<SignLocation> signsLocation = new ArrayList<SignLocation>();
	
	@JsonCreator
	public SignLocation(@JsonProperty("x") int x, @JsonProperty("y")  int y, @JsonProperty("z")  int z, @JsonProperty("price")  double price) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.price = price;
		signsLocation.add(this);
	}

	public SignLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public double getPrice() {
		return price;
	}
	
    public static String toJson() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        ObjectMapper mapper = new ObjectMapper();

        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        
        mapper.writerWithType(new TypeReference<ArrayList
                <SignLocation>>() {
                }).writeValue(out, signsLocation);

        return out.toString();
    }
    
    public static void fromJSON(String itemsJSON) throws IOException {
    	if(itemsJSON.isEmpty()) return;
    	signsLocation.clear();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.readValue(itemsJSON, new TypeReference<ArrayList<SignLocation>>() {});
    }
    
    public static ArrayList<SignLocation> getSignsLocation() {
    	return signsLocation;
    }
    
    public static SignLocation isSignLocation(int x, int y, int z) {
    	for(SignLocation signLocation: signsLocation) {
    		if(signLocation.x == x && signLocation.y == y && signLocation.z == z) {
    			return signLocation;
    		}
    	}
    	return null;
    }
    
    public static boolean deleteSign(int x, int y, int z) {
    	for(int i=0; i<signsLocation.size();i++) {
    		if(signsLocation.get(i).x == x && signsLocation.get(i).y == y && signsLocation.get(i).z == z) {
    			signsLocation.remove(i);
    			return true;
    		}
    	}
    	return false;
    }

    
    public String toString() {
    	String string= "";
    	for(SignLocation signLocation: signsLocation) {
    		string = signLocation.x+";"+signLocation.y+";"+signLocation.z+";"+signLocation.price+"\n";
    	}
    	return string;
    }
    
    public void onSignCreated(SignChangeEvent event) {
    	event.setLine(0, "§cAcheter");
    	event.setLine(3, price+"€");
    }
    
    public boolean onSignClicked(BuyTool plugin, Player player) {
    	if(price == 0) return true;
		Economy econ = plugin.getVault().getEcononomy();
		EconomyResponse r = econ.withdrawPlayer(player, price);
		if(r.transactionSuccess()) {
			player.sendMessage(String.format("%s ont été pris sur votre compte. Vous avez maintenant %s", econ.format(r.amount), econ.format(r.balance)));
			return true;
		}
		else {
			player.sendMessage(String.format("§cUne erreur s'est produite : %s", r.errorMessage));
			return false;
		}
    }
    
    
	public boolean setPrice(String[] lines, BuyTool plugin, Player player) {
		return false;
	}
	
	public boolean setPrice(String[] lines, BuyTool plugin, Player player, String config) {
		if(!lines[1].isEmpty()) {
			try {
				price = Double.parseDouble(lines[1]);	
			}
			catch(NumberFormatException e) {
				player.sendMessage("§cErreur : le prix initialisé dans le panneau n'est pas un chiffre");
				return false;
			}
			if(price >= 0) {
				return true;
			}
		}
		try {
			price = Double.parseDouble(plugin.getConfig().getString(config));
		}
		catch(NumberFormatException e) {
			player.sendMessage("§cErreur : le prix initialisé dans config.yml n'est pas un chiffre");
			return false;
		}
		catch(Exception e) {
			player.sendMessage("§cErreur : le prix initialisé dans config.yml n'est pas un chiffre");
			return false;
		}
		if(price >= 0) {
			return true;
		}
		return false;
	}
	
	public boolean doAction(BuyTool plugin, SignChangeEvent event) {
		return true;
	}
	
	public void save(Player player, String dirName, String fileName) {
		String json;
		try {
			json = toJson();
		} catch (IOException e) {
			player.sendMessage("§cUne erreur est survenue : les données n'ont pas pu être converties en json lors de la création d'un panneau");
			return;
		}
		new Save().createFile(json, dirName, fileName);
	}
}
