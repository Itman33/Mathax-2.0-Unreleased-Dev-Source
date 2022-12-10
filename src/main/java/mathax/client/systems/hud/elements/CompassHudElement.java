package mathax.client.systems.hud.elements;

import mathax.client.settings.DoubleSetting;
import mathax.client.settings.EnumSetting;
import mathax.client.settings.Setting;
import mathax.client.settings.SettingGroup;
import mathax.client.systems.hud.Hud;
import mathax.client.systems.hud.HudElement;
import mathax.client.systems.hud.HudRenderer;
import mathax.client.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

public class CompassHudElement extends HudElement {
    private static final Color RED = new Color(225, 45, 45);

    private final SettingGroup generalSettings = settings.createGroup("General");

    // General

    private final Setting<Mode> modeSetting = generalSettings.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Which type of direction information to show.")
        .defaultValue(Mode.Axis)
        .build()
    );

    private final Setting<Double> scaleSetting = generalSettings.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("The scale.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    public CompassHudElement(Hud hud) {
        super(hud, "Compass", "Displays a compass.");
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(100 * scaleSetting.get(), 100 * scaleSetting.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX() + (box.width / 2);
        double y = box.getY() + (box.height / 2);

        double pitch = isInEditor() ? 120 : MathHelper.clamp(mc.player.getPitch() + 30, -90, 90);
        pitch = Math.toRadians(pitch);

        double yaw = isInEditor() ? 180 : MathHelper.wrapDegrees(mc.player.getYaw());
        yaw = Math.toRadians(yaw);

        for (Direction direction : Direction.values()) {
            String axis = modeSetting.get() == Mode.Axis ? direction.getAxis() : direction.name();
            renderer.text(axis, (x + getX(direction, yaw)) - (renderer.textWidth(axis) / 2), (y + getY(direction, yaw, pitch)) - (renderer.textHeight() / 2), direction == Direction.North ? RED : hud.primaryColorSetting.get());
        }
    }

    private double getX(Direction direction, double yaw) {
        return Math.sin(getPos(direction, yaw)) * scaleSetting.get() * 40;
    }

    private double getY(Direction direction, double yaw, double pitch) {
        return Math.cos(getPos(direction, yaw)) * Math.sin(pitch) * scaleSetting.get() * 40;
    }

    private double getPos(Direction direction, double yaw) {
        return yaw + direction.ordinal() * Math.PI / 2;
    }

    private enum Direction {
        North("Z-"),
        West("X-"),
        South("Z+"),
        East("X+");

        private final String axis;

        Direction(String axis) {
            this.axis = axis;
        }

        public String getAxis() {
            return axis;
        }
    }

    public enum Mode {
        Direction("Direction"),
        Axis("Axis");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
