package ovh.mc_survie.buytool;

import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ovh.mc_survie.buytool.sign.SignListener;

public class BuyTool extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("Minecraft");
	private Vault vault;
	
	@Override
	public void onEnable(){
		saveDefaultConfig();
		
		Listener l = new SignListener(this);//On crée une instance de notre classe qui implémente Listener
		PluginManager pm = getServer().getPluginManager();//On récupère le PluginManager du serveur
		pm.registerEvents(l, this);//On enregistre notre instance de Listener et notre plugin auprès du PluginManager
		
		/* VAULT BEGIN */
		vault = new Vault(this);
		if (!vault.setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        vault.setupPermissions();
        //vault.setupChat();
        /* VAULT END */
				
		getLogger().info("Plugin launched");
	}
	
	@Override
	public void onDisable(){
		// Actions à effectuer à la désactivation du plugin
		//   - A l'extinction du serveur
		//   - Pendant un /reload
	}
	
	public Vault getVault() {
		return vault;
	}
}

