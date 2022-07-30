package me.nt.checkitemduplicate;

import me.nt.checkitemduplicate.command.CommandHeader;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import me.nt.checkitemduplicate.entity.DetectedPlayerEntity;
import me.nt.checkitemduplicate.function.Announcer;
import me.nt.checkitemduplicate.function.ControlDatabase;
import me.nt.checkitemduplicate.function.Page;
import me.nt.checkitemduplicate.listner.MenuControl;
import me.nt.checkitemduplicate.listner.DetectHaveItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class CheckItemDuplicate extends JavaPlugin {

    CommandHeader command;
    MenuControl menuControl;
    DetectHaveItem detectHaveItem;
    Set<DetectItemEntity> detect_Item;
    Set<DetectedPlayerEntity> detected_Player;
    FileConfiguration config;
    String server_name;
    ControlDatabase database;
    Announcer announcer;
    HashMap<Player, Page> page;

    @Override
    public void onEnable() {

        init_Config();
        database = new ControlDatabase(this);

        command = new CommandHeader(this);
        this.getCommand("dd").setExecutor(command);

        menuControl = new MenuControl(this);
        Bukkit.getPluginManager().registerEvents(menuControl, this);

        detectHaveItem = new DetectHaveItem(this);
        Bukkit.getPluginManager().registerEvents(detectHaveItem, this);

        announcer = new Announcer(this, config.getInt("Config.Announcer_Cooldown"));

        init_DB();
        init_Data();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.disconnect();
    }

    public void init_Data() {
        if(!database.isConnected()) {
            detect_Item = new HashSet<>();
            detected_Player = new HashSet<>();
            detectHaveItem.updateItemList(detect_Item);
            return;
        }

        detect_Item = database.getItemList();
        detected_Player = database.getDetectedPlayer();

        if(detect_Item==null) {
            detected_Player = new HashSet<>();
        }

        if (detected_Player == null) {
            detected_Player = new HashSet<>();
        }

        page = new HashMap<>();
        detectHaveItem.updateItemList(detect_Item);
    }

    public void deleteItemFromList(DetectItemEntity item) {
        database.deleteItemFromList(item);
        this.detect_Item.remove(item);
        Bukkit.getLogger().info("삭제시도 한 아이템 : "+item);
    }

    private void init_DB() {
        if(database.isConnected()) {
            return;
        }
        try {
            database.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("failed to database connect");
            Bukkit.getLogger().info(e.getMessage());
        }

        if(database.isConnected()) {
            Bukkit.getLogger().info("Success to database connect");
            database.setUpTable();
        }
    }

    public void init_Config() {
        this.saveDefaultConfig();
        this.config = getConfig();
        this.server_name = config.getString("Config.Server_Name");
    }

    public void reload_Config() {
        this.config = getConfig();
        command.reload_Config();
        announcer.updateConfig();
        this.server_name = config.getString("Config.Server_Name");
    }

    public Set<DetectItemEntity> getDetectItemList() {
        return detect_Item;
    }

    public void addToDetectItemList(DetectItemEntity item) {
        detect_Item.add(item);
    }

    public Set<DetectedPlayerEntity> getDetected_Player() {
        return detected_Player;
    }

    public void addDetected_Player(DetectedPlayerEntity entity) {
        this.detected_Player.add(entity);
        database.addDetectedPlayertoDB(entity);
        announcer.announce_DetectedNews(entity);
        announcer.updateDetected_Players(detected_Player);

    }

    public void updateEntities() {
        this.detected_Player = database.getDetectedPlayer();
        this.detect_Item = database.getItemList();
        detectHaveItem.updateItemList(detect_Item);
        announcer.updateDetected_Players(detected_Player);
    }

    public void clearDetected_Player() {
        detected_Player = new HashSet<>();
    }

    public ControlDatabase getDatabase() {
        return database;
    }

    public String getServer_name() {
        return server_name;
    }

    public void setPage(HashMap<Player, Page> page) {
        this.page = page;
    }

    public HashMap<Player, Page> getPage() {
        return page;
    }

}
