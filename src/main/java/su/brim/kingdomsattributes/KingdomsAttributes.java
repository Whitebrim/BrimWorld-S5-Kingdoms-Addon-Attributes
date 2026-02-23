package su.brim.kingdomsattributes;

import su.brim.kingdomsattributes.commands.KingdomsAttributesCommand;
import su.brim.kingdomsattributes.listeners.PlayerScaleListener;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public final class KingdomsAttributes extends JavaPlugin {

    private static KingdomsAttributes instance;
    private Logger logger;

    // Атрибуты снежного королевства: Attribute -> value
    private Map<Attribute, Double> snowKingdomAttributes;

    // Whitelist: playerName (lowercase) -> (Attribute -> value)
    private Map<String, Map<Attribute, Double>> whitelist;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        saveDefaultConfig();
        loadConfiguration();

        KingdomsAttributesCommand commandExecutor = new KingdomsAttributesCommand(this);
        PluginCommand command = getCommand("kingdomsattributes");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        getServer().getPluginManager().registerEvents(new PlayerScaleListener(this), this);

        logger.info("KingdomsAttributes enabled!");
        logger.info("- Snow Kingdom attributes: " + snowKingdomAttributes.size());
        logger.info("- Whitelist players: " + whitelist.size());
    }

    @Override
    public void onDisable() {
        logger.info("KingdomsAttributes disabled!");
        instance = null;
    }

    public void loadConfiguration() {
        reloadConfig();

        // Загружаем атрибуты снежного королевства
        snowKingdomAttributes = new LinkedHashMap<>();
        ConfigurationSection snowSection = getConfig().getConfigurationSection("snow-kingdom");
        if (snowSection != null) {
            for (String key : snowSection.getKeys(false)) {
                Attribute attr = parseAttribute(key);
                if (attr != null) {
                    snowKingdomAttributes.put(attr, snowSection.getDouble(key));
                } else {
                    logger.warning("Unknown attribute in snow-kingdom: " + key);
                }
            }
        }

        // Загружаем whitelist
        whitelist = new LinkedHashMap<>();
        ConfigurationSection whitelistSection = getConfig().getConfigurationSection("whitelist");
        if (whitelistSection != null) {
            for (String playerName : whitelistSection.getKeys(false)) {
                ConfigurationSection playerSection = whitelistSection.getConfigurationSection(playerName);
                if (playerSection == null) continue;

                Map<Attribute, Double> attrs = new LinkedHashMap<>();
                for (String key : playerSection.getKeys(false)) {
                    Attribute attr = parseAttribute(key);
                    if (attr != null) {
                        attrs.put(attr, playerSection.getDouble(key));
                    } else {
                        logger.warning("Unknown attribute for player " + playerName + ": " + key);
                    }
                }

                if (!attrs.isEmpty()) {
                    whitelist.put(playerName.toLowerCase(), attrs);
                }
            }
        }
    }

    /**
     * Парсит snake_case название атрибута из конфига в Bukkit Attribute.
     * Использует Registry.ATTRIBUTE с NamespacedKey (minecraft namespace).
     * Например: "scale" -> minecraft:scale, "entity_interaction_range" -> minecraft:entity_interaction_range
     */
    private Attribute parseAttribute(String configKey) {
        // Конфиг использует snake_case, NamespacedKey тоже snake_case
        String normalizedKey = configKey.toLowerCase();

        // Пробуем как minecraft:key
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(normalizedKey));
        if (attr != null) {
            return attr;
        }

        // Пробуем с префиксом generic_ для обратной совместимости
        attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + normalizedKey));
        if (attr != null) {
            return attr;
        }

        return null;
    }

    /**
     * Возвращает все атрибуты, которыми управляет плагин (snow-kingdom + все whitelist записи).
     */
    public Set<Attribute> getAllManagedAttributes() {
        Set<Attribute> managed = new HashSet<>(snowKingdomAttributes.keySet());
        for (Map<Attribute, Double> playerAttrs : whitelist.values()) {
            managed.addAll(playerAttrs.keySet());
        }
        return managed;
    }

    public Map<Attribute, Double> getSnowKingdomAttributes() {
        return snowKingdomAttributes;
    }

    public boolean isInWhitelist(String playerName) {
        return whitelist.containsKey(playerName.toLowerCase());
    }

    public Map<Attribute, Double> getWhitelistAttributes(String playerName) {
        return whitelist.getOrDefault(playerName.toLowerCase(), Collections.emptyMap());
    }

    public int getWhitelistSize() {
        return whitelist.size();
    }

    public static KingdomsAttributes getInstance() {
        return instance;
    }
}
