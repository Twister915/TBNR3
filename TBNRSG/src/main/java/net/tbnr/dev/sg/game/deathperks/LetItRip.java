package net.tbnr.dev.sg.game.deathperks;

import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.SGGame;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public final class LetItRip implements DeathPerk {
    private final String name = "Let it Rip";
    private final List<String> description = Arrays.asList("Gives you five TNT that you can throw", "at your killer after you have died.");

    @Override
    public boolean onDeath(SGGame game, CPlayer died, CPlayer killer) {
        if (killer == null) return false;
        new LetItRipSession(died, killer, game);
        return true;
    }

    @Data
    private class LetItRipSession implements Listener {
        private final CPlayer player;
        private final CPlayer target;
        private final SGGame game;
        private final Set<TNTPrimed> primedTnts = new HashSet<>();
        private int tnts = 5;

        {
            SurvivalGames.getInstance().registerListener(this);
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_AIR) return;
            Player bukkitPlayer = event.getPlayer();
            if (!bukkitPlayer.equals(player.getBukkitPlayer())) return;
            Vector multiply = bukkitPlayer.getLocation().getDirection().clone().add(new Vector(0, 1, 0)).multiply(1.5f);
            TNTPrimed spawn = bukkitPlayer.getWorld().spawn(bukkitPlayer.getLocation(), TNTPrimed.class);
            spawn.setVelocity(multiply);
            primedTnts.add(spawn);
            tnts--;
            if (tnts == 0) {
                HandlerList.unregisterAll(this);
                game.finishDeath(player);
                return;
            }
            updateInventory();
        }

        @EventHandler
        public void onPlayerDamage(EntityDamageByEntityEvent event) {
            if (event.getEntity().equals(player.getBukkitPlayer())) event.setCancelled(true);
            else if (event.getDamager() instanceof TNTPrimed && primedTnts.contains(event.getDamager()) && !event.getEntity().equals(target))
                event.setCancelled(true);
        }

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            if (event.getEntity().equals(target.getBukkitPlayer()) && event.getEntity().getKiller() != null && event.getEntity().getKiller().equals(player.getBukkitPlayer())) {
                game.doAllDeath(target, player);
                game.finishDeath(player);
                HandlerList.unregisterAll(this);
            }
        }

        private void updateInventory() {
            ItemStack itemStack = new ItemStack(Material.TNT);
            itemStack.setAmount(tnts);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RED + name);
            Player bukkitPlayer = player.getBukkitPlayer();
            PlayerInventory inventory = bukkitPlayer.getInventory();
            inventory.clear();
            inventory.setArmorContents(new ItemStack[4]);
            ItemStack[] stacks = new ItemStack[inventory.getSize()];
            Arrays.fill(stacks, 0, 8, itemStack);
            inventory.setContents(stacks);
            bukkitPlayer.updateInventory();
        }
    }
}
