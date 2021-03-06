package net.tbnr.dev.sg.game.loots;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.util.RandomUtils;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

@Data
public final class Tier {
    private final static String ENTRIES = "entries";
    private final static String MATERIAL = "material";
    private final static String PROBABILITY = "chance";
    private final static String DATA_VALUE = "data_value";
    private final static String QUANTITY = "quantity";
    private final static String MIN = "min_items";
    private final static String MAX = "max_items";

    private final Set<TierEntry> entrySet;
    private final Integer max;
    private final Integer min;

    public Tier(JSONObject object) {
        this.min = ((Long) object.get(MIN)).intValue();
        this.max = ((Long) object.get(MAX)).intValue();
        Set<TierEntry> entries = new HashSet<>();
        for (Object o : ((JSONArray) object.get(ENTRIES))) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject jsonObject = (JSONObject)o;
            TierEntry tierEntry = new TierEntry();
            try {
                tierEntry.material = Material.valueOf((String) jsonObject.get(MATERIAL));
            } catch (IllegalArgumentException e) {
                continue;
            }
            tierEntry.probability = jsonObject.containsKey(PROBABILITY) ? ((Double) jsonObject.get(PROBABILITY)).floatValue() : 1f;
            tierEntry.dataValue = jsonObject.containsKey(DATA_VALUE) ? ((Long) jsonObject.get(DATA_VALUE)).byteValue() : 0b0;
            tierEntry.quantity = jsonObject.containsKey(QUANTITY) ? ((Long)jsonObject.get(QUANTITY)).intValue() : 1;
            entries.add(tierEntry);
        }
        this.entrySet = entries;
    }

    public void fillChest(Chest chest) {
        Inventory inventory = chest.getInventory();
        inventory.clear();
        int mDelta = max - min;
        int amt = min + Core.getRandom().nextInt(mDelta+1);
        TierEntry[] tierEntries = entrySet.toArray(new TierEntry[entrySet.size()]);
        TierEntry[] tierEntriesAdd = new TierEntry[amt];
        for (int i = 0; i < amt; i++) {
            TierEntry entry = null;
            do {
                TierEntry tierEntry = tierEntries[Core.getRandom().nextInt(tierEntries.length)];
                if (RandomUtils.contains(tierEntriesAdd, tierEntry)) continue;
                float v = Core.getRandom().nextFloat();
                if (tierEntry.probability > v) entry = tierEntry;
            } while (entry == null);
            tierEntriesAdd[i] = entry;
        }
        for (TierEntry tierEntry : tierEntriesAdd) {
            inventory.setItem(Core.getRandom().nextInt(inventory.getSize()), new ItemStack(tierEntry.material, tierEntry.quantity, tierEntry.dataValue));
        }
    }

    private static class TierEntry {
        private Float probability;
        private Material material;
        private Integer quantity;
        private Byte dataValue;
    }
}
