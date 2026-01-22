package gg.brim.kingdomsattributes;

import gg.brim.kingdomsattributes.listeners.PlayerScaleListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class KingdomsAttributes extends JavaPlugin {

    private static KingdomsAttributes instance;
    private Logger logger;
    
    private double snowKingdomScale;
    private Set<String> excludedPlayers;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // Загружаем конфиг
        saveDefaultConfig();
        loadConfiguration();

        // Регистрируем слушатели
        getServer().getPluginManager().registerEvents(new PlayerScaleListener(this), this);

        logger.info("KingdomsAttributes enabled!");
        logger.info("- Snow Kingdom scale: " + snowKingdomScale);
        logger.info("- Excluded players: " + excludedPlayers.size());
    }

    @Override
    public void onDisable() {
        logger.info("KingdomsAttributes disabled!");
        instance = null;
    }
    
    public void loadConfiguration() {
        reloadConfig();
        
        snowKingdomScale = getConfig().getDouble("snow-kingdom.scale", 0.8);
        
        List<String> excludedList = getConfig().getStringList("snow-kingdom.excluded-players");
        excludedPlayers = new HashSet<>();
        for (String name : excludedList) {
            excludedPlayers.add(name.toLowerCase());
        }
    }
    
    public double getSnowKingdomScale() {
        return snowKingdomScale;
    }
    
    public boolean isPlayerExcluded(String playerName) {
        return excludedPlayers.contains(playerName.toLowerCase());
    }

    public static KingdomsAttributes getInstance() {
        return instance;
    }
}
