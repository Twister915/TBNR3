package net.tbnr.dev.inventory.player;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.util.MongoToolsHub;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Data
@Getter(AccessLevel.NONE)
public final class WarpMongoRepository implements Iterable<Warp> {
    private final CMongoDatabase mongoDatabase;
    private final Set<Warp> warps = new HashSet<>();

    public void reloadWarps() {
        warps.clear();
        for (DBObject hub_warps : mongoDatabase.getCollection("hub_warps").find()) {
            warps.add(warpFromDBObject(hub_warps));
        }
    }

    public void saveWarp(Warp w) {
        warps.add(w);
        mongoDatabase.getCollection("hub_warps").save(getObjectForWarp(w));
    }

    private static final String POINT = "warp_point";
    private static final String POSITION = "pos";
    private static final String NAME = "name";
    private static final String MATERIALS = "material_list";


    private static DBObject getObjectForWarp(Warp warp) {
        BasicDBObjectBuilder objectBuilder = new BasicDBObjectBuilder();
        objectBuilder.add(POINT, MongoToolsHub.pointForDB(warp.getPoint()));
        objectBuilder.add(NAME, warp.getName());
        BasicDBList list = new BasicDBList();
        for (Material material : warp.getMaterials()) {
            list.add(material.name());
        }
        objectBuilder.add(MATERIALS, list);
        objectBuilder.add(POSITION, warp.getPosition());
        return objectBuilder.get();
    }

    private static Warp warpFromDBObject(DBObject object) {
        Point point = MongoToolsHub.pointFrom((DBObject) object.get(POINT));
        String name = (String) object.get(NAME);
        BasicDBList materials = (BasicDBList) object.get(MATERIALS);
        Material[] materilz = new Material[materials.size()];
        for (int i = 0; i < materials.size(); i++) {
            materilz[i] = Material.valueOf((String)materials.get(i));
        }
        Integer position = (Integer) object.get(POSITION);
        return new Warp(point, name, position, materilz);
    }



    @Override
    public Iterator<Warp> iterator() {
        return warps.iterator();
    }
}
