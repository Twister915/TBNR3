package net.tbnr.dev.particle;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.effect.particle.ParticleEffect;
import net.cogzmc.core.effect.particle.ParticleEffectType;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.player.PerkButton;
import net.tbnr.dev.setting.PlayerSetting;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public final class ParticleEffectManager implements Listener {
    private final Map<CPlayer, Point> lastLocations = new WeakHashMap<>();
    private final Map<CPlayer, ParticlePack> particlePacks = new WeakHashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        String packName = onlinePlayer.getSettingValue("particle_pack", String.class, null);
        if (packName == null) return;
        ParticlePack particlePack;
        try {
            particlePack = ParticlePack.valueOf(packName);
        } catch (IllegalArgumentException e) {
            return;
        }
        setParticlePack(onlinePlayer, particlePack);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        particlePacks.remove(onlinePlayer);
    }

    public boolean setParticlePack(CPlayer player, ParticlePack pack) {
        particlePacks.remove(player);
        if (!player.hasPermission(pack.permission) && !player.hasPermission("hub.particle.all")) return false;
        particlePacks.put(player, pack);
        player.storeSettingValue("particle_pack", pack.name());
        return true;
    }

    public ParticlePack getPackFor(CPlayer player) {
        return particlePacks.get(player);
    }

    public InventoryGraphicalInterface getPackChooser(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, "Particle Packs");
        graphicalInterface.addButton(new PerkButton(PlayerSetting.PARTICLE_EFFECT, player, graphicalInterface), 0);
        int x = 1;
        for (ParticlePack particlePack : ParticlePack.values()) {
            graphicalInterface.addButton(new ParticlePackButton(particlePack, player, graphicalInterface), x);
            x++;
        }
        graphicalInterface.updateInventory();
        return graphicalInterface;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.PARTICLE_EFFECT, onlinePlayer)) return;
        ParticlePack packFor = getPackFor(onlinePlayer);
        if (packFor == null) return;
        Point point1 = lastLocations.get(onlinePlayer);
        Point point = onlinePlayer.getPoint();
        if (point1 != null && point1.distanceSquared(point) < 0.125) return;
        lastLocations.put(onlinePlayer, point);
        ParticleEffect[] effects = new ParticleEffect[7];
        Location[] locations = new Location[effects.length];
        World world = onlinePlayer.getBukkitPlayer().getWorld();
        float yInc = 2.0f/locations.length;
        ParticleEffectType[] types = packFor.types;
        for (int i = 0; i < effects.length ; i++) {
            effects[i] = new ParticleEffect(types[Core.getRandom().nextInt(types.length)]);
            locations[i] = point.deepCopy().getLocation(world).add(0, yInc*i, 0);
            effects[i].setAmount(1);
            effects[i].setYSpread(0.3f);
            effects[i].setZSpread(0.3f);
            effects[i].setXSpread(0.3f);
            effects[i].setSpeed(1f);
        }
        for (CPlayer player : Core.getOnlinePlayers()) {
            if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.PLAYERS, player) && !player.equals(onlinePlayer)) continue;
            if (player.getPoint().distanceSquared(point) > 400) continue;
            for (int i = 0; i < effects.length; i++) {
                effects[i].emitToPlayer(player, locations[i]);
            }
        }
    }

    private class ParticlePackButton extends InventoryButton {
        private final ParticlePack pack;
        private final CPlayer player;
        private final InventoryGraphicalInterface gInterface;

        public ParticlePackButton(ParticlePack pack, CPlayer player, InventoryGraphicalInterface gInterface) {
            super(new ItemStack(Material.FIREWORK));
            this.player = player;
            this.pack = pack;
            this.gInterface = gInterface;
            updateStack();
        }

        private void updateStack() {
            boolean b = getPackFor(player) == pack;
            ItemStack stack = getStack();
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName((b ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + pack.name);
            itemMeta.setLore(Arrays.asList("", b ? ChatColor.GREEN + ChatColor.BOLD.toString() + "You have this pack enabled!" : !player.hasPermission(pack.permission) && !player.hasPermission("hub.particle.all") ? ChatColor.RED + "You do not have permission for this!" : ChatColor.GREEN + ChatColor.BOLD.toString() + "Click to enable!"));
            stack.setItemMeta(itemMeta);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            boolean b = setParticlePack(player, pack);
            if (!b) player.sendMessage(TBNRHub.getInstance().getFormat("no-permission"));
            else player.playSoundForPlayer(Sound.NOTE_PIANO);
            for (InventoryButton inventoryButton : gInterface.getButtons()) {
                if (!(inventoryButton instanceof ParticlePackButton)) continue;
                ((ParticlePackButton) inventoryButton).updateStack();
                gInterface.markForUpdate(inventoryButton);
            }
            gInterface.updateInventory();

        }
    }
}
