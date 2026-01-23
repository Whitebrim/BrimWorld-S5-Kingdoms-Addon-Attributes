package su.brim.kingdomsattributes.commands;

import su.brim.kingdomsattributes.KingdomsAttributes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KingdomsAttributesCommand implements CommandExecutor, TabCompleter {

    private final KingdomsAttributes plugin;

    public KingdomsAttributesCommand(KingdomsAttributes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("§6KingdomsAttributes §7v" + plugin.getDescription().getVersion());
            sender.sendMessage("§7Используйте §f/" + label + " reload §7для перезагрузки конфига");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("kingdomsattributes.reload")) {
                sender.sendMessage("§cУ вас нет прав на эту команду!");
                return true;
            }

            plugin.loadConfiguration();
            sender.sendMessage("§aКонфигурация перезагружена!");
            sender.sendMessage("§7- Scale: §f" + plugin.getSnowKingdomScale());
            sender.sendMessage("§7- Исключений: §f" + plugin.getExcludedPlayersCount());
            return true;
        }

        sender.sendMessage("§cНеизвестная подкоманда. Используйте §f/" + label + " reload");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("kingdomsattributes.reload")) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
