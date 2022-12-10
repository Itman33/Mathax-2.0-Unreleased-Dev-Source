package mathax.client.systems.modules.render;

import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mathax.client.eventbus.EventHandler;
import mathax.client.events.game.ItemStackTooltipEvent;
import mathax.client.events.render.TooltipDataEvent;
import mathax.client.mixin.EntityAccessor;
import mathax.client.mixin.EntityBucketItemAccessor;
import mathax.client.settings.*;
import mathax.client.systems.modules.Category;
import mathax.client.systems.modules.Module;
import mathax.client.utils.Utils;
import mathax.client.utils.input.KeyBind;
import mathax.client.utils.misc.ByteCountDataOutput;
import mathax.client.utils.player.EChestMemory;
import mathax.client.utils.render.color.Color;
import mathax.client.utils.tooltip.*;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;

public class BetterTooltips extends Module {
    public static final Color ECHEST_COLOR = new Color(0, 50, 50);

    private final SettingGroup generalSettings = settings.createGroup("General");
    private final SettingGroup previewsSettings = settings.createGroup("Previews");
    private final SettingGroup otherSettings = settings.createGroup("Other");

    // General

    private final Setting<DisplayWhen> displayWhenSetting = generalSettings.add(new EnumSetting.Builder<DisplayWhen>()
            .name("Display when")
            .description("When to display previews.")
            .defaultValue(DisplayWhen.Always)
            .build()
    );

    private final Setting<KeyBind> keybindSetting = generalSettings.add(new KeyBindSetting.Builder()
            .name("KeyBind")
            .description("The bind for keybind mode.")
            .defaultValue(KeyBind.fromKey(GLFW_KEY_LEFT_ALT))
            .visible(() -> displayWhenSetting.get() == DisplayWhen.KeyBind)
            .build()
    );

