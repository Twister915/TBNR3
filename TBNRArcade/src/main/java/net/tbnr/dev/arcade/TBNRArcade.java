package net.tbnr.dev.arcade;

import lombok.Getter;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;

@ModuleMeta(
        description = "TBNR Arcade",
        name = "TBNR Arcade"
)
public final class TBNRArcade extends ModularPlugin {
    @Getter private static TBNRArcade instance;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
    }
}
