package me.nt.checkitemduplicate.command;

import me.nt.checkitemduplicate.DuplicateDetect;
import me.nt.checkitemduplicate.function.ControlDatabase;
import me.nt.checkitemduplicate.function.ControlItem;
import me.nt.checkitemduplicate.function.DetectedPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandHeader implements CommandExecutor {
    DuplicateDetect plugin;
    ControlItem controlItem;
    ControlDatabase controlDatabase;
    DetectedPlayer detectedPlayer;
    String reload_Success;

    public CommandHeader(DuplicateDetect plugin) {
        this.plugin = plugin;
        this.controlItem = new ControlItem(plugin);
        this.detectedPlayer = new DetectedPlayer(plugin);
        this.controlDatabase = plugin.getDatabase();
        reload_Success = plugin.getConfig().getString("Message.Reload_Success");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission("dd.command.basic")){
            return true;
        }

        if (args.length == 0) {
            showHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                controlItem.addItem(sender, args);
                break;
            case "item":
                controlItem.showItemList(sender);
                break;
            case "gui":
                if(args.length>=2) {
                    if(args[1].equalsIgnoreCase("clear") && sender.hasPermission("dd.gui.clear")) {
                        detectedPlayer.clearDetectedPlayerList(sender);
                        break;
                    }
                }
                detectedPlayer.showDetectedPlayerList(sender);
                break;
            case "reload":
                plugin.reload_Config();
                sender.sendMessage(reload_Success);
                break;
            case "dbreload":
                plugin.updateEntities();
                break;

        }
        return true;
    }

    public void showHelpMessage(CommandSender sender) {
        String msg = "§7§m----------------------------------------------------\n" +
                "§6§lDuplicate Detect\n" +
                "§7§m----------------------------------------------------\n" +
                "§e/dd add <Amount> §8- §7손에 든 아이템을 감지 목록에 추가합니다. Amount 이상으로 유저가 소지하게 되면 감지됩니다.\n" +
                "§e/dd gui §8- §7감지된 유저 목록을 확인합니다.\n" +
                "§e/dd gui clear §8- §7감지된 유저 목록을 전부 제거합니다.\n" +
                "§e/dd item §8- §7등록된 아이템 목록을 확인합니다.\n" +
                "§e/dd reload §8- §7config를 리로드합니다.\n" +
                "§e/dd dbreload §8- §7DB의 정보를 다시 불러옵니다.\n";

        sender.sendMessage(msg);
    }

    public void reload_Config() {
        controlItem.reloadConfig();
        detectedPlayer.reload_config();
    }
}
