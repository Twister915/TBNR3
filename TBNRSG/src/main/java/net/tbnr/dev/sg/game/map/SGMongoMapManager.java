package net.tbnr.dev.sg.game.map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mongodb.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.maps.CoreMaps;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.sg.game.PreGameLobby;

import java.util.*;

import static net.cogzmc.core.player.mongo.MongoUtils.getValueFrom;

@Data
public final class SGMongoMapManager {
    private final String MAPS_COLLECTION = "survivalgames_maps";

    private final CMongoDatabase database;
    @Getter(AccessLevel.NONE) private final Set<SGMap> maps = new HashSet<>();
    private PreGameLobby preGameLobby;

    public void reloadMaps() {
        maps.clear();
        DBCollection collection = database.getCollection(MAPS_COLLECTION);
        for (DBObject dbObject : collection.find()) {
            try {
                if (dbObject.containsField(SGMapKeys.PRE_GAME_LOBBY_FLAG.toString())) {
                    preGameLobby = gameLobbyFromDB(dbObject);
                    continue;
                }
                SGMap sgMap = mapFromDB(dbObject);
                if (sgMap == null) continue;
                maps.add(sgMap);
            } catch (Throwable t) {
                t.printStackTrace();
                continue;
            }
        }
        if (preGameLobby == null) throw new IllegalStateException("There is no pre-game lobby defined!");
    }

    public ImmutableSet<SGMap> getMaps() {
        return ImmutableSet.copyOf(maps);
    }

    public void saveMap(SGMap map) {
        maps.add(map);
        database.getCollection(MAPS_COLLECTION).save(objectFromMap(map));
    }

    public void savePreGameLobby(PreGameLobby lobby) {
        preGameLobby = lobby;
        database.getCollection(MAPS_COLLECTION).save(objectForPreGameLobby(lobby));
    }

    public List<SGMap> getRandomMaps(Integer size) {
        List<SGMap> maps = new ArrayList<>();
        ImmutableList<SGMap> maps1 = getMaps().asList();
        while (maps.size() < size) {
            SGMap map;
            do {
                map = maps1.get(Core.getRandom().nextInt(maps1.size()));
            } while (maps.contains(map));
            maps.add(map);
        }
        return maps;
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
        YAW,
        PRE_GAME_LOBBY_FLAG,
        PRE_GAME_SPAWN,
        PRE_GAME_VILLAGER;

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
        Set<Point> cornPoints = getPointsFrom((BasicDBList)object.get(SGMapKeys.CORNICOPIA_SPAWN_POINTS.toString()));
        Set<Point> tier1 = getPointsFrom((BasicDBList)object.get(SGMapKeys.TIER_1_CHESTS.toString()));
        Set<Point> tier2 = getPointsFrom((BasicDBList)object.get(SGMapKeys.TIER_2_CHESTS.toString()));
        Set<Point> cornChests = getPointsFrom((BasicDBList)object.get(SGMapKeys.CORNICOPIA_CHESTS.toString()));
        Set<Point> deathmatchSpawn = getPointsFrom((BasicDBList)object.get(SGMapKeys.DEATHMATCH_SPAWN.toString()));
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

    private static PreGameLobby gameLobbyFromDB(DBObject object) {
        CMap mapByID = CoreMaps.getInstance().getMapManager().getMapByID(UUID.fromString(getValueFrom(object, SGMapKeys.MAP_ID, String.class)));
        if (mapByID == null) return null;
        Set<Point> spawn = getPointsFrom((BasicDBList)object.get(SGMapKeys.PRE_GAME_SPAWN.toString()));
        Set<Point> villager = getPointsFrom((BasicDBList)object.get(SGMapKeys.PRE_GAME_VILLAGER.toString()));
        return new PreGameLobby(mapByID, spawn, villager);
    }

    private static DBObject objectForPreGameLobby(PreGameLobby gameLobby) {
        BasicDBObjectBuilder objectBuilder = new BasicDBObjectBuilder();
        objectBuilder.add(SGMapKeys.PRE_GAME_SPAWN.toString(), getPointList(gameLobby.getSpawnPoints()));
        objectBuilder.add(SGMapKeys.PRE_GAME_VILLAGER.toString(), getPointList(gameLobby.getPerkVillagers()));
        objectBuilder.add(SGMapKeys.MAP_ID.toString(), gameLobby.getMap().getMapId().toString());
        objectBuilder.add(SGMapKeys.PRE_GAME_LOBBY_FLAG.toString(), true);
        return objectBuilder.get();
    }

    private static Set<Point> getPointsFrom(BasicDBList objects) {
        Set<Point> objects1 = new HashSet<>();
        for (Object object1 : objects) {
            BasicDBObject object = (BasicDBObject)object1;
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
