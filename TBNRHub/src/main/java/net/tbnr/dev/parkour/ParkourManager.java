package net.tbnr.dev.parkour;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.AbstractMobNPC;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.effect.npc.NPCObserver;
import net.cogzmc.core.effect.npc.mobs.MobNPCVillager;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Sound;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public final class ParkourManager implements Listener {
    private final Map<CPlayer, ParkourSession> sessions = new WeakHashMap<>();
    private final Set<Parkour> parkours = new HashSet<>();
    private final Map<Parkour, MobNPCVillager> villagers = new HashMap<>();

    public void enable() {
        String format = TBNRHub.getInstance().getFormat("parkour-npc-title");
        for (Parkour parkour : parkours) {
            Point spawnPoint = parkour.getSpawnPoint();
            MobNPCVillager villager = new MobNPCVillager(spawnPoint, parkour.getWorld(), null, format);
            villager.setProfession(Villager.Profession.PRIEST);
            villager.spawn();
            villager.registerObserver(new NPCObserver() {
                @Override
                public void onPlayerInteract(CPlayer player, AbstractMobNPC villager, ClickAction action) {

                }
            });
            villagers.put(parkour, villager);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Point to = Point.of(event.getTo());
        CPlayer player = Core.getOnlinePlayer(event.getPlayer());
        for (Parkour parkour : parkours) {
            if (parkour.getStartRegion().isWithin(to)) {
                player.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 1.2f);
                if (sessions.containsKey(player)) return;
                ParkourSession parkourSession = new ParkourSession(parkour, this, player);
                sessions.put(player, parkourSession);
                return;
            }
        }
    }
}
