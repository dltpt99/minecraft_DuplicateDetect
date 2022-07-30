package me.nt.checkitemduplicate.listner;

import me.nt.checkitemduplicate.DuplicateDetect;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import me.nt.checkitemduplicate.entity.DetectedPlayerEntity;
import me.nt.checkitemduplicate.function.ControlDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class DetectHaveItem implements Listener {

    DuplicateDetect plugin;
    //    HashMap<ItemStack, Integer> Check_items;
    Set<DetectItemEntity> Check_items;
    ControlDatabase database;
    int cool_down;

    public DetectHaveItem(DuplicateDetect plugin) {
        this.plugin = plugin;
        Check_items = plugin.getDetectItemList();
        this.database = plugin.getDatabase();
        reloadConfig();
    }

    public void reloadConfig() {
        cool_down = plugin.getConfig().getInt("Config.Detect_Cooldown");
    }

    @EventHandler
    public void whenGetItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (Check_items.size() == 0) {
            return;
        }
        Player player = ((Player) e.getEntity()).getPlayer();

        if(!(player.hasPermission("dd.noDetect"))) return;

        HashMap<DetectItemEntity, Integer> playerHaveItems = new HashMap<>();

        // 플레이어 인벤토리를 리스트에 등록
        getItemsFromInv(playerHaveItems, (player.getInventory().getStorageContents()));
        // 플레이어가 습득한 아이템을 리스트에 등록
        getItemsFromInv(playerHaveItems, e.getItem().getItemStack());

        isHaveOverItem(player, playerHaveItems);
    }

    @EventHandler
    public void whenOpenInventory(InventoryOpenEvent e) {
        if (Check_items.size() == 0) {
            return;
        }

        if(!e.getPlayer().hasPermission("dd.noDetect")) return;

        //설정창 열었을때는 탐지 안함
        if (e.getInventory().getName().contains("DD - ") ||
                e.getInventory().getName().equalsIgnoreCase("Item Modify on DD")) {
            return;
        }

        HashMap<DetectItemEntity, Integer> playerHaveItems = new HashMap<>();

        // 플레이어가 오픈한 인벤토리(창고,화로,엔더체스트 등) 집계
        getItemsFromInv(playerHaveItems, e.getInventory().getStorageContents());

        // 플레이어 인벤토리 아이템 집계
        getItemsFromInv(playerHaveItems, e.getPlayer().getInventory().getStorageContents());

        // 플레이어가 오픈한 인벤 + 플레이어의 인벤을 집계한 것들을 체크아이템 리스트와 비교
        isHaveOverItem((Player) e.getPlayer(), playerHaveItems);
    }

    private void isHaveOverItem(Player player, HashMap<DetectItemEntity, Integer> playerHaveItems) {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (DetectItemEntity item_per_list : Check_items) {
                    if (playerHaveItems.containsKey(item_per_list)) {
                        if (playerHaveItems.get(item_per_list) >= item_per_list.getAmount()) {
                            DetectedPlayerEntity entity = new DetectedPlayerEntity(
                                    player.getUniqueId(),
                                    item_per_list.getItem(),
                                    playerHaveItems.get(item_per_list),
                                    plugin.getServer_name(),
                                    player.getLocation()
                            );
                            if (isOverCoolDown(entity)) {
                                plugin.addDetected_Player(entity);
                            }
                        }
                    }
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    private boolean isOverCoolDown(DetectedPlayerEntity inv_item) {

        Iterator<DetectedPlayerEntity> detected_list = plugin.getDetected_Player().iterator();
        DetectedPlayerEntity entity;

        while (detected_list.hasNext()) {
            entity = detected_list.next();
            if (!(entity.getItem().equals(inv_item.getItem()) && entity.getUuid().equals(inv_item.getUuid()))) {
                continue;
            }
            if(Duration.between(entity.getDate(), inv_item.getDate()).getSeconds() < cool_down ) {
//                plugin.getLogger().info("같은 플레이어가 같은 아이템으로 " + cool_down + "이내로 탐지되어 기록하지 않음");
                return false;
            }
        }
        return true;
    }

    private void getItemsFromInv(HashMap<DetectItemEntity, Integer> playerHaveItems, ItemStack... invItems) {
        DetectItemEntity temp_entity;
        for (ItemStack item : invItems) {
            if(item == null) continue;

            temp_entity = new DetectItemEntity(item);

            if (playerHaveItems.containsKey(temp_entity)) {
                playerHaveItems.put(temp_entity, playerHaveItems.get(temp_entity) + temp_entity.getAmount());
            } else{
                playerHaveItems.put(temp_entity, temp_entity.getAmount());
            }
        }
    }

    public void updateItemList(Set<DetectItemEntity> entity) {
        this.Check_items = entity;
    }
}
