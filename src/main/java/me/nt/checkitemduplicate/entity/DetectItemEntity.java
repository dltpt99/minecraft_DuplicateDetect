package me.nt.checkitemduplicate.entity;

import me.nt.checkitemduplicate.function.Serialize;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class DetectItemEntity {
    // uuid meaning of who added this item to check list
    private UUID uuid;
    private ItemStack item;
    private final String item_serialize;
    private int amount;
    private final LocalDateTime date;

    public DetectItemEntity(ItemStack item) {
        setItemStackAmountOne(item);
        this.item_serialize = Serialize.toSerialize(this.item);
        this.date = LocalDateTime.now();
    }

    // using when load from database
    public DetectItemEntity(UUID uuid, String item_serialize, int amount, LocalDateTime date) {
        this.uuid = uuid;
        this.date = date;
        this.item_serialize = item_serialize;
        this.item = Serialize.fromSerialize(this.item_serialize);
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectItemEntity entity = (DetectItemEntity) o;
        return Objects.equals(item, entity.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }

    public void setItemStackAmountOne(ItemStack item) {
        this.amount = item.getAmount();
        this.item = item.clone();
        this.item.setAmount(1);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String getItem_serialize() {
        return item_serialize;
    }

    public int getAmount() {
        return amount;
    }

    public UUID getUuid() {
        return uuid;
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
}
