package net.tbnr.dev.inventory.player;

import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.HubInventoryButton;
import net.tbnr.dev.setting.PlayerSetting;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DonorInventory extends PlayerInventory {
    @Override
    protected HubInventoryButton getNewButtonAt(Integer slot) {
        HubInventoryButton newButtonAt = super.getNewButtonAt(slot);
        if (newButtonAt != null) return newButtonAt;
        switch (slot) {
            case 7:
                return new ToggleItem(PlayerSetting.FLY_IN_HUB) {
                    @Override
                    public String getName() {
                        return "Fly in Hub";
                    }

                    @Override
                    public List<String> getDescription() {
                        return Arrays.asList("Toggles flight in hub if you have purchased it.");
                    }

                    @Override
                    protected boolean canUse(CPlayer player) {
                        return player.hasPermission("hub.flight");
                    }
                };
            case 5:
                return new ToggleItem(PlayerSetting.JUMP_BOOST) {
                    @Override
                    public String getName() {
                        return "Jump Boost";
                    }

                    @Override
                    public List<String> getDescription() {
                        return Arrays.asList("Will give you Speed + Jump Boost status effects.");
                    }

                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack stack = super.getStack(player);
                        stack.setDurability((short)(TBNRHub.getInstance().getSettingsManager().getStateFor(setting, player) ? 14 : 8));
                        return stack;
                    }
                };
            case 3:

        }
        return null;
    }
}