    private final Setting<Boolean> middleClickOpenSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Middle click open")
            .description("Open a GUI window with the inventory of the storage block when you middle click the item.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> alwaysShowSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Always show")
            .description("Disable the HideFlags nbt tag.")
            .defaultValue(true)
            .build()
    );

    // Previews

    private final Setting<Boolean> shulkersSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Containers")
            .description("Shows a preview of a containers when hovering over it in an inventory.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> shulkerCompactTooltipSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Compact shulker tooltip")
            .description("Compact the lines of the shulker tooltip.")
            .defaultValue(true)
            .visible(shulkersSetting::get)
            .build()
    );

    public final Setting<Boolean> echestSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Echests")
            .description("Show a preview of your echest when hovering over it in an inventory.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> mapsSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Maps")
            .description("Show a preview of a map when hovering over it in an inventory.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Double> mapsScaleSetting = previewsSettings.add(new DoubleSetting.Builder()
            .name("Map scale")
            .description("Scale of the map preview.")
            .defaultValue(1)
            .min(0.001)
            .sliderRange(0.001, 1)
            .visible(mapsSetting::get)
            .build()
    );

    private final Setting<Boolean> booksSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Books")
            .description("Show contents of a book when hovering over it in an inventory.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> bannersSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Banners")
            .description("Show banners' patterns when hovering over it in an inventory. Also works with shields.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> entitiesSetting = previewsSettings.add(new BoolSetting.Builder()
            .name("Entities")
            .description("Show entities in buckets when hovering over it in an inventory.")
            .defaultValue(true)
            .build()
    );

    // Other

    public final Setting<Boolean> byteSizeSetting = otherSettings.add(new BoolSetting.Builder()
            .name("Byte size")
            .description("Display an item's size in bytes in the tooltip.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> statusEffectsSetting = otherSettings.add(new BoolSetting.Builder()
            .name("Status effects")
            .description("Add list of status effects to tooltips of food items.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> beehiveSetting = otherSettings.add(new BoolSetting.Builder()
            .name("Beehive")
            .description("Display information about a beehive or bee nest.")
            .defaultValue(true)
            .build()
    );

    public BetterTooltips(Category category) {
        super(category, "Better Tooltips", "Displays more useful tooltips for certain items.");
    }

    @EventHandler
    private void appendTooltip(ItemStackTooltipEvent event) {
        if (statusEffectsSetting.get()) {
            if (event.itemStack.getItem() == Items.SUSPICIOUS_STEW) {
                NbtCompound tag = event.itemStack.getNbt();
                if (tag != null) {
                    NbtList effects = tag.getList("Effects", 10);
                    if (effects != null) {
                        for (int i = 0; i < effects.size(); i++) {
                            NbtCompound effectTag = effects.getCompound(i);
                            byte effectId = effectTag.getByte("EffectId");
                            int effectDuration = effectTag.contains("EffectDuration") ? effectTag.getInt("EffectDuration") : 160;
                            StatusEffect type = StatusEffect.byRawId(effectId);
                            if (type != null) {
                                StatusEffectInstance effect = new StatusEffectInstance(type, effectDuration, 0);
                                event.list.add(1, getStatusText(effect));
                            }
                        }
                    }
                }
            } else if (event.itemStack.getItem().isFood()) {
                FoodComponent food = event.itemStack.getItem().getFoodComponent();
                if (food != null) {
                    food.getStatusEffects().forEach((e) -> {
                        StatusEffectInstance effect = e.getFirst();
                        event.list.add(1, getStatusText(effect));
                    });
                }
            }
        }

        if (beehiveSetting.get()) {
            if (event.itemStack.getItem() == Items.BEEHIVE || event.itemStack.getItem() == Items.BEE_NEST) {
                NbtCompound tag = event.itemStack.getNbt();
                if (tag != null) {
                    NbtCompound blockStateTag = tag.getCompound("BlockStateTag");
                    if (blockStateTag != null) {
                        int level = blockStateTag.getInt("honey_level");
                        event.list.add(1, Text.literal(String.format("%sHoney level: %s%d%s.", Formatting.GRAY, Formatting.YELLOW, level, Formatting.GRAY)));
                    }

                    NbtCompound blockEntityTag = tag.getCompound("BlockEntityTag");
                    if (blockEntityTag != null) {
                        NbtList beesTag = blockEntityTag.getList("Bees", 10);
                        event.list.add(1, Text.literal(String.format("%sBees: %s%d%s.", Formatting.GRAY, Formatting.YELLOW, beesTag.size(), Formatting.GRAY)));
                    }
                }
            }
        }

        if (byteSizeSetting.get()) {
            try {
                event.itemStack.writeNbt(new NbtCompound()).write(ByteCountDataOutput.INSTANCE);

                ByteCountDataOutput.INSTANCE.reset();

                int byteCount = ByteCountDataOutput.INSTANCE.getCount();
                String count;
                if (byteCount >= 1024) {
                    count = String.format("%.2f kb", byteCount / (float) 1024);
                } else {
                    count = String.format("%d bytes", byteCount);
                }

                event.list.add(Text.literal(count).formatted(Formatting.GRAY));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        if ((Utils.hasItems(event.itemStack) && shulkersSetting.get() && !previewShulkers()) || (event.itemStack.getItem() == Items.ENDER_CHEST && echestSetting.get() && !previewEChest()) || (event.itemStack.getItem() == Items.FILLED_MAP && mapsSetting.get() && !previewMaps()) || (event.itemStack.getItem() == Items.WRITABLE_BOOK && booksSetting.get() && !previewBooks()) || (event.itemStack.getItem() == Items.WRITTEN_BOOK && booksSetting.get() && !previewBooks()) || (event.itemStack.getItem() instanceof EntityBucketItem && entitiesSetting.get() && !previewEntities()) || (event.itemStack.getItem() instanceof BannerItem && bannersSetting.get() && !previewBanners()) || (event.itemStack.getItem() instanceof BannerPatternItem && bannersSetting.get()  && !previewBanners()) || (event.itemStack.getItem() == Items.SHIELD && bannersSetting.get() && !previewBanners())) {
            event.list.add(Text.literal(""));
            event.list.add(Text.literal("Hold " + Formatting.YELLOW + keybind + Formatting.RESET + " to preview"));
        }
    }

    @EventHandler
    private void getTooltipData(TooltipDataEvent event) {
        if (Utils.hasItems(event.itemStack) && previewShulkers()) {
            NbtCompound compoundTag = event.itemStack.getSubNbt("BlockEntityTag");
            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(compoundTag, itemStacks);
            event.tooltipData = new ContainerTooltipComponent(itemStacks, Utils.getShulkerColor(event.itemStack));
        } else if (event.itemStack.getItem() == Items.ENDER_CHEST && previewEChest()) {
            event.tooltipData = new ContainerTooltipComponent(EChestMemory.ITEMS, ECHEST_COLOR);
        } else if (event.itemStack.getItem() == Items.FILLED_MAP && previewMaps()) {
            Integer mapId = FilledMapItem.getMapId(event.itemStack);
            if (mapId != null) {
                event.tooltipData = new MapTooltipComponent(mapId);
            }
        } else if ((event.itemStack.getItem() == Items.WRITABLE_BOOK || event.itemStack.getItem() == Items.WRITTEN_BOOK) && previewBooks()) {
            Text page = getFirstPage(event.itemStack);
            if (page != null) {
                event.tooltipData = new BookTooltipComponent(page);
            }
        } else if (event.itemStack.getItem() instanceof BannerItem && previewBanners()) {
            event.tooltipData = new BannerTooltipComponent(event.itemStack);
        } else if (event.itemStack.getItem() instanceof BannerPatternItem patternItem && previewBanners()) {
            RegistryEntry<BannerPattern> bannerPattern = (Registry.BANNER_PATTERN.getEntryList(patternItem.getPattern()).isPresent() ? Registry.BANNER_PATTERN.getEntryList(patternItem.getPattern()).get().get(0) : null);
            if (bannerPattern != null) {
                event.tooltipData = new BannerTooltipComponent(createBannerFromPattern(bannerPattern));
            }
        } else if (event.itemStack.getItem() == Items.SHIELD && previewBanners()) {
            ItemStack banner = createBannerFromShield(event.itemStack);
            if (banner != null) {
                event.tooltipData = new BannerTooltipComponent(banner);
            }
        } else if (event.itemStack.getItem() instanceof EntityBucketItem bucketItem && previewEntities()) {
            EntityType<?> type = ((EntityBucketItemAccessor) bucketItem).getEntityType();
            Entity entity = type.create(mc.world);
            if (entity != null) {
                ((Bucketable) entity).copyDataFromNbt(event.itemStack.getOrCreateNbt());
                ((EntityAccessor) entity).setInWater(true);
                event.tooltipData = new EntityTooltipComponent(entity);
            }
        }
    }

    public void applyCompactShulkerTooltip(ItemStack stack, List<Text> tooltip) {
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");

        if (tag != null) {
            if (tag.contains("LootTable", 8)) {
                tooltip.add(Text.literal("???????"));
            }

            if (tag.contains("Items", 9)) {
                DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.readNbt(tag, items);

                Object2IntMap<Item> counts = new Object2IntOpenHashMap<>();
                for (ItemStack item : items) {
                    if (item.isEmpty()) {
                        continue;
                    }

                    int count = counts.getInt(item.getItem());
                    counts.put(item.getItem(), count + item.getCount());
                }

                counts.keySet().stream().sorted(Comparator.comparingInt(value -> -counts.getInt(value))).limit(5).forEach(item -> {
                    MutableText mutableText = item.getName().copyContentOnly();
                    mutableText.append(Text.literal(" x").append(String.valueOf(counts.getInt(item))).formatted(Formatting.GRAY));
                    tooltip.add(mutableText);
                });

                if (counts.size() > 5) {
                    tooltip.add((Text.translatable("container.shulkerBox.more", counts.size() - 5)).formatted(Formatting.ITALIC));
                }
            }
        }
    }

    private MutableText getStatusText(StatusEffectInstance effect) {
        MutableText text = Text.translatable(effect.getTranslationKey());
        if (effect.getAmplifier() != 0) {
            text.append(String.format(" %d (%s)", effect.getAmplifier() + 1, StatusEffectUtil.durationToString(effect, 1)));
        } else {
            text.append(String.format(" (%s)", StatusEffectUtil.durationToString(effect, 1)));
        }

        if (effect.getEffectType().isBeneficial()) {
            return text.formatted(Formatting.BLUE);
        }

        return text.formatted(Formatting.RED);
    }

    private Text getFirstPage(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null) {
            return null;
        }

        NbtList pages = tag.getList("pages", 8);
        if (pages.size() < 1) {
            return null;
        }

        if (stack.getItem() == Items.WRITABLE_BOOK) {
            return Text.literal(pages.getString(0));
        }

        try {
            return Text.Serializer.fromLenientJson(pages.getString(0));
        } catch (JsonSyntaxException exception) {
            return Text.literal("Invalid book data");
        }
    }

    private ItemStack createBannerFromPattern(RegistryEntry<BannerPattern> pattern) {
        ItemStack itemStack = new ItemStack(Items.GRAY_BANNER);
        NbtCompound nbt = itemStack.getOrCreateSubNbt("BlockEntityTag");
        NbtList listNbt = new BannerPattern.Patterns().add(BannerPatterns.BASE, DyeColor.BLACK).add(pattern, DyeColor.WHITE).toNbt();
        nbt.put("Patterns", listNbt);

        return itemStack;
    }

    private ItemStack createBannerFromShield(ItemStack item) {
        if (!item.hasNbt() || !item.getNbt().contains("BlockEntityTag") || !item.getNbt().getCompound("BlockEntityTag").contains("Base")) {
            return null;
        }

        NbtList listNbt = new BannerPattern.Patterns().add(BannerPatterns.BASE, ShieldItem.getColor(item)).toNbt();
        NbtCompound nbt = item.getOrCreateSubNbt("BlockEntityTag");
        ItemStack bannerItem = new ItemStack(Items.GRAY_BANNER);
        NbtCompound bannerTag = bannerItem.getOrCreateSubNbt("BlockEntityTag");

        bannerTag.put("Patterns", listNbt);
        if (!nbt.contains("Patterns")) {
            return bannerItem;
        }

        NbtList shieldPatterns = nbt.getList("Patterns", NbtElement.COMPOUND_TYPE);
        listNbt.addAll(shieldPatterns);

        return bannerItem;
    }

    public boolean middleClickOpen() {
        return isEnabled() && middleClickOpenSetting.get();
    }

    public boolean alwaysShow() {
        return isEnabled() && alwaysShowSetting.get();
    }

    public boolean previewShulkers() {
        return isEnabled() && isPressed() && shulkersSetting.get();
    }

    public boolean shulkerCompactTooltip() {
        return isEnabled() && shulkerCompactTooltipSetting.get();
    }

    private boolean previewEChest() {
        return isPressed() && echestSetting.get();
    }

    private boolean previewMaps() {
        return isPressed() && mapsSetting.get();
    }

    private boolean previewBooks() {
        return isPressed() && booksSetting.get();
    }

    private boolean previewBanners() {
        return isPressed() && bannersSetting.get();
    }

    private boolean previewEntities() {
        return isPressed() && entitiesSetting.get();
    }

    private boolean isPressed() {
        return (keybindSetting.get().isPressed() && displayWhenSetting.get() == DisplayWhen.KeyBind) || displayWhenSetting.get() == DisplayWhen.Always;
    }

    public enum DisplayWhen {
        KeyBind("KeyBind"),
        Always("Always");

        private final String name;

        DisplayWhen(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}