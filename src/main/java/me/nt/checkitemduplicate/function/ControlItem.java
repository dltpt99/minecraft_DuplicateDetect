package me.nt.checkitemduplicate.function;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ControlItem {
    CheckItemDuplicate plugin;
    String add_Success_msg;
    String add_Fail_No_Items_In_Your_Hand;
    String add_Fail_Already_Registered_Item;
    List<String> display_lore;
    ControlDatabase database;

    public ControlItem(CheckItemDuplicate plugin) {
        this.plugin = plugin;
        this.database = this.plugin.getDatabase();
        reloadConfig();
    }

    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        add_Success_msg = config.getString("Message.Add_Success");
        add_Fail_No_Items_In_Your_Hand = config.getString("Message.Add_Fail_No_Items_In_Your_Hand");
        add_Fail_Already_Registered_Item = config.getString("Message.Add_Fail_Already_Registered_Item");
        display_lore = config.getStringList("GUI.DD_Item.Lore");
    }

    public void addItem(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(ChatColor.BLUE +"/dd add [Amount]");
            return;
        }
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (Exception e) {
            sender.sendMessage("/dd add [Amount]");
            return;
        }
        Player player = (Player) sender;
        ItemStack item_temp = player.getInventory().getItemInMainHand();
        ItemMeta item_meta = item_temp.getItemMeta();

        ItemStack item = new ItemStack(item_temp.getType(), 1);
        item.setItemMeta(item_temp.getItemMeta());
        item.setItemMeta(item_meta);
        item.setDurability(item_temp.getDurability());
        item.setAmount(amount);
        DetectItemEntity entity = new DetectItemEntity(item);

        if (item.getData().getItemType() == Material.AIR) {
            sender.sendMessage(add_Fail_No_Items_In_Your_Hand);
            return;
        }

        if (plugin.getDetectItemList().contains(entity)) {
            sender.sendMessage(ChatColor.RED+"이미 등록된 아이템 입니다!");
            return;
        }

        if( database == null ) {
            sender.sendMessage("databse가 null입니다");
            return;
        }

        database.addItemtoDB(sender, entity);
        plugin.addToDetectItemList(entity);

        String send_msg = add_Success_msg;
        if(send_msg.contains("%Item_Name%")) {
            if(item.getItemMeta().getDisplayName() == null) {
                send_msg = send_msg.replaceAll("%Item_Name%",item.getType().name());
            } else {
                send_msg = send_msg.replaceAll("%Item_Name%",item.getItemMeta().getDisplayName());
            }
        }

        if(send_msg.contains("%Amount%")) {
            send_msg = send_msg.replaceAll("%Amount%", Integer.toString(amount));
        }
        sender.sendMessage(send_msg);
    }

    public void showItemList(CommandSender sender) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<DetectItemEntity> itemList = plugin.getDetectItemList();
                int page_num = (itemList.size() / 45) + 1;
                int page_index = 0;
                @SuppressWarnings("unchecked")
                ArrayList<ItemStack>[] item_per_page = new ArrayList[page_num];
                Arrays.setAll(item_per_page, ArrayList<ItemStack> :: new);

                int i = 0;

                Iterator<DetectItemEntity> iter = itemList.stream()
                        .sorted(Comparator.comparing(DetectItemEntity::getDate)
                                .reversed())
                        .iterator();

                while (iter.hasNext()) {
                    DetectItemEntity itemEntity = iter.next();

                    if(i++>=45){
                        page_index++;
                        i = 0;
                    }

                    ItemStack item = itemEntity.getItem().clone();
                    ItemMeta item_meta = item.getItemMeta();

                    List<String> lore_backup = item_meta.getLore();
                    List<String> lores = new ArrayList<>(display_lore);
                    int index = 0;

                    for (String lore : lores) {
                        if(lore.contains("%Player_Name")) {
                            if(plugin.getServer().getPlayer(itemEntity.getUuid()) !=null) {
                                lore = lore.replaceAll("%Player_Name%", Bukkit.getPlayer(itemEntity.getUuid()).getName());
                            }else {
                                lore = lore.replaceAll("%Player_Name%", Bukkit.getOfflinePlayer(itemEntity.getUuid()).getName());
                            }
                        }

                        if(lore.contains("%Amount%"))  {
                            lore = lore.replaceAll("%Amount%", itemEntity.getAmount()+"");
                        }

                        if (lore.contains("%Time%")) {
                            lore = lore.replaceAll("%Time%", itemEntity.getDateAsFormatted());
                        }

                        lores.set(index, lore);
                        index++;
                    }

                    if(lore_backup==null) {
                        lore_backup = new ArrayList<>();
                    }

                    lore_backup.addAll(lores);
                    item_meta.setLore(lore_backup);

                    item.setItemMeta(item_meta);
                    item_per_page[page_index].add(item);

                }
                plugin.getPage().put((Player)sender, new Page(item_per_page, (Player) sender, "Item", plugin));
                plugin.getPage().get((Player)sender).showPage();

            }
        }.runTask(plugin);

    }
}
