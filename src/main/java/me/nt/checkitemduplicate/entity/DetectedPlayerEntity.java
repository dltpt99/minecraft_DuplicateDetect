package me.nt.checkitemduplicate.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class DetectedPlayerEntity {
    private UUID uuid;
    private ItemStack item;
    private int amount;
    private Location loc;
    private final String server;
    private final LocalDateTime date;

    // using when Load Data from Database
    public DetectedPlayerEntity(UUID uuid, ItemStack item, int amount, Location loc, String server, LocalDateTime date) {
        this.uuid = uuid;
        this.item = item;
        this.amount = amount;
        this.loc = loc;

        this.loc.setX((int)loc.getX());
        this.loc.setY((int)loc.getY());
        this.loc.setZ((int)loc.getZ());

        this.server = server;
        this.date = date;
    }

    // using when Player Detected
    public DetectedPlayerEntity(UUID uuid, ItemStack item, int amount, String server, Location loc) {
        this.uuid = uuid;
        this.item = item;
        this.amount = amount;
        this.loc = loc;

        this.loc.setX((int)loc.getX());
        this.loc.setY((int)loc.getY());
        this.loc.setZ((int)loc.getZ());

        this.server = server;
        this.date = LocalDateTime.now();

    }

    public String getServer() {
        return server;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Timestamp getDateAsTimestamp() {
        return Timestamp.valueOf(date);
    }

    public String getDateAsFormatted() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(this.date);
    }

//    uuid, item, amount, world, loc_x, loc_y, loc_z, date, time
}
