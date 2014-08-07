package net.tbnr.dev.sg.game.map;

import com.google.common.collect.ImmutableSet;
import com.mongodb.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.maps.CoreMaps;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.util.Point;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.cogzmc.core.player.mongo.MongoUtils.getListFor;
import static net.cogzmc.core.player.mongo.MongoUtils.getValueFrom;

@Data
public final class SGMongoMapManager {
    private final String MAPS_COLLECTION = "survivalgames_maps";

    private final CMongoDatabase database;
    @Getter(AccessLevel.NONE) private final Set<SGMap> maps = new HashSet<>();

    public void reloadMaps() {
        maps.clear();
        DBCollection collection = database.getCollection(MAPS_COLLECTION);
        for (DBObject dbObject : collection.find()) {
            SGMap sgMap = mapFromDB(dbObject);
            if (sgMap == null) continue;
            maps.add(sgMap);
        }
    }

    public ImmutableSet<SGMap> getMaps() {
        return ImmutableSet.copyOf(maps);
    }

    public void saveMap(SGMap map) {
        maps.add(map);
        database.getCollection(MAPS_COLLECTION).save(objectFromMap(map));
    }

    private static enum SGMapKeys {
        MAP_ID,
        NAME,
        AUTHOR,
        SOCIAL_LINK,
        CORNICOPIA_SPAWN_POINTS,
        TIER_1_CHESTS,
        TIER_2_CHESTS,
        CORNICOPIA_CHESTS,
        DEATHMATCH_SPAWN,
        X,
        Y,
        Z,
        PITCH,
        YAW;

        @Override
        public String toString() {
            return name();
        }
    }

    private static SGMap mapFromDB(DBObject object) {
        CMap mapByID = CoreMaps.getInstance().getMapManager().getMapByID(UUID.fromString(getValueFrom(object, SGMapKeys.MAP_ID, String.class)));
        if (mapByID == null) return null;
        String name = getValueFrom(object, SGMapKeys.NAME, String.class);
        String author = getValueFrom(object, SGMapKeys.AUTHOR, String.class);
        String socialLink = getValueFrom(object, SGMapKeys.SOCIAL_LINK, String.class);
        Set<Point> cornPoints = getPointsFrom(getListFor(getValueFrom(object, SGMapKeys.CORNICOPIA_SPAWN_POINTS, BasicDBList.class), DBObject.class));
        Set<Point> tier1 = getPointsFrom(getListFor(getValueFrom(object, SGMapKeys.TIER_1_CHESTS, BasicDBList.class), DBObject.class));
        Set<Point> tier2 = getPointsFrom(getListFor(getValueFrom(object, SGMapKeys.TIER_2_CHESTS, BasicDBList.class), DBObject.class));
        Set<Point> cornChests = getPointsFrom(getListFor(getValueFrom(object, SGMapKeys.CORNICOPIA_CHESTS, BasicDBList.class), DBObject.class));
        Set<Point> deathmatchSpawn = getPointsFrom(getListFor(getValueFrom(object, SGMapKeys.DEATHMATCH_SPAWN, BasicDBList.class), DBObject.class));
        return new SGMap(mapByID, name, author, socialLink, cornPoints, tier1, tier2, cornChests, deathmatchSpawn);
    }

    private static DBObject objectFromMap(SGMap map) {
        if (map.getMap() == null) throw new IllegalArgumentException("You must have a CMap linked to the SG map");
        BasicDBObjectBuilder objectBuilder = new BasicDBObjectBuilder();
        objectBuilder.add(SGMapKeys.MAP_ID.toString(), map.getMap().getMapId().toString());
        objectBuilder.add(SGMapKeys.NAME.toString(), map.getName());
        objectBuilder.add(SGMapKeys.AUTHOR.toString(), map.getAuthor());
        objectBuilder.add(SGMapKeys.SOCIAL_LINK.toString(), map.getSocialLink());
        objectBuilder.add(SGMapKeys.CORNICOPIA_SPAWN_POINTS.toString(), getPointList(map.getCornicopiaSpawnPoints()));
        objectBuilder.add(SGMapKeys.TIER_1_CHESTS.toString(), getPointList(map.getTier1chests()));
        objectBuilder.add(SGMapKeys.TIER_2_CHESTS.toString(), getPointList(map.getTier2chests()));
        objectBuilder.add(SGMapKeys.CORNICOPIA_CHESTS.toString(), getPointList(map.getCornicopiaChests()));
        objectBuilder.add(SGMapKeys.DEATHMATCH_SPAWN.toString(), getPointList(map.getDeathmatchSpawn()));
        return objectBuilder.get();
    }

    private static Set<Point> getPointsFrom(Collection<DBObject> objects) {
        Set<Point> objects1 = new HashSet<>();
        for (DBObject object : objects) {
            objects1.add(Point.of(
                    getNumFor(object, SGMapKeys.X),
                    getNumFor(object, SGMapKeys.Y),
                    getNumFor(object, SGMapKeys.Z),
                    getNumFor(object, SGMapKeys.PITCH).floatValue(),
                    getNumFor(object, SGMapKeys.YAW).floatValue()
            ));
        }
        return objects1;
    }

    private static BasicDBList getPointList(Collection<Point> points) {
        BasicDBList objects = new BasicDBList();
        for (Point point : points) {
            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put(SGMapKeys.X.toString(), point.getX());
            basicDBObject.put(SGMapKeys.Y.toString(), point.getY());
            basicDBObject.put(SGMapKeys.Z.toString(), point.getZ());
            basicDBObject.put(SGMapKeys.PITCH.toString(), point.getPitch());
            basicDBObject.put(SGMapKeys.YAW.toString(), point.getYaw());
            objects.add(basicDBObject);
        }
        return objects;
    }

    private static Double getNumFor(DBObject object, Object key) {
        return ((Double)object.get(key.toString()));
    }
}
