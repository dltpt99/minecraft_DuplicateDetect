package me.nt.checkitemduplicate.listner;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import me.nt.checkitemduplicate.function.ControlDatabase;
import me.nt.checkitemduplicate.function.Page;
import me.nt.checkitemduplicate.function.Serialize;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MenuControl implements Listener {
    CheckItemDuplicate plugin;
    ControlDatabase database;
    ChatLisnter chatLisnter;
    ItemStack modify_icon;
    ItemStack delete_icon;

    public MenuControl(CheckItemDuplicate plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        initDetailicon();
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e) {
        if(e.getCurrentItem()==null) return;
        if(!e.getWhoClicked().hasPermission("dd.modify.item")) return;

        if (e.getView().getTitle().contains("§0DD - Item")) {
            Page page = plugin.getPage().get(e.getWhoClicked());
            List<ItemStack> iter = page.getItem_list()[page.getCurrent_page()-1];
            ItemStack item;
            int i=0;

            e.setCancelled(true);
        }
        
        if (e.getView().getTitle().contains("DD - ")) {
            e.setCancelled(true);

            if(e.getRawSlot()==48 && e.getCurrentItem().getType() != Material.AIR) {
                plugin.getPage().get(e.getWhoClicked()).beforePage();
                return;
            }
            if(e.getRawSlot()==50 && e.getCurrentItem().getType() != Material.AIR) {
                plugin.getPage().get(e.getWhoClicked()).nextPage();
                return;
            }
        } else{
            plugin.getPage().remove(e.getWhoClicked());
        }

        if (e.getView().getTitle().equalsIgnoreCase("Item Modify on DD")) {
            if (e.getCurrentItem().equals(modify_icon)) {
                modifyAmount((Player) e.getWhoClicked(),e.getInventory().getItem(2));
                e.getWhoClicked().closeInventory();
            }
            e.setCancelled(true);
        }

        if (e.getView().getTitle().equalsIgnoreCase("Item Modify on DD")) {
            if (e.getCurrentItem().equals(delete_icon)) {
                deleteFromList((Player) e.getWhoClicked(), e.getInventory().getItem(2));
                e.getWhoClicked().closeInventory();
            }
            e.setCancelled(true);
        }
    }

/*    @EventHandler
    public void closeInventory(InventoryCloseEvent e) {
        e.getPlayer().sendMessage("close reason"+e.getEventName());
        if(e.getInventory().getName().contains("DD - ")) {
            plugin.getPage().remove(e.getPlayer());
        }
    }*/

    public void deleteFromList(Player player, ItemStack item) {
        player.sendMessage(ChatColor.RED+"If you want to delete this item from list, Enter the Y or Yes");
        chatLisnter = new ChatLisnter(this);
        chatLisnter.setTargetPlayer(player, item);
        chatLisnter.setDelete();

        Bukkit.getPluginManager().registerEvents(chatLisnter, plugin);
    }

    public void setDelete(Player player, ItemStack item) {
        plugin.deleteItemFromList(new DetectItemEntity(item));
        player.sendMessage(ChatColor.GREEN+"Deleted Item from check list!");
        plugin.updateEntities();
    }

    public void modifyAmount(Player player, ItemStack item) {
        player.sendMessage(ChatColor.BLUE+"Enter the number you want to change");
        chatLisnter = new ChatLisnter(this);
        chatLisnter.setTargetPlayer(player, item);

        Bukkit.getPluginManager().registerEvents(chatLisnter, plugin);
    }

    public void setAmount(Player player, ItemStack item) {
        DetectItemEntity entity = new DetectItemEntity(item);
        database.modifyItemAmount(entity);
        player.sendMessage(ChatColor.GREEN + "Item amount has been modified");
        plugin.updateEntities();
    }

    public void unregisterChatListner() {
        AsyncPlayerChatEvent.getHandlerList().unregister(chatLisnter);
        chatLisnter = null;
    }

    public void openDetailMenu(InventoryClickEvent e, ItemStack item) {
        Inventory menu = Bukkit.createInventory(null, 18, "Item Modify on DD");
        menu.setItem(2 , item);

        menu.setItem( 6, modify_icon);
        menu.setItem( 15, delete_icon);

        e.getWhoClicked().openInventory(menu);
    }

    public void initDetailicon() {
        modify_icon = new ItemStack(Material.EYE_OF_ENDER, 1);
        delete_icon = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemMeta modify_meta = modify_icon.getItemMeta();
        ItemMeta delete_meta = delete_icon.getItemMeta();
        List<String> modify_lore = new ArrayList<>();
        List<String> delete_lore = new ArrayList<>();

        modify_meta.setDisplayName(ChatColor.GREEN+"Modify Amount");
        modify_lore.add("You can modify amount to here");
        modify_meta.setLore(modify_lore);
        modify_icon.setItemMeta(modify_meta);

        delete_meta.setDisplayName(ChatColor.RED+"Delete From List");
        delete_lore.add("Delete This item From List");
        delete_meta.setLore(delete_lore);
        delete_icon.setItemMeta(delete_meta);
    }

}

class ChatLisnter implements Listener {
    MenuControl menuControl;
    Player player;
    ItemStack item;
    boolean isDelete=false;

    public ChatLisnter(MenuControl menuControl) {
        this.menuControl = menuControl;
    }

    @EventHandler
    public void amountListner(AsyncPlayerChatEvent e) {
        if (e.getPlayer() != player) return;
        if(isDelete) {
            if(e.getMessage().equalsIgnoreCase("Yes") ||
                    e.getMessage().equalsIgnoreCase("Y")) {
                menuControl.setDelete(e.getPlayer(), item);
            }
            e.setCancelled(true);
            menuControl.unregisterChatListner();
            return;
        }
        int amount;

        try {
            amount = Integer.parseInt(e.getMessage());
        } catch (Exception exception) {
            e.getPlayer().sendMessage(ChatColor.RED + "Invalid value.");
            e.setCancelled(true);
            menuControl.unregisterChatListner();
            return;
        }
        e.setCancelled(true);
        item.setAmount(amount);
        menuControl.setAmount(e.getPlayer(), item);
        menuControl.unregisterChatListner();

    }

    public void setDelete() {
        isDelete=true;
    }

    public void setTargetPlayer(Player player, ItemStack target) {
        this.player = player;
        this.item = target;
    }
}