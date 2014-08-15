package net.tbnr.dev.parkour;

import lombok.EqualsAndHashCode;
import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.AbstractMobNPC;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.effect.npc.NPCObserver;
import net.cogzmc.core.effect.npc.mobs.MobNPCVillager;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.json.PointSerializer;
import net.cogzmc.core.json.RegionSerializer;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.cogzmc.core.util.TimeUtils;
import net.tbnr.dev.TBNRHub;
import org.bukkit.*;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private static final short[] dataValuesForInventory = new short[]{5, 6, 4, 3, 2, 14, 10};

    private final Map<CPlayer, ParkourSession> sessions = new WeakHashMap<>();
    private final List<Parkour> parkours = new ArrayList<>();
    private final Map<Parkour, MobNPCVillager> villagers = new HashMap<>();
    private final Map<CPlayer, InventoryGraphicalInterface> interfaces = new WeakHashMap<>();

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
        Bukkit.getScheduler().runTaskTimer(TBNRHub.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Parkour, MobNPCVillager> parkourMobNPCVillagerEntry : villagers.entrySet()) {
                    parkourMobNPCVillagerEntry.getValue().move(parkourMobNPCVillagerEntry.getKey().getVillagerPoint());
                }
            }
        }, 1200, 1200);
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
               startParkour(parkour, 1, player, false);
                return;
            }
        }
    }

    public void startParkour(Parkour parkour, Integer level, CPlayer player, boolean teleport) {
        if (teleport) player.getBukkitPlayer().teleport(parkour.getLevels().get(level-1).getCheckpoint().getLocation(parkour.getWorld()));
        player.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 1.2f);
        ParkourSession parkourSession = new ParkourSession(parkour, this, level-1, player);
        parkourSession.start();
        sessions.put(player, parkourSession);
    }

    void parkourCompleted(ParkourSession session) {
        HandlerList.unregisterAll(session);
        sessions.remove(session.getPlayer());
        interfaces.remove(session.getPlayer());
        getInterfaceFor(session.getPlayer(), session.getParkour());
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
        interfaces.remove(player);
        if (!sessions.containsKey(player)) return;
        ParkourSession remove = sessions.remove(player);
        HandlerList.unregisterAll(remove);
    }

    public void addParkour(Parkour parkourLevels) {
        parkours.add(parkourLevels);
        setupParkour(parkourLevels);
    }

    private void setupParkour(final Parkour parkour) {
        String format = TBNRHub.getInstance().getFormat("parkour-npc-title", false);
        Point spawnPoint = parkour.getVillagerPoint().deepCopy().add(0d, 1d, 0d);
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
                getInterfaceFor(player, parkour).open(player);
            }
        });
        villagers.put(parkour, villager);
    }

    private InventoryGraphicalInterface getInterfaceFor(CPlayer player, Parkour parkour) {
        if (interfaces.containsKey(player)) return interfaces.get(player);
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GREEN + ChatColor.BOLD.toString() + "Parkour");
        for (int i = 1; i <= parkour.getLevels().size(); i++) {
            graphicalInterface.addButton(new LevelButton(i, playerHasCompletedLevel(player, i), getBestTimeForPlayer(player, i), parkour.getLevels().get(i-1), parkour));
        }
        graphicalInterface.updateInventory();
        interfaces.put(player, graphicalInterface);
        return graphicalInterface;
    }

    @EqualsAndHashCode(callSuper = true)
    private class LevelButton extends InventoryButton {
        private final Integer levelNumber;
        private final boolean completed;
        private final Duration timeCompleted;
        private final ParkourLevel level;
        private final Parkour parkour;

        public LevelButton(Integer levelNumber, boolean completed, Duration timeCompleted, ParkourLevel level, Parkour parkour) {
            super(null);
            this.levelNumber = levelNumber;
            this.completed = completed;
            this.timeCompleted = timeCompleted;
            this.level = level;
            this.parkour = parkour;
            setStack(getStackFor(this));
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (sessions.get(player) != null) return;
            if (!completed) {
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-no-teleport"));
                return;
            }
            else if (action == ClickAction.RIGHT_CLICK) {
                if (levelNumber == parkour.getLevels().size()) return;
                startParkour(parkour, levelNumber+1, player, true);
            } else {
                startParkour(parkour, levelNumber, player, true);
            }
            player.playSoundForPlayer(Sound.NOTE_PLING);
        }
    }

    private ItemStack getStackFor(LevelButton levelButton) {
        ItemStack stack = new ItemStack(Material.STAINED_CLAY);
        stack.setDurability(dataValuesForInventory[levelButton.levelNumber-1 % dataValuesForInventory.length]);
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName((levelButton.completed ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + "Level #" + (levelButton.levelNumber) + " - " + (levelButton.completed ? "Completed" : "Not Completed"));
        List<String> strings = new ArrayList<>();
        strings.add("");
        if (levelButton.completed) {
            strings.add(ChatColor.GREEN + "You have completed this level!");
            strings.add("");
            strings.add(ChatColor.GREEN + "Left click this to teleport");
            strings.add(ChatColor.GREEN + "to the start of this level.");
            if (levelButton.parkour.getLevels().size() != levelButton.levelNumber) {
                strings.add(ChatColor.YELLOW + "Right click this to teleport");
                strings.add(ChatColor.YELLOW + "to the start of the next level.");
                strings.add("");
            }
            strings.add(ChatColor.GREEN + "Best Time Completed: ");
            strings.add(ChatColor.YELLOW + "   " + TimeUtils.formatDurationNicely(levelButton.timeCompleted));
        } else {
            strings.add(ChatColor.RED + "You have not completed this level!");
            strings.add("");
            strings.add(ChatColor.RED + "Right click the previous level");
            strings.add(ChatColor.RED + "to teleport to the start of this level.");
            strings.add("");
        }
        strings.add(ChatColor.GREEN + "Target Time: ");
        strings.add(ChatColor.YELLOW + "   " +  TimeUtils.formatDurationNicely(levelButton.level.getTargetDuration()));
        itemMeta.setLore(strings);
        stack.setAmount(levelButton.levelNumber);
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public static boolean playerHasCompletedLevel(CPlayer player, Integer parkourLevelNumber) {
        return player.getSettingValue("parkour_complete_" + parkourLevelNumber, Boolean.class, false);
    }

    public static Duration getBestTimeForPlayer(CPlayer player, Integer parkourNumber) {
        return new Duration(player.getSettingValue("parkour_level_time_" + parkourNumber, Long.class, 0L));
    }
}
