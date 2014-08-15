package net.tbnr.dev.sg.game.deathperks;

import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.SGGame;
import net.tbnr.dev.sg.game.util.Timer;
import net.tbnr.dev.sg.game.util.TimerDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

@Data
public final class LastStand implements DeathPerk {
    private final String name = "Don't Miss";
    private final List<String> description = Arrays.asList("Upon death you get a single bow and arrow", "which can kill your killer.");

    @Override
    public boolean onDeath(final SGGame game, final CPlayer died, final CPlayer killer) {
        if (killer == null) return false;
        died.addStatusEffect(PotionEffectType.CONFUSION, 1);
        died.addStatusEffect(PotionEffectType.SLOW, 2);
        new Timer(2, new TimerDelegate() {
            private LastStandSafety safety = new LastStandSafety(died);

            @Override
            public void countdownStarted(Timer timer, Integer totalSeconds) {
                SurvivalGames.getInstance().registerListener(safety);
            }

            @Override
            public void countdownEnded(Timer timer, Integer totalSeconds) {
                PlayerInventory inventory = died.getBukkitPlayer().getInventory();
                inventory.setArmorContents(new ItemStack[4]);
                ItemStack[] itemStacks = new ItemStack[inventory.getSize()];
                Arrays.fill(itemStacks, 0, 8, new ItemStack(Material.BOW));
                inventory.setContents(itemStacks);
                inventory.addItem(new ItemStack(Material.ARROW));
                new LastStandSession(died, killer, game, 10, safety);
            }

            @Override
            public void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds) {
                died.sendMessage(SurvivalGames.getInstance().getFormat("last-stand-in", new String[]{"<seconds>", String.valueOf(timer.getLength()-timer.getSecondsPassed())}));
            }
        }).start();
        return true;
    }

    private class LastStandSession extends BukkitRunnable implements Listener {
        private final CPlayer player;
        private final CPlayer target;
        private final SGGame game;
        private final LastStandSafety safety;

        private boolean hit = false;

        LastStandSession(CPlayer player, CPlayer target, SGGame game, Integer timeout, LastStandSafety safety) {
            runTaskLater(SurvivalGames.getInstance(), timeout*20);
            this.player = player;
            this.target = target;
            this.game = game;
            this.safety = safety;
            SurvivalGames.getInstance().registerListener(this);
        }

        @Override
        public void run() {
            player.sendMessage(SurvivalGames.getInstance().getFormat("last-stand-expired"));
            SurvivalGames.getInstance().getGameManager().getRunningGame().finishDeath(player);
            unregisterListeners();
        }

        @EventHandler
        public void onPlayerDamage(EntityDamageByEntityEvent event) {
            Player target = this.target.getBukkitPlayer();
            if (!event.getEntity().equals(target)) return;
            if (!(event.getDamager() instanceof Arrow)) return;
            Arrow damager = (Arrow) event.getDamager();
            ProjectileSource shooter = damager.getShooter();
            if (!(shooter instanceof Player)) return;
            Player playerShooter = player.getBukkitPlayer();
            if (!shooter.equals(playerShooter)) return;
            hit = true;
            playerShooter.teleport(target.getLocation());
            PlayerInventory inventory = playerShooter.getInventory();
            PlayerInventory inventory1 = target.getInventory();
            inventory.setArmorContents(inventory1.getArmorContents());
            inventory.setContents(inventory1.getContents());
            playerShooter.setTotalExperience(target.getTotalExperience());
            playerShooter.setFoodLevel(20);
            target.damage(target.getHealth());
            inventory1.clear();
            inventory1.setArmorContents(new ItemStack[4]);
            cancel();
            game.revive(player);
            for (PotionEffect effect : playerShooter.getActivePotionEffects()) {
                playerShooter.removePotionEffect(effect.getType());
            }
            unregisterListeners();
        }


        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            if (!event.getEntity().getShooter().equals(player.getBukkitPlayer())) return;
            Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (hit) return;
                    game.finishDeath(player);
                    unregisterListeners();
                    cancel();
                }
            }, 2L);
        }

        private void unregisterListeners() {
            HandlerList.unregisterAll(this);
            HandlerList.unregisterAll(safety);
        }
    }

    @Data
    private class LastStandSafety implements Listener {
        private final CPlayer player;

        @EventHandler
        public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
            if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
            event.setCancelled(true);
        }

        @EventHandler
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            if (event.getEntity() instanceof Player && event.getEntity().equals(player.getBukkitPlayer())) event.setCancelled(true);
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player && event.getDamager().equals(player.getBukkitPlayer())) event.setCancelled(true);
        }
    }
}
