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
            return;
        }
        
        // Проверяем исключения
        if (plugin.isPlayerExcluded(player.getName())) {
            resetScale(player);
            return;
        }

        String kingdom = api.getPlayerKingdom(player.getUniqueId());
        
        if (SNOW_KINGDOM.equals(kingdom)) {
            double scale = plugin.getSnowKingdomScale();
            setScale(player, scale);
            plugin.getLogger().info("Applied scale " + scale + " to " + player.getName() + " (Snow Kingdom)");
        } else {
            // Для игроков других королевств сбрасываем на стандартный размер
            resetScale(player);
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
}
