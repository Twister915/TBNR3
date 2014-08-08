package net.tbnr.dev.migrator;

import com.mongodb.*;
import net.cogzmc.core.player.CGroup;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.player.mongo.CMongoGroupRepository;
import net.cogzmc.core.player.mongo.CMongoPlayerRepository;
import net.cogzmc.core.player.mongo.COfflineMongoPlayer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TBNRMigrator {
    /* in core format */
    private final CMongoPlayerRepository target;
    /* in Jake0oo0 format */
    private final CMongoDatabase source;

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("You did not specify at least two databases!");
            return;
        }
        TBNRMigrator tbnrMigrator = new TBNRMigrator(getDatabaseFor(args[0], args[1]), getDatabaseFor(args[2], args[3]));
        tbnrMigrator.start();
    }

    public TBNRMigrator(CMongoDatabase target, CMongoDatabase source) throws DatabaseConnectException {
        target.connect();
        source.connect();
        this.target = new CMongoPlayerRepository(target);
        this.target.setGroupRepository(new CMongoGroupRepository(target, this.target));
        this.source = source;
    }

    public void start() throws DatabaseConnectException {
        DBCollection permplayers = source.getCollection("permplayers");
        Map<String, CGroup> migrateTo = new HashMap<>();
        migrateTo.put("TBNR", target.getGroupRepository().getGroup("TBNR"));
        migrateTo.put("Hero", target.getGroupRepository().getGroup("Hero"));
        migrateTo.put("Premium", target.getGroupRepository().getGroup("Premium"));
        migrateTo.put("JrMod", target.getGroupRepository().getGroup("JrModerator"));
        migrateTo.put("Mod", target.getGroupRepository().getGroup("Moderator"));
        migrateTo.put("JrAdmin", target.getGroupRepository().getGroup("Admin"));
        migrateTo.put("Admin", target.getGroupRepository().getGroup("Admin"));
        migrateTo.put("Owner", target.getGroupRepository().getGroup("Owner"));
        migrateTo.put("Youtuber", target.getGroupRepository().getGroup("VIP"));

        int migrations = 0;
        for (Map.Entry<String, CGroup> migration : migrateTo.entrySet()) {
            for (DBObject player : permplayers.find(new BasicDBObject("group", migration.getKey()))) {
                UUID uuid;
                String uuid1 = (String) player.get("uuid");
                try {
                    uuid = UUID.fromString(uuid1);
                } catch (Exception e) {
                    try {
                        uuid = UUID.fromString(uuid1.replaceAll(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                "$1-$2-$3-$4-$5"));
                    } catch (Exception e1) {
                        continue;
                    }
                }
                COfflinePlayer offlinePlayerByUUID = target.getOfflinePlayerByUUID(uuid);
                if (!offlinePlayerByUUID.isDirectlyInGroup(migration.getValue())) offlinePlayerByUUID.addToGroup(migration.getValue());
                offlinePlayerByUUID.saveIntoDatabase();
                migrations++;
            }
        }

        System.out.println("Migrated " + migrations + " ranks.");

        migrations = 0;
        for (DBObject player : permplayers.find(new BasicDBObject("permissions", "gearz.flight,true"))) {
            UUID uuid;
            String uuid1 = (String) player.get("uuid");
            try {
                uuid = UUID.fromString(uuid1);
            } catch (Exception e) {
                try {
                    uuid = UUID.fromString(uuid1.replaceAll(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                            "$1-$2-$3-$4-$5"));
                } catch (Exception e1) {
                    continue;
                }
            }
            COfflinePlayer offlinePlayerByUUID = target.getOfflinePlayerByUUID(uuid);
            offlinePlayerByUUID.setPermission("hub.perk.flight", true);
            offlinePlayerByUUID.saveIntoDatabase();
            migrations++;
        }

        System.out.println("Migrated " + migrations + " fly perms.");

        migrations = 0;
        for (DBObject dbObject : source.getCollection("users").find(new BasicDBObject("time-online", new BasicDBObject("$gt", 21600000)))) {
            UUID uuid;
            String uuid1 = (String) dbObject.get("uuid");
            try {
                uuid = UUID.fromString(uuid1);
            } catch (Exception e) {
                try {
                    uuid = UUID.fromString(uuid1.replaceAll(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                            "$1-$2-$3-$4-$5"));
                } catch (Exception e1) {
                    continue;
                }
            }
            COfflineMongoPlayer offlinePlayerByUUID = target.getOfflinePlayerByUUID(uuid);
            try {
                Field millisecondsOnline = offlinePlayerByUUID.getClass().getDeclaredField("millisecondsOnline");
                millisecondsOnline.setAccessible(true);
                millisecondsOnline.set(offlinePlayerByUUID, dbObject.get("time-online"));
            } catch (Exception e) {
                continue;
            }
            offlinePlayerByUUID.saveIntoDatabase();
            migrations++;
        }
        System.out.println("Migrated " + migrations + " online time.");
    }

    private static CMongoDatabase getDatabaseFor(String arg, String db) {
        MongoClientURI mongoClientURI = new MongoClientURI(arg);
        System.out.println(mongoClientURI.toString());
        System.out.println(" -" + db);
        return new CMongoDatabase(mongoClientURI, null, db);
    }
}
