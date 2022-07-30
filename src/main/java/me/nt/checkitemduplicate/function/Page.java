package me.nt.checkitemduplicate.function;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import me.nt.checkitemduplicate.listner.MenuControl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Page {
    final private ArrayList<ItemStack>[] item_list;
    final private int page_size;
    private int current_page;
    final private Player player;
    final private String inv_title;

    public Page(ArrayList<ItemStack>[] item_list, Player player, String inv_title) {
        this.item_list = item_list;
        this.page_size = item_list.length;
        this.current_page = 1;
        this.player = player;
        this.inv_title = inv_title+" ";
    }

    public void showPage() {
        Inventory inv = Bukkit.createInventory(null, 54, "§0DD - " + inv_title + current_page);
        int i =0;
        for(ItemStack item : item_list[current_page]) {
            inv.setItem(i++, item);
        }
        if(!(current_page==1 && item_list[0].size()<45)) {
            if(current_page==1) {
                inv.setItem(50, getArrowNext());
            } else if(current_page==page_size) {
                inv.setItem(48, getArrowBefore());
            } else {
                inv.setItem(48, getArrowBefore());
                inv.setItem(50, getArrowNext());
            }
        }
        player.openInventory(inv);
    }

    public void nextPage() {
        current_page++;
        player.closeInventory();
        showPage();
    }

    public void beforePage() {
        current_page--;
        player.closeInventory();
        showPage();
    }

    public List<ItemStack>[] getItem_list() {
        return item_list;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public ItemStack getArrowNext() {
        ItemStack arrow_next = new ItemStack(Material.ARROW, 1);
        ItemMeta arrow_next_meta = arrow_next.getItemMeta();
        arrow_next_meta.setDisplayName("[다음 페이지]");
        arrow_next.setItemMeta(arrow_next_meta);
        return arrow_next;
    }

    public ItemStack getArrowBefore() {
        ItemStack arrow_before = new ItemStack(Material.ARROW, 1);
        ItemMeta arrow_before_meta = arrow_before.getItemMeta();
        arrow_before_meta.setDisplayName("[이전 페이지]");
        arrow_before.setItemMeta(arrow_before_meta);
        return arrow_before;
    }


}
