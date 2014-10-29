package net.tbnr.dev.setting;

import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.SettingUtils;

public final class ToggleSettingCommand extends ModuleCommand {
    public ToggleSettingCommand() {
        super("togglesetting");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 1) throw new ArgumentRequirementException("You must supply two arguments!");
        PlayerSetting playerSetting = PlayerSetting.valueOf(args[0]);
        try {
            player.getCooldownManager().testCooldown("setting_" + playerSetting.hashCode(), 1L);
        } catch (CooldownUnexpiredException e) {
            SettingUtils.onSettingDeny(player);
            return;
        }
        if (playerSetting.getPermission() != null && !player.hasPermission(playerSetting.getPermission())) {
            SettingUtils.onSettingDeny(player);
            return;
        }
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        try {
            settingsManager.toggleStateFor(playerSetting, player);
        } catch (SettingChangeException e) {
            SettingUtils.onSettingDeny(player);
            return;
        }
        SettingUtils.onSettingToggle(player, playerSetting);
    }
}
