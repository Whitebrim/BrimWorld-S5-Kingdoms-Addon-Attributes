package su.brim.kingdomsattributes.listeners;

import su.brim.kingdoms.api.KingdomsAPI;
import su.brim.kingdomsattributes.KingdomsAttributes;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Устанавливает GENERIC_SCALE для игроков снежного королевства
 */
public class PlayerScaleListener implements Listener {

    private static final String SNOW_KINGDOM = "snow_kingdom";
    private static final double DEFAULT_SCALE = 1.0;
    private static final double DEFAULT_ENTITY_REACH = 3.0;

    private final KingdomsAttributes plugin;

    public PlayerScaleListener(KingdomsAttributes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Используем Folia-совместимый scheduler для работы с entity
        player.getScheduler().run(plugin, scheduledTask -> {
            applyScaleModifier(player);
        }, null);
    }

    private void applyScaleModifier(Player player) {
        KingdomsAPI api = KingdomsAPI.getInstance();
        if (api == null) {
            plugin.getLogger().warning("KingdomsAPI not available!");
            return;
        }

        // Админы не получают модификаторы
        if (api.isAdmin(player)) {
            resetScale(player);
            resetEntityReach(player);
            return;
        }
        
        // Проверяем исключения
        if (plugin.isPlayerExcluded(player.getName())) {
            resetScale(player);
            resetEntityReach(player);
            return;
        }

        String kingdom = api.getPlayerKingdom(player.getUniqueId());
        
        if (SNOW_KINGDOM.equals(kingdom)) {
            double scale = plugin.getSnowKingdomScale();
            setScale(player, scale);
            plugin.getLogger().info("Applied scale " + scale + " to " + player.getName() + " (Snow Kingdom)");
            double entityReach = plugin.getSnowKingdomEntityReach();
            setEntityReach(player, entityReach);
            plugin.getLogger().info("Applied entity reach " + entityReach + " to " + player.getName() + " (Snow Kingdom)");
        }
    }

    private void setScale(Player player, double scale) {
        AttributeInstance attribute = player.getAttribute(Attribute.SCALE);
        if (attribute != null) {
            attribute.setBaseValue(scale);
        }
    }

    private void resetScale(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.SCALE);
        if (attribute != null) {
            attribute.setBaseValue(DEFAULT_SCALE);
        }
    }

    private void setEntityReach(Player player, double value) {
        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute != null) {
            attribute.setBaseValue(value);
        }
    }

    private void resetEntityReach(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute != null) {
            attribute.setBaseValue(DEFAULT_ENTITY_REACH);
        }
    }
}
