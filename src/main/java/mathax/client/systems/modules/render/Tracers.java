package mathax.client.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import mathax.client.eventbus.EventHandler;
import mathax.client.events.render.Render3DEvent;
import mathax.client.settings.*;
import mathax.client.systems.enemies.Enemies;
import mathax.client.systems.friends.Friends;
import mathax.client.systems.modules.Category;
import mathax.client.systems.modules.Module;
import mathax.client.systems.modules.Modules;
import mathax.client.utils.entity.EntityUtils;
import mathax.client.utils.entity.Target;
import mathax.client.utils.player.PlayerUtils;
import mathax.client.utils.render.RenderUtils;
import mathax.client.utils.render.color.Color;
import mathax.client.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

public class Tracers extends Module {
    private final Color distanceColor = new Color(255, 255, 255);

    private int count;

    private final SettingGroup generalSettings = settings.createGroup("General");
    private final SettingGroup appearanceSettings = settings.createGroup("Appearance");
    private final SettingGroup colorSettings = settings.createGroup("Colors");

    // General

    private final Setting<Object2BooleanMap<EntityType<?>>> entitiesSetting = generalSettings.add(new EntityTypeListSetting.Builder()
            .name("Entities")
            .description("Select specific entities.")
            .defaultValue(EntityType.PLAYER)
            .build()
    );


    public final Setting<Boolean> ignoreFriends = generalSettings.add(new BoolSetting.Builder()
            .name("Ignore friends")
            .description("Doesn't draw tracers to friends.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> ignoreEnemies = generalSettings.add(new BoolSetting.Builder()
            .name("Ignore enemies")
            .description("Doesn't draw tracers to enemies.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> showInvisibleSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Show invisible")
            .description("Shows invisible entities.")
            .defaultValue(true)
            .build()
    );

    // Appearance

    private final Setting<Target> targetSetting = appearanceSettings.add(new EnumSetting.Builder<Target>()
            .name("Target")
            .description("What part of the entity to target.")
            .defaultValue(Target.Body)
            .build()
    );

    private final Setting<Boolean> stemSetting = appearanceSettings.add(new BoolSetting.Builder()
            .name("Stem")
            .description("Draw a line through the center of the tracer target.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> maxDistanceSetting = appearanceSettings.add(new IntSetting.Builder()
            .name("Max distance")
            .description("Maximum distance for tracers to show.")
            .defaultValue(256)
            .min(0)
            .sliderRange(0, 256)
            .build()
    );

    // Colors

    public final Setting<Boolean> distanceSetting = colorSettings.add(new BoolSetting.Builder()
            .name("Distance colors")
            .description("Changes the color of tracers depending on distance.")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> playersColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Players colors")
            .description("The player's color.")
            .defaultValue(new SettingColor(205, 205, 205, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    private final Setting<SettingColor> animalsColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Animals color")
            .description("The animal's color.")
            .defaultValue(new SettingColor(145, 255, 145, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    private final Setting<SettingColor> waterAnimalsColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Water animals color")
            .description("The water animal's color.")
            .defaultValue(new SettingColor(145, 145, 255, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    private final Setting<SettingColor> monstersColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Monsters color")
            .description("The monster's color.")
            .defaultValue(new SettingColor(255, 145, 145, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    private final Setting<SettingColor> ambientColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Ambient color")
            .description("The ambient color.")
            .defaultValue(new SettingColor(75, 75, 75, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    private final Setting<SettingColor> miscColorSetting = colorSettings.add(new ColorSetting.Builder()
            .name("Misc color")
            .description("The misc color.")
            .defaultValue(new SettingColor(145, 145, 145, 125))
            .visible(() -> !distanceSetting.get())
            .build()
    );

    public Tracers(Category category) {
        super(category, "Tracers", "Displays tracer lines to specified entities.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) {
            return;
        }

        count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (!PlayerUtils.isWithin(entity, maxDistanceSetting.get()) || (!Modules.get().isEnabled(Freecam.class) && entity == mc.player) || !entitiesSetting.get().getBoolean(entity.getType()) || (ignoreFriends.get() && entity instanceof PlayerEntity && Friends.get().contains((PlayerEntity) entity)) || (ignoreEnemies.get() && entity instanceof PlayerEntity && Enemies.get().contains((PlayerEntity) entity)) || (!showInvisibleSetting.get() && entity.isInvisible()) | !EntityUtils.isInRenderDistance(entity)) {
                continue;
            }

            Color color;
            if (distanceSetting.get()) {
                color = getColorFromDistance(entity);
            } else if (entity instanceof PlayerEntity) {
                color = PlayerUtils.getPlayerColor(((PlayerEntity) entity), playersColorSetting.get());
            } else {
                color = switch (entity.getType().getSpawnGroup()) {
                    case CREATURE -> animalsColorSetting.get();
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> waterAnimalsColorSetting.get();
                    case MONSTER -> monstersColorSetting.get();
                    case AMBIENT -> ambientColorSetting.get();
                    default -> miscColorSetting.get();
                };
            }

            double x = entity.prevX + (entity.getX() - entity.prevX) * event.tickDelta;
            double y = entity.prevY + (entity.getY() - entity.prevY) * event.tickDelta;
            double z = entity.prevZ + (entity.getZ() - entity.prevZ) * event.tickDelta;

            double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
            if (targetSetting.get() == Target.Head) {
                y += height;
            } else if (targetSetting.get() == Target.Body) {
                y += height / 2;
            }

            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, x, y, z, color);
            if (stemSetting.get()) {
                event.renderer.line(x, entity.getY(), z, x, entity.getY() + height, z, color);
            }

            count++;
        }
    }

    private Color getColorFromDistance(Entity entity) {
        double distance = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getPos());
        double percent = distance / 60;
        if (percent < 0 || percent > 1) {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }

        int r, g;
        if (percent < 0.5) {
            r = 255;
            g = (int) (255 * percent / 0.5);  // Closer to 0.5, closer to yellow (255,255,0)
        } else {
            g = 255;
            r = 255 - (int) (255 * (percent - 0.5) / 0.5); // Closer to 1.0, closer to green (0,255,0)
        }

        distanceColor.set(r, g, 0, 255);

        return distanceColor;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}