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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * При заходе игрока:
 * 1. Сбрасывает управляемые атрибуты (только base value, без удаления модификаторов)
 * 2. Если игрок в снежном королевстве — применяет атрибуты snow-kingdom
 * 3. Если игрок в whitelist — применяет персональные атрибуты (перезаписывает snow-kingdom)
 */
public class PlayerScaleListener implements Listener {

    private static final String SNOW_KINGDOM = "snow_kingdom";

    /**
     * Правильные дефолтные значения атрибутов ДЛЯ ИГРОКА (Player).
     * Отличаются от Attribute.getDefaultValue() которые возвращают generic defaults!
     * Например: MOVEMENT_SPEED generic default = 0.7, player default = 0.1
     */
    private static final Map<Attribute, Double> PLAYER_DEFAULTS = new HashMap<>();

    static {
        PLAYER_DEFAULTS.put(Attribute.MAX_HEALTH, 20.0);
        PLAYER_DEFAULTS.put(Attribute.FOLLOW_RANGE, 32.0);
        PLAYER_DEFAULTS.put(Attribute.KNOCKBACK_RESISTANCE, 0.0);
        PLAYER_DEFAULTS.put(Attribute.MOVEMENT_SPEED, 0.1);
        PLAYER_DEFAULTS.put(Attribute.FLYING_SPEED, 0.02);
        PLAYER_DEFAULTS.put(Attribute.ATTACK_DAMAGE, 1.0);
        PLAYER_DEFAULTS.put(Attribute.ATTACK_KNOCKBACK, 0.0);
        PLAYER_DEFAULTS.put(Attribute.ATTACK_SPEED, 4.0);
        PLAYER_DEFAULTS.put(Attribute.ARMOR, 0.0);
        PLAYER_DEFAULTS.put(Attribute.ARMOR_TOUGHNESS, 0.0);
        PLAYER_DEFAULTS.put(Attribute.FALL_DAMAGE_MULTIPLIER, 1.0);
        PLAYER_DEFAULTS.put(Attribute.LUCK, 0.0);
        PLAYER_DEFAULTS.put(Attribute.MAX_ABSORPTION, 0.0);
        PLAYER_DEFAULTS.put(Attribute.SAFE_FALL_DISTANCE, 3.0);
        PLAYER_DEFAULTS.put(Attribute.SCALE, 1.0);
        PLAYER_DEFAULTS.put(Attribute.STEP_HEIGHT, 0.6);
        PLAYER_DEFAULTS.put(Attribute.GRAVITY, 0.08);
        PLAYER_DEFAULTS.put(Attribute.JUMP_STRENGTH, 0.42);
        PLAYER_DEFAULTS.put(Attribute.BURNING_TIME, 1.0);
        PLAYER_DEFAULTS.put(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, 0.0);
        PLAYER_DEFAULTS.put(Attribute.MOVEMENT_EFFICIENCY, 0.0);
        PLAYER_DEFAULTS.put(Attribute.OXYGEN_BONUS, 0.0);
        PLAYER_DEFAULTS.put(Attribute.WATER_MOVEMENT_EFFICIENCY, 0.0);
        PLAYER_DEFAULTS.put(Attribute.BLOCK_INTERACTION_RANGE, 4.5);
        PLAYER_DEFAULTS.put(Attribute.ENTITY_INTERACTION_RANGE, 3.0);
        PLAYER_DEFAULTS.put(Attribute.BLOCK_BREAK_SPEED, 1.0);
        PLAYER_DEFAULTS.put(Attribute.MINING_EFFICIENCY, 0.0);
        PLAYER_DEFAULTS.put(Attribute.SNEAKING_SPEED, 0.3);
        PLAYER_DEFAULTS.put(Attribute.SUBMERGED_MINING_SPEED, 0.2);
        PLAYER_DEFAULTS.put(Attribute.SWEEPING_DAMAGE_RATIO, 0.0);
    }

    private final KingdomsAttributes plugin;

    public PlayerScaleListener(KingdomsAttributes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Folia-совместимый scheduler
        player.getScheduler().run(plugin, scheduledTask -> {
            applyAttributes(player);
        }, null);
    }

    private void applyAttributes(Player player) {
        Set<Attribute> managedAttributes = plugin.getAllManagedAttributes();

        // 1. Сбрасываем только управляемые атрибуты (base value)
        resetManagedAttributes(player, managedAttributes);

        KingdomsAPI api = KingdomsAPI.getInstance();
        if (api == null) {
            plugin.getLogger().warning("KingdomsAPI not available!");
            applyWhitelistIfPresent(player, managedAttributes);
            return;
        }

        String kingdom = api.getPlayerKingdom(player.getUniqueId());

        // 2. Если игрок в снежном королевстве — применяем атрибуты
        if (SNOW_KINGDOM.equals(kingdom)) {
            Map<Attribute, Double> snowAttrs = plugin.getSnowKingdomAttributes();
            for (Map.Entry<Attribute, Double> entry : snowAttrs.entrySet()) {
                setAttribute(player, entry.getKey(), entry.getValue());
            }
            plugin.getLogger().info("Applied snow kingdom attributes to " + player.getName());
        }

        // 3. Если игрок в whitelist — перезаписываем
        applyWhitelistIfPresent(player, managedAttributes);
    }

    private void applyWhitelistIfPresent(Player player, Set<Attribute> managedAttributes) {
        if (plugin.isInWhitelist(player.getName())) {
            resetManagedAttributes(player, managedAttributes);

            Map<Attribute, Double> playerAttrs = plugin.getWhitelistAttributes(player.getName());
            for (Map.Entry<Attribute, Double> entry : playerAttrs.entrySet()) {
                setAttribute(player, entry.getKey(), entry.getValue());
            }
            plugin.getLogger().info("Applied whitelist attributes to " + player.getName()
                    + " (" + playerAttrs.size() + " attributes)");
        }
    }

    /**
     * Сбрасывает ТОЛЬКО управляемые атрибуты — ставит base value в player default.
     * НЕ удаляет модификаторы — они ставятся Minecraft для экипировки (броня, зелья и т.д.)
     * и их удаление ломает работу надетых предметов.
     * Плагин работает только через setBaseValue, поэтому удалять модификаторы не нужно.
     */
    private void resetManagedAttributes(Player player, Set<Attribute> managedAttributes) {
        for (Attribute attribute : managedAttributes) {
            try {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) continue;

                Double playerDefault = PLAYER_DEFAULTS.get(attribute);
                if (playerDefault != null) {
                    instance.setBaseValue(playerDefault);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void setAttribute(Player player, Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        } else {
            plugin.getLogger().warning("Attribute " + attribute.getKey() + " not available for " + player.getName());
        }
    }
}
