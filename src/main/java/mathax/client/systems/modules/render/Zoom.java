package mathax.client.systems.modules.render;

import mathax.client.MatHax;
import mathax.client.eventbus.EventHandler;
import mathax.client.events.mathax.MouseScrollEvent;
import mathax.client.events.render.GetFovEvent;
import mathax.client.events.render.Render3DEvent;
import mathax.client.events.world.TickEvent;
import mathax.client.settings.BoolSetting;
import mathax.client.settings.DoubleSetting;
import mathax.client.settings.Setting;
import mathax.client.settings.SettingGroup;
import mathax.client.systems.modules.Category;
import mathax.client.systems.modules.Module;
import mathax.client.utils.Utils;
import net.minecraft.util.math.MathHelper;

public class Zoom extends Module {
    private boolean enabled;
    private boolean preCinematic;

    private double preMouseSensitivity;
    private double value;
    private double lastFov;
    private double time;

    private final SettingGroup generalSettings = settings.createGroup("General");

    // General

    private final Setting<Double> zoomSetting = generalSettings.add(new DoubleSetting.Builder()
            .name("Zoom")
            .description("How much to zoom.")
            .defaultValue(6)
            .min(1)
            .sliderRange(1, 10)
            .build()
    );

    private final Setting<Double> scrollSensitivitySetting = generalSettings.add(new DoubleSetting.Builder()
            .name("Scroll sensitivity")
            .description("Allows you to change zoom value using scroll wheel. (0 to disable)")
            .defaultValue(1)
            .min(0)
            .sliderRange(0, 5)
            .build()
    );

    private final Setting<Boolean> smoothSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Smooth")
            .description("Smooth transition.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> cinematicSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Cinematic")
            .description("Enable cinematic camera.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> renderHandsSetting = generalSettings.add(new BoolSetting.Builder()
            .name("Show hands")
            .description("Whether or not to render your hands.")
            .defaultValue(true)
            .build()
    );

    public Zoom(Category category) {
        super(category, "Zoom", "Zooms your view.");
        autoSubscribe = false;
    }

    @Override
    public void onEnable() {
        if (!enabled) {
            preCinematic = mc.options.smoothCameraEnabled;
            preMouseSensitivity = mc.options.getMouseSensitivity().getValue();
            value = zoomSetting.get();
            lastFov = mc.options.getFov().getValue();
            time = 0.001;

            MatHax.EVENT_BUS.subscribe(this);
            enabled = true;
        }
    }

    public void onStop() {
        mc.options.smoothCameraEnabled = preCinematic;
        mc.options.getMouseSensitivity().setValue(preMouseSensitivity);

        mc.worldRenderer.scheduleTerrainUpdate();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        mc.options.smoothCameraEnabled = cinematicSetting.get();

        if (!cinematicSetting.get()) {
            mc.options.getMouseSensitivity().setValue(preMouseSensitivity / Math.max(value() * 0.5, 1));
        }

        if (time == 0) {
            MatHax.EVENT_BUS.unsubscribe(this);
            enabled = false;

            onStop();
        }
    }

    @EventHandler
    private void onMouseScroll(MouseScrollEvent event) {
        if (scrollSensitivitySetting.get() > 0 && isEnabled()) {
            value += event.value * 0.25 * (scrollSensitivitySetting.get() * value);
            if (value < 1) {
                value = 1;
            }

            event.cancel();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!smoothSetting.get()) {
            time = isEnabled() ? 1 : 0;
            return;
        }

        if (isEnabled()) {
            time += event.frameTime * 5;
        } else {
            time -= event.frameTime * 5;
        }

        time = Utils.clamp(time, 0, 1);
    }

    @EventHandler
    private void onGetFov(GetFovEvent event) {
        event.fov /= value();

        if (lastFov != event.fov) {
            mc.worldRenderer.scheduleTerrainUpdate();
        }

        lastFov = event.fov;
    }

    private double value() {
        double delta = time < 0.5 ? 4 * time * time * time : 1 - Math.pow(-2 * time + 2, 3) / 2; // Ease in out cubic
        return MathHelper.lerp(delta, 1, value);
    }

    public boolean renderHands() {
        return !isEnabled() || renderHandsSetting.get();
    }
}