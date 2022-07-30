package me.nt.checkitemduplicate.function;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectedPlayerEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class Announcer {
    Set<DetectedPlayerEntity> detected_Players;
    CheckItemDuplicate plugin;
    String announce_msg;
    String new_detected_msg;
    int cool_down;

    public Announcer(CheckItemDuplicate plugin, int cool_down) {
        this.plugin = plugin;
        this.cool_down = cool_down;
        this.detected_Players = plugin.getDetected_Player();
        this.announce_msg = plugin.getConfig().getString("Message.Period_Announcer");
        this.new_detected_msg = plugin.getConfig().getString("Message.New_Announcer");
        announce_DetectedPlayers();
    }

    public void announce_DetectedNews(DetectedPlayerEntity entity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
                String send_msg = new_detected_msg;

                for(Player player : players) {

                    // Permission
                    if(!player.hasPermission("dd.getAnnounce")) return;

                    if(send_msg.contains("%Server%")) send_msg = send_msg.replaceAll("%Server%", entity.getServer());
                    if(send_msg.contains("%World%")) send_msg = send_msg.replaceAll("%World%", entity.getLoc().getWorld().getName());
                    if(send_msg.contains("%Item_Name%")) {
                        if(entity.getItem().getItemMeta().getDisplayName() == null) {
                            send_msg = send_msg.replaceAll("%Item_Name%",entity.getItem().getType().name());
                        } else {
                            send_msg = send_msg.replaceAll("%Item_Name%",entity.getItem().getItemMeta().getDisplayName());
                        }
                    }
                    if(send_msg.contains("%Amount%")) send_msg = send_msg.replaceAll("%Amount%", entity.getAmount()+"");
                    if(send_msg.contains("%Location%")) send_msg = send_msg.replaceAll("%Location%",
                            entity.getLoc().getX()+", "+entity.getLoc().getY()+", "+entity.getLoc().getZ());

                    player.sendMessage(send_msg);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void announce_DetectedPlayers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(detected_Players == null || detected_Players.size()==0) {
                    plugin.updateEntities();
                    return;
                }

                Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

                String send_msg = announce_msg;
                for(Player player : players) {

                    // Permission
                    if(!player.hasPermission("dd.getAnnounce")) return;

                    if(send_msg.contains("%Amount%")) {
                        send_msg = send_msg.replaceAll("%Amount%", Integer.toString(detected_Players.size()));
                    }
                    player.sendMessage(send_msg);

                }
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20L * 60 * cool_down );
        // cool_down 분 단위로 주기적으로 알림
    }

    public void updateConfig() {
        this.announce_msg = plugin.getConfig().getString("Message.Announcer");
    }

    public void updateDetected_Players(Set<DetectedPlayerEntity> detected_Players) {
        this.detected_Players = detected_Players;
    }
}
