package net.tbnr.dev.signs;

import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import lombok.Data;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.player.mongo.MongoUtils;
import net.cogzmc.core.util.Region;
import net.tbnr.dev.Game;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.util.MongoToolsHub;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

@Data
public final class ServerSignMatrixManager {
    private final static String SIGNS_COLLECTION = "hub_signs";

    private final CMongoDatabase database;
    private final Set<ServerSignMatrix> matrixes = new HashSet<>();
    private BukkitTask updateTask;

    {
        TBNRHub.getInstance().registerListener(new ServerSignListener(this));
    }

    public void reload() {
        if (updateTask != null) updateTask.cancel();
        matrixes.clear();
        for (DBObject dbObject : database.getCollection(SIGNS_COLLECTION).find()) {
            ServerSignMatrix matrixFrom = getMatrixFrom(dbObject);
            if (matrixFrom == null) continue;
            matrixes.add(matrixFrom);
        }
        updateTask = Bukkit.getScheduler().runTaskTimer(TBNRHub.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (ServerSignMatrix matrix : matrixes) {
                    matrix.update();
                }
            }
        }, 40L, 40L);
    }

    public void save(ServerSignMatrix matrix) {
        database.getCollection(SIGNS_COLLECTION).save(getDBObjectFrom(matrix));
        matrixes.add(matrix);
    }

    public ImmutableSet<ServerSignMatrix> getMatricies() {
        return ImmutableSet.copyOf(matrixes);
    }

    private static final String REGION_MIN = "REGION_MIN";
    private static final String REGION_MAX = "REGION_MAX";
    private static final String GAME = "GAME";
    private static final String WORLD = "WORLD";

    private static ServerSignMatrix getMatrixFrom(DBObject object) {
        World world = Bukkit.getWorld((String) object.get(WORLD));
        if (world == null) return null;
        Game game = Game.valueOf((String) object.get(GAME));
        Region r = new Region(MongoToolsHub.pointFrom((DBObject) object.get(REGION_MIN)), MongoToolsHub.pointFrom((DBObject) object.get(REGION_MAX)));
        return new ServerSignMatrix(r, world, game);
    }

    private static DBObject getDBObjectFrom(ServerSignMatrix matrix) {
        BasicDBObjectBuilder objectBuilder = new BasicDBObjectBuilder();
        objectBuilder.add(GAME, matrix.getGame().name());
        objectBuilder.add(REGION_MAX, MongoToolsHub.pointForDB(matrix.getRegion().getMax()));
        objectBuilder.add(REGION_MIN, MongoToolsHub.pointForDB(matrix.getRegion().getMin()));
        objectBuilder.add(WORLD, matrix.getWorld().getName());
        return objectBuilder.get();
    }
}
