package mathax.client.systems.hud.elements;

import mathax.client.systems.hud.DoubleTextHudElement;
import mathax.client.systems.hud.Hud;
import mathax.client.utils.misc.HorizontalDirection;

public class RotationHudElement extends DoubleTextHudElement {
    public RotationHudElement(Hud hud) {
        super(hud, "Rotation", "Displays your rotation.");
    }

    @Override
    protected String getLeft() {
        HorizontalDirection direction = HorizontalDirection.get(mc.gameRenderer.getCamera().getYaw());
        return String.format(name + ": %s %s ", direction.name, direction.axis);
    }

    @Override
    protected String getRight() {
        float yaw = mc.gameRenderer.getCamera().getYaw() % 360;
        if (yaw < 0) {
            yaw += 360;
        }

        if (yaw > 180) {
            yaw -= 360;
        }

        float pitch = mc.gameRenderer.getCamera().getPitch() % 360;
        if (pitch < 0) {
            pitch += 360;
        }

        if (pitch > 180) {
            pitch -= 360;
        }

        return String.format("%.1f %.1f", yaw, pitch);
    }
}
