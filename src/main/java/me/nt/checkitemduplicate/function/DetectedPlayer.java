package me.nt.checkitemduplicate.function;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectedPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DetectedPlayer {
    CheckItemDuplicate plugin;
    ControlDatabase controlDatabase;
    String detected_Server;
    List<String> display_lore;

    public DetectedPlayer(CheckItemDuplicate plugin) {
        this.plugin = plugin;
        this.controlDatabase = plugin.getDatabase();
        reload_config();
    }

    public void reload_config() {
        FileConfiguration config = plugin.getConfig();
        detected_Server = config.getString("Config.Server_Name");
        display_lore = config.getStringList("GUI.DD_GUI.Lore");
    }

    public void showDetectedPlayerList(CommandSender sender) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<DetectedPlayerEntity> detectedPlayers = plugin.getDetected_Player();
                int page_num = (detectedPlayers.size() / 45) + 1;
                int page_index = 0;
                @SuppressWarnings("unchecked")
                ArrayList<ItemStack>[] item_per_page = new ArrayList[page_num];
                Arrays.setAll(item_per_page, ArrayList<ItemStack> :: new);

                // 정렬(날짜 순)
                Iterator<DetectedPlayerEntity> iter = detectedPlayers.stream()
                        .sorted(Comparator.comparing(DetectedPlayerEntity::getDateAsFormatted)
                                .reversed())
                        .iterator();

                DetectedPlayerEntity player;
                int i = 0;

                // 탐지된 플레이어 목록 배열에 삽입
                while (iter.hasNext()) {
                    player = iter.next();
                    if(i>=45){
                        page_index++;
                        i = 0;
                    }

                    ItemStack item = player.getItem().clone();
                    ItemMeta item_meta = item.getItemMeta();

                    List<String> backup_lore = item_meta.getLore();
                    if( backup_lore==null) {
                        backup_lore = new ArrayList<>();
                    }

                    int index = 0;
                    List<String> lores = new ArrayList<>(display_lore);

                    for (String lore : lores) {
                        if(lore.contains("%Player_Name%")) {
                            if(plugin.getServer().getPlayer(player.getUuid()) !=null) {
                                lore = lore.replaceAll("%Player_Name%", Bukkit.getPlayer(player.getUuid()).getName());
                            }else {
                                lore = lore.replaceAll("%Player_Name%", Bukkit.getOfflinePlayer(player.getUuid()).getName());
                            }
                        }

                        if (lore.contains("%Amount%")) lore = lore.replaceAll("%Amount%", player.getAmount()+"");
                        if (lore.contains("%Server%")) lore = lore.replaceAll("%Server%", player.getServer());
                        if (lore.contains("%World%")) lore = lore.replaceAll("%World%",
                                player.getLoc().getWorld().getName());
                        if(lore.contains("%X%")) lore = lore.replaceAll("%X%", player.getLoc().getX()+"");
                        if(lore.contains("%Y%")) lore = lore.replaceAll("%Y%", player.getLoc().getY()+"");
                        if(lore.contains("%Z%")) lore = lore.replaceAll("%Z%", player.getLoc().getZ()+"");
                        if(lore.contains("%Time%")) lore = lore.replaceAll("%Time%", player.getDateAsFormatted());

                        lores.set(index, lore);
                        index++;
                    }

                    backup_lore.addAll(lores);
                    item_meta.setLore(backup_lore);
                    item.setItemMeta(item_meta);

                    item_per_page[page_index].add(item);
                    i++;
                }
                plugin.getPage().put((Player)sender, new Page(item_per_page, (Player) sender, "GUI"));
                plugin.getPage().get((Player)sender).showPage();
            }
        }.runTask(plugin);
    }

    public void clearDetectedPlayerList(CommandSender sender) {
        sender.sendMessage("탐지 리스트를 초기화 하였습니다.");
        plugin.clearDetected_Player();
        controlDatabase.clearDetectedPlayerList();
    }
}
