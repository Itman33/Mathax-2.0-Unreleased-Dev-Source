package mathax.client.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mathax.client.settings.*;
import mathax.client.systems.hud.Hud;
import mathax.client.systems.hud.HudElement;
import mathax.client.systems.hud.HudRenderer;
import mathax.client.utils.render.RenderUtils;
import mathax.client.utils.render.color.Color;
import mathax.client.utils.render.color.SettingColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorHudElement extends HudElement {
    private final SettingGroup generalSettings = settings.createGroup("General");

    // General

    private final Setting<Double> scaleSetting = generalSettings.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("The scale.")
            .defaultValue(2.75)
            .min(1)
            .sliderRange(1, 5)
            .build()
    );

    private final Setting<Boolean> flipOrderSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Flip order")
            .description("Flip the order of armor items.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Orientation> orientationSetting = generalSettings.add(new EnumSetting.Builder<Orientation>()
            .name("Orientation")
            .description("How to display armor.")
            .defaultValue(Orientation.Vertical)
            .build()
    );

    private final Setting<Durability> durabilitySetting = generalSettings.add(new EnumSetting.Builder<Durability>()
            .name("Durability")
            .description("How to display armor durability.")
            .defaultValue(Durability.Percentage)
            .build()
    );

    private final Setting<SettingColor> textColorSetting = generalSettings.add(new ColorSetting.Builder()
            .name("Text color")
            .description("The color of durability")
            .defaultValue(new SettingColor(Color.MATHAX))
            .build()
    );

    public ArmorHudElement(Hud hud) {
        super(hud, "Armor", "Displays information about your armor.");
    }

    @Override
    public void update(HudRenderer renderer) {
        switch (orientationSetting.get()) {
            case Horizontal -> box.setSize(16 * scaleSetting.get() * 4 + 2 * 4, 16 * scaleSetting.get());
            case Vertical -> box.setSize(16 * scaleSetting.get(), 16 * scaleSetting.get() * 4 + 2 * 4);
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();
        double armorX = 0;
        double armorY = 0;

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(scaleSetting.get().floatValue(), scaleSetting.get().floatValue(), 1);

        int slot = flipOrderSetting.get() ? 3 : 0;
        for (int position = 0; position < 4; position++) {
            ItemStack itemStack = getItem(slot);

            switch (orientationSetting.get()) {
                case Vertical -> {
                    armorX = x / scaleSetting.get();
                    armorY = y / scaleSetting.get() + position * 18;
                }
                case Horizontal -> {
                    armorX = x / scaleSetting.get() + position * 18;
                    armorY = y / scaleSetting.get();
                }
            }

            RenderUtils.drawItem(itemStack, (int) armorX, (int) armorY, (itemStack.isDamageable() && durabilitySetting.get() == Durability.Bar));

            if (itemStack.isDamageable() && !isInEditor() && durabilitySetting.get() != Durability.Bar && durabilitySetting.get() != Durability.None) {
                String percentage = Integer.toString(Math.round(((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage()));
                String message = switch (durabilitySetting.get()) {
                    case Total -> Integer.toString(itemStack.getMaxDamage() - itemStack.getDamage());
                    case Percentage -> percentage + "%";
                    default -> "error";
                };

                double messageWidth = renderer.textWidth(message);
                switch (orientationSetting.get()) {
                    case Vertical -> {
                        armorX = x + 8 * scaleSetting.get() - messageWidth / 2.0;
                        armorY = y + (18 * position * scaleSetting.get()) + (18 * scaleSetting.get() - renderer.textHeight());
                    }
                    case Horizontal -> {
                        armorX = x + 18 * position * scaleSetting.get() + 8 * scaleSetting.get() - messageWidth / 2.0;
                        armorY = y + (box.height - renderer.textHeight());
                    }
                }

                renderer.text(message, armorX, armorY, textColorSetting.get());
            }

            if (flipOrderSetting.get()) {
                slot--;
            } else {
                slot++;
            }
        }

        matrixStack.pop();
    }

    private ItemStack getItem(int i) {
        if (isInEditor()) {
            return switch (i) {
                default -> Items.NETHERITE_BOOTS.getDefaultStack();
                case 1 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 2 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 3 -> Items.NETHERITE_HELMET.getDefaultStack();
            };
        }

        return mc.player.getInventory().getArmorStack(i);
    }

    public enum Durability {
        Bar("Bar"),
        Total("Total"),
        Percentage("Percentage"),
        None("None");

        private final String title;

        Durability(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum Orientation {
        Horizontal("Horizontal"),
        Vertical("Vertical");

        private final String title;

        Orientation(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}