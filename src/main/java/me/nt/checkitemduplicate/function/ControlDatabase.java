package me.nt.checkitemduplicate.function;

import me.nt.checkitemduplicate.CheckItemDuplicate;
import me.nt.checkitemduplicate.entity.DetectItemEntity;
import me.nt.checkitemduplicate.entity.DetectedPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ControlDatabase {
    CheckItemDuplicate plugin;
    Connection connection;
    String db_host;
    String db_port;
    String db_database;
    String db_username;
    String db_password;


    public ControlDatabase(CheckItemDuplicate plugin) {
        this.plugin = plugin;
        reload_Config();
    }

    public void reload_Config() {
        FileConfiguration config = plugin.getConfig();
        db_host = config.getString("DataBase.DB_HOST");
        db_port = config.getString("DataBase.DB_PORT");
        db_database = config.getString("DataBase.DB_DATABASE");
        db_username = config.getString("DataBase.DB_USERNAME");
        db_password = config.getString("DataBase.DB_PASSWORD");
    }

    public Set<DetectItemEntity> getItemList() {

        Set<DetectItemEntity> result = new HashSet<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps= connection.prepareStatement("SELECT * FROM CheckItems");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        DetectItemEntity entity = new DetectItemEntity(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("item"),
                                rs.getInt("amount"),
                                rs.getTimestamp("date").toLocalDateTime()
                        );
                        result.add(entity);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
        return result;
    }

    public void modifyItemAmount(DetectItemEntity entity) {
        String sql = "UPDATE CheckItems SET amount=" + entity.getAmount() + " WHERE item='"+ entity.getItem_serialize() +"';";
        update(sql);
    }

    public void deleteItemFromList(DetectItemEntity item) {
        String sql = "DELETE FROM CheckItems WHERE item='"+ item.getItem_serialize() +"';";
        update(sql);
    }

    public void addItemtoDB(CommandSender sender, DetectItemEntity entity) {
        String sql = "INSERT INTO CheckItems(uuid, item, amount, date) VALUES (?,?,?,?);";

        update(sql, ((Player)sender).getUniqueId().toString(), entity.getItem_serialize(), entity.getAmount(), entity.getDateAsTimestamp());
        plugin.init_Data();
    }

    public Set<DetectedPlayerEntity> getDetectedPlayer() {

        Set<DetectedPlayerEntity> output= new HashSet<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps= connection.prepareStatement("SELECT * FROM DetectedPlayers");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        DetectedPlayerEntity entity = new DetectedPlayerEntity(
                                UUID.fromString(rs.getString("uuid")),
                                Serialize.fromSerialize(rs.getString("item")),
                                rs.getInt("amount"),
                                new Location(Bukkit.getWorld(rs.getString("world")),
                                        rs.getInt("loc_x"),
                                        rs.getInt("loc_y"),
                                        rs.getInt("loc_z")),
                                rs.getString("server"),
                                rs.getTimestamp("date").toLocalDateTime()
                        );
                        output.add(entity);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    plugin.getLogger().info("Error casued on getDetectedPlayer");
                }
            }
        }.runTaskAsynchronously(plugin);

        return output;
    }

    public void clearDetectedPlayerList() {
        String sql = "DELETE FROM DetectedPlayers;";
        update(sql);
    }

    public void addDetectedPlayertoDB(DetectedPlayerEntity player) {
        String sql = "INSERT INTO DetectedPlayers(uuid, item, amount, server, world, loc_x, loc_y, loc_z, date) VALUES(?,?,?,?,?,?,?,?,?);";
        update(sql, player.getUuid().toString(), Serialize.toSerialize(player.getItem()), player.getAmount(),
                player.getServer(), player.getLoc().getWorld().getName(),
                player.getLoc().getX(), player.getLoc().getY(), player.getLoc().getZ(),
                player.getDateAsTimestamp());
    }

    public void connect() throws SQLException, ClassNotFoundException {
        if(isConnected()) return;

        String db_url = "jdbc:mysql://" + this.db_host + ":" + db_port + "/" + db_database + "?useSSL=false";
        Bukkit.getLogger().info("CONNECT URL : " + db_url);
        connection = DriverManager.getConnection(db_url, db_username, db_password);
    }

    public void update(String sql, Object...args) {

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement ps = connection.prepareStatement(sql);

                    for (int i = 1; i <= args.length; i++) {
                        if(args[i-1] == null) {
                            Bukkit.getLogger().info("Data Serialize에 실패하였습니다.");
                            return;
                        }
                        ps.setObject(i, args[i - 1]);
                    }
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    plugin.getLogger().info("Error casued on update");
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    public void disconnect() {
        if(!isConnected()) return;

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reconnect(CommandSender sender) {
        if(isConnected()){
            sender.sendMessage("이미 DB에 연결되어 있습니다.");
            return;
        }
        try {
            connect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void setUpTable() {
        String createItemsTableQuery = "CREATE TABLE IF NOT EXISTS CheckItems("
                + "uuid VARCHAR(36) NOT NULL, "
                + "item TEXT NOT NULL, "
                + "amount INT NOT NULL, "
                + "date TIMESTAMP NOT NULL);";

        String createDetectedPlayerTableQuery = "CREATE TABLE IF NOT EXISTS DetectedPlayers("
                + "uuid VARCHAR(36) NOT NULL, "
                + "item TEXT NOT NULL, "
                + "amount INT NOT NULL, "
                + "server VARCHAR(30) NOT NULL, "
                + "world VARCHAR(30) NOT NULL, "
                + "loc_x REAL NOT NULL, "
                + "loc_y REAL NOT NULL, "
                + "loc_z REAL NOT NULL, "
                + "date TIMESTAMP NOT NULL) ;";
        update(createDetectedPlayerTableQuery);
        update(createItemsTableQuery);
    }

}