package net.tbnr.dev.migrator;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClientURI;
import net.cogzmc.core.player.CGroup;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.cogzmc.core.player.mongo.CMongoGroupRepository;
import net.cogzmc.core.player.mongo.CMongoPlayerRepository;

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
        DBCollection permPlayers = source.getCollection("permplayers");
        DBCollection permGroups = source.getCollection("permgroups");
        Map<String, CGroup> newBindings = new HashMap<>();
        Map<CGroup, DBObject> oldBindings = new HashMap<>();
        for (DBObject dbObject : permGroups.find()) {
            CGroup name = target.getGroupRepository().createNewGroup((String) dbObject.get("name"));
            name.setTablistColor((String) dbObject.get("tabcolor"));
            name.setChatPrefix((String) dbObject.get("prefix"));
            for (Object permission : ((BasicDBList) dbObject.get("permissions"))) {
                String[] split = ((String) permission).split(",");
                name.setPermission(split[0], Boolean.valueOf(split[1]));
            }
            newBindings.put(name.getName(), name);
            oldBindings.put(name, dbObject);
        }
        for (CGroup cGroup : oldBindings.keySet()) {
            DBObject dbObject = oldBindings.get(cGroup);
            BasicDBList inheritances = (BasicDBList) dbObject.get("inheritances");
            for (Object inheritance : inheritances) {
                cGroup.addParent(newBindings.get(inheritance));
            }
        }
        target.getGroupRepository().setDefaultGroup(newBindings.get("Player"));
        target.getGroupRepository().save();
        for (DBObject dbObject : permPlayers.find()) {
            COfflinePlayer player;
            try {
                player = target.getOfflinePlayerByUUID(UUID.fromString((String) dbObject.get("uuid")));
            } catch (Exception e) {
                String uuid = ((String) dbObject.get("uuid")).replaceAll(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5");
                try {
                    player = target.getOfflinePlayerByUUID(UUID.fromString(uuid));
                } catch (Exception e2) {
                    System.err.println("Could not transition " + dbObject.get("uuid"));
                    e2.printStackTrace();
                    continue;
                }
            }
            CGroup group = newBindings.get(dbObject.get("group"));
            BasicDBList permissions = (BasicDBList) dbObject.get("permissions");
            boolean b = target.getGroupRepository().isDefaultGroup(group);
            if (b && permissions.size() == 0) continue;
            if (!b) player.addToGroup(group);
            for (Object permissionRaw : permissions) {
                String[] split = ((String) permissionRaw).split(",");
                String permission = split[0];
                Boolean value = split.length < 2 ? true : Boolean.valueOf(split[1]);
                if (permission.equals("gearz.flight") || permission.equals("gearz.fly")) {
                    player.setPermission("hub.perk.flight", value);
                } else {
                    player.setPermission(permission, value);
                }
            }
            player.saveIntoDatabase();
        }
    }

    private static CMongoDatabase getDatabaseFor(String arg, String db) {
        MongoClientURI mongoClientURI = new MongoClientURI(arg);
        System.out.println(mongoClientURI.toString());
        System.out.println(" -" + db);
        return new CMongoDatabase(mongoClientURI, null, db);
    }
}
