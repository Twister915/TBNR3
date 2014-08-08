package net.tbnr.dev.spawn;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.parkour.ParkourSession;
import net.tbnr.dev.util.MongoToolsHub;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.World;

@Data
@Getter(AccessLevel.NONE)
@Setter(AccessLevel.NONE)
/**
 * lazy db implementation, laugh at me
 * - Joey
 */
public final class SpawnManager {
    private final CMongoDatabase mongoDatabase;
    private Location _spawn;
    private ObjectId id;

    public SpawnManager(World world, CMongoDatabase database) {
        this.mongoDatabase = database;
        DBCollection hub_junk = database.getCollection("hub_junk");
        DBObject spawnObject = hub_junk.findOne(new BasicDBObject("key", "spawn"));
        if (spawnObject == null) _spawn = world.getSpawnLocation();
        else {
            _spawn = MongoToolsHub.pointFrom(spawnObject).getLocation(world);
            id = (ObjectId) spawnObject.get("_id");
        }
    }

    public Location getSpawn() {
        return _spawn;
    }

    public void setSpawn(Location location) {
        _spawn = location;
    }

    public void onDisable() {
        DBObject dbObject = MongoToolsHub.pointForDB(Point.of(_spawn));
        dbObject.put("key", "spawn");
        if (id != null) dbObject.put("_id", id);
        mongoDatabase.getCollection("hub_junk").save(dbObject);
    }

    public void teleportToSpawn(CPlayer player) {
        ParkourSession parkourFor = TBNRHub.getInstance().getParkourManager().getParkourFor(player);
        if (parkourFor != null) {
            parkourFor.cleanupParkour();
        }
        Location clone = getSpawn().clone();
        Point point = player.getPoint();
        clone.setPitch(point.getPitch());
        clone.setYaw(point.getYaw());
        player.getBukkitPlayer().teleport(clone);
        player.sendMessage(TBNRHub.getInstance().getFormat("teleported-to-spawn"));
    }
}
