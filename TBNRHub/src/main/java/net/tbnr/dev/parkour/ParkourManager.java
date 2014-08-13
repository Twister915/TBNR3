package net.tbnr.dev.parkour;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.AbstractMobNPC;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.effect.npc.NPCObserver;
import net.cogzmc.core.effect.npc.mobs.MobNPCVillager;
import net.cogzmc.core.json.PointSerializer;
import net.cogzmc.core.json.RegionSerializer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.setting.PlayerSetting;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.joda.time.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public final class ParkourManager implements Listener, CPlayerConnectionListener {
    private static final String PARKOUR_FOLDER = "parkour";

    private final Map<CPlayer, ParkourSession> sessions = new WeakHashMap<>();
    private final List<Parkour> parkours = new ArrayList<>();
    private final Map<Parkour, MobNPCVillager> villagers = new HashMap<>();

    public ParkourManager() {
        TBNRHub.getInstance().registerListener(this);
        Core.getPlayerManager().registerCPlayerConnectionListener(this);
        File file = new File(TBNRHub.getInstance().getDataFolder(), PARKOUR_FOLDER);
        String[] list = getParkourFiles();
        for (String s : list) {
            File parkourFile = new File(file, s);
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(parkourFile));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                String s1 = builder.toString().trim();
                JSONObject parse = (JSONObject) JSONValue.parse(s1);
                Parkour parkourLevels = deserializeParkour(parse);
                parkours.add(parkourLevels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TBNRHub.getInstance().logMessage("Loaded " + parkours.size() + " parkours!");
        for (Parkour parkour : parkours) {
            setupParkour(parkour);
        }
    }

    private String[] getParkourFiles() {
        File file = new File(TBNRHub.getInstance().getDataFolder(), PARKOUR_FOLDER);
        if (!file.exists()) file.mkdir();
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
    }

    public void save() {
        File file = new File(TBNRHub.getInstance().getDataFolder(), PARKOUR_FOLDER);
        for (String s : getParkourFiles()) {
            File file1 = new File(file, s);
            file1.delete();
        }
        for (int x = 0; x < parkours.size(); x++) {
            Parkour parkourLevels = parkours.get(x);
            String s = serializeParkour(parkourLevels).toJSONString();
            try {
                File file1 = new File(file, x + ".json");
                file1.createNewFile();
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file1));
                bufferedWriter.write(s);
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Point to = Point.of(event.getTo());
        CPlayer player = Core.getOnlinePlayer(event.getPlayer());
        if (sessions.containsKey(player)) return;
        for (Parkour parkour : parkours) {
            if (parkour.getStartRegion().isWithin(to)) {
                player.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 1.2f);
                ParkourSession parkourSession = new ParkourSession(parkour, this, player);
                parkourSession.start();
                sessions.put(player, parkourSession);
                return;
            }
        }
    }

    void parkourCompleted(ParkourSession session) {
        HandlerList.unregisterAll(session);
        sessions.remove(session.getPlayer());
    }

    private final static String START_REGION = "start_region";
    private final static String END_REGION = "end_region";
    private final static String SPAWN_POINT = "spawn_point";
    private final static String VILLAGER_POINT = "villager_point";
    private final static String WORLD = "world";
    private final static String LEVELS = "levels";

    private static JSONObject serializeParkour(Parkour parkour) {
        JSONObject object = new JSONObject();
        RegionSerializer serializer = Region.getSerializer();
        PointSerializer serializer1 = Point.getSerializer();
        object.put(START_REGION, serializer.serialize(parkour.getStartRegion()));
        object.put(END_REGION, serializer.serialize(parkour.getEndRegion()));
        object.put(SPAWN_POINT, serializer1.serialize(parkour.getSpawnPoint()));
        object.put(VILLAGER_POINT, serializer1.serialize(parkour.getVillagerPoint()));
        object.put(WORLD, parkour.getWorld().getName());
        JSONArray jsonArray = new JSONArray();
        for (ParkourLevel parkourLevel : parkour.getLevels()) {
            jsonArray.add(serializeLevel(parkourLevel));
        }
        object.put(LEVELS, jsonArray);
        return object;
    }

    private static Parkour deserializeParkour(JSONObject object) {
        RegionSerializer serializer = Region.getSerializer();
        PointSerializer serializer1 = Point.getSerializer();
        Region sRegion = serializer.deserialize((JSONObject) object.get(START_REGION));
        Region eRegion = serializer.deserialize((JSONObject) object.get(END_REGION));
        Point startPoint = serializer1.deserialize((JSONObject) object.get(SPAWN_POINT));
        Point villagerPoint = serializer1.deserialize((JSONObject) object.get(VILLAGER_POINT));
        World world = Bukkit.getWorld((String)object.get(WORLD));
        List<ParkourLevel> levels = new ArrayList<>();
        for (Object o : ((JSONArray) object.get(LEVELS))) {
            levels.add(deserializeLevel((JSONObject)o));
        }
        Parkour parkour = new Parkour(sRegion, eRegion, startPoint, villagerPoint, world);
        parkour.getLevels().addAll(levels);
        return parkour;
    }

    private final static String TARGET_LENGTH = "t_length";
    private final static String CHECKPOINT = "checkpoint";

    private static JSONObject serializeLevel(ParkourLevel parkourLevel) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(START_REGION, Region.getSerializer().serialize(parkourLevel.getStartRegion()));
        jsonObject.put(TARGET_LENGTH, parkourLevel.getTargetDuration().getMillis());
        jsonObject.put(CHECKPOINT, Point.getSerializer().serialize(parkourLevel.getCheckpoint()));
        return jsonObject;
    }

    private static ParkourLevel deserializeLevel(JSONObject object) {
        Region startRegion = Region.getSerializer().deserialize((JSONObject) object.get(START_REGION));
        Duration targetLength = new Duration(object.get(TARGET_LENGTH));
        Point checkpoint = Point.getSerializer().deserialize((JSONObject) object.get(CHECKPOINT));
        return new ParkourLevel(startRegion, targetLength, checkpoint);
    }

    public ParkourSession getParkourFor(CPlayer player) {
        return sessions.get(player);
    }

    @Override
    public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {

    }

    @Override
    public void onPlayerDisconnect(CPlayer player) {
        if (!sessions.containsKey(player)) return;
        ParkourSession remove = sessions.remove(player);
        HandlerList.unregisterAll(remove);
    }

    public void addParkour(Parkour parkourLevels) {
        parkours.add(parkourLevels);
        setupParkour(parkourLevels);
    }

    private void setupParkour(Parkour parkour) {
        String format = TBNRHub.getInstance().getFormat("parkour-npc-title", false);
        Point spawnPoint = parkour.getVillagerPoint().add(0d, 4d, 0d);
        MobNPCVillager villager = new MobNPCVillager(spawnPoint, parkour.getWorld(), null, format);
        villager.setProfession(Villager.Profession.PRIEST);
        villager.spawn();
        villager.registerObserver(new NPCObserver() {
            @Override
            public void onPlayerInteract(CPlayer player, AbstractMobNPC villager, ClickAction action) {
                try {
                    player.getCooldownManager().testCooldown(villager.hashCode() + "_click", 500L, TimeUnit.MILLISECONDS, false);
                } catch (CooldownUnexpiredException e) {
                    return;
                }
                player.sendMessage(TBNRHub.getInstance().getFormat("villager-parkour-prompt"));
            }
        });
        villagers.put(parkour, villager);
    }
}
