package com.lvfoods.sxv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;


public class LvFoodsSxV implements ModInitializer {
    public static final String MOD_ID = "lv-foods-sxv";

    private static ResourceKey<Item> key(String id) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, id));
    }

    private static FoodProperties food() {
        return new FoodProperties.Builder()
                .nutrition(20)
                .saturationModifier(2.0f)
                .alwaysEdible()
                .build();
    }

    private static Item register(String id, Reward reward) {
        ResourceKey<Item> itemKey = key(id);
        Item item = new RewardFoodItem(
                new Item.Properties()
                        .setId(itemKey)
                        .food(food(), Consumables.defaultFood().build()),
                reward
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static final Item XP_1000 = register("xp_1000_food", Reward.xp("xp_feast_1"));
    public static final Item XP_5000 = register("xp_5000_food", Reward.xp("xp_feast_2"));
    public static final Item XP_10000 = register("xp_10000_food", Reward.xp("xp_feast_3"));
    public static final Item XP_15000 = register("xp_15000_food", Reward.xp("xp_feast_4"));
    public static final Item XP_30000 = register("xp_30000_food", Reward.xp("xp_feast_5"));
    public static final Item LV_50 = register("level_50_food", Reward.levels("legendary_feast_levels"));
    public static final Item LV_99 = register("level_99_food", Reward.levels("divine_feast_levels"));

    @Override
    public void onInitialize() {
        LvFoodsConfig.load();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.accept(XP_1000);
            entries.accept(XP_5000);
            entries.accept(XP_10000);
            entries.accept(XP_15000);
            entries.accept(XP_30000);
            entries.accept(LV_50);
            entries.accept(LV_99);
        });
    }

    public record Reward(String configKey, boolean xp) {
        public static Reward xp(String key) { return new Reward(key, true); }
        public static Reward levels(String key) { return new Reward(key, false); }
    }

    public static class RewardFoodItem extends Item {
        private final Reward reward;

        public RewardFoodItem(Properties properties, Reward reward) {
            super(properties);
            this.reward = reward;
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
            ItemStack result = super.finishUsingItem(stack, level, user);

            if (!level.isClientSide() && user instanceof Player player) {
                int amount = LvFoodsConfig.get(reward.configKey());
                if (reward.xp()) {
                    player.giveExperiencePoints(amount);
                } else {
                    player.giveExperienceLevels(amount);
                }
            }
            return result;
        }
    }

    /**
     * Editable gameplay values stored in config/lv_foods_sxv.json.
     * The file is created automatically on first launch.
     */
    public static final class LvFoodsConfig {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path PATH = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("lv_foods_sxv.json");

        private static JsonObject values = defaults();

        private static JsonObject defaults() {
            JsonObject json = new JsonObject();
            json.addProperty("xp_feast_1", 1000);
            json.addProperty("xp_feast_2", 5000);
            json.addProperty("xp_feast_3", 10000);
            json.addProperty("xp_feast_4", 15000);
            json.addProperty("xp_feast_5", 30000);
            json.addProperty("legendary_feast_levels", 105);
            json.addProperty("divine_feast_levels", 10000);
            return json;
        }

        public static void load() {
            try {
                Files.createDirectories(PATH.getParent());

                if (Files.notExists(PATH)) {
                    values = defaults();
                    save();
                    return;
                }

                try (Reader reader = Files.newBufferedReader(PATH)) {
                    JsonObject loaded = GSON.fromJson(reader, JsonObject.class);
                    if (loaded == null) {
                        throw new JsonParseException("Config is empty.");
                    }

                    JsonObject fallback = defaults();
                    for (String key : fallback.keySet()) {
                        if (loaded.has(key) && loaded.get(key).isJsonPrimitive()
                                && loaded.get(key).getAsJsonPrimitive().isNumber()) {
                            int value = loaded.get(key).getAsInt();
                            loaded.addProperty(key, Math.max(0, value));
                        } else {
                            loaded.addProperty(key, fallback.get(key).getAsInt());
                        }
                    }

                    values = loaded;
                    save();
                }
            } catch (IOException | JsonParseException | NumberFormatException e) {
                System.err.println("[Lv Foods SxV] Could not read config " + PATH + ": " + e.getMessage());
                System.err.println("[Lv Foods SxV] Using default XP/level values for this session.");
                values = defaults();
            }
        }

        private static void save() throws IOException {
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(values, writer);
            }
        }

        public static int get(String key) {
            if (values.has(key) && values.get(key).isJsonPrimitive()
                    && values.get(key).getAsJsonPrimitive().isNumber()) {
                return Math.max(0, values.get(key).getAsInt());
            }
            return defaults().get(key).getAsInt();
        }
    }
}
