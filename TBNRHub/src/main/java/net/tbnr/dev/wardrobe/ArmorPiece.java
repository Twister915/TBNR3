package net.tbnr.dev.wardrobe;

import org.bukkit.Material;

enum ArmorPiece {
    HAT("Hat", Material.LEATHER_HELMET),
    CHESTPLATE("Chestplate", Material.LEATHER_CHESTPLATE),
    PANTS("Pants", Material.LEATHER_LEGGINGS),
    BOOTS("Boots", Material.LEATHER_BOOTS);

    final String humanName;
    final Material representing;

    ArmorPiece(String humanName, Material representing) {
        this.humanName = humanName;
        this.representing = representing;
    }
}
