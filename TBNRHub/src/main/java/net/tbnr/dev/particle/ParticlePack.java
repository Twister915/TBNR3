package net.tbnr.dev.particle;

import net.cogzmc.core.effect.particle.ParticleEffectType;

public enum ParticlePack {
    MAGIC_MIX("Magic Mix", "hub.particle.magicmix", ParticleEffectType.HAPPY_VILLAGER, ParticleEffectType.INSTANT_SPELL, ParticleEffectType.SPELL, ParticleEffectType.MOB_SPELL, ParticleEffectType.SPLASH, ParticleEffectType.DRIPLAVA),
    ENCHANT("Enchanter", "hub.particle.enchanter", ParticleEffectType.ENCHANTMENT_TABLE),
    GREEN("Glowing Green", "hub.particle.green",  ParticleEffectType.HAPPY_VILLAGER),
    TRAIL_OF_LOVE("Trail of Love", "hub.particle.love", ParticleEffectType.HEART, ParticleEffectType.SPELL),
    SMOKE_TRAIL("Smoke Trail", "hub.particle.smoke", ParticleEffectType.SMOKE, ParticleEffectType.LARGE_SMOKE),
    CLOUD_9("Cloud 9", "hub.particle.cloud", ParticleEffectType.CLOUD, ParticleEffectType.SNOWBALL_POOF);

    final String permission;
    final String name;
    final ParticleEffectType[] types;

    ParticlePack(String name, String permission, ParticleEffectType... types) {
        this.permission = permission;
        this.name = name;
        this.types = types;
    }
}
