package mathax.client.mixin;

import mathax.client.systems.modules.Modules;
import mathax.client.systems.modules.misc.AutoReconnect;
import mathax.client.utils.network.LastServerInfo;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow
    @Final
    private Screen parent;

    @Shadow
    private int reasonHeight;

    @Unique
    private ButtonWidget autoReconnectBtn;

    @Unique
    private double time = Modules.get().get(AutoReconnect.class).timeSetting.get();

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        if (LastServerInfo.getLastServer() != null) {
            int x = width / 2 - 100;
            int y = Math.min((height / 2 + reasonHeight / 2) + 32, height - 30);
            int x2 = width / 2 - 100;
            int y2 = Math.min((height / 2 + reasonHeight / 2) + 56, height - 30);

            addDrawableChild(new ButtonWidget(x, y, 200, 20, Text.literal("Reconnect"), button -> {
                LastServerInfo.reconnect(parent);
            }));

            AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
            if (autoReconnect != null) {
                autoReconnectBtn = addDrawableChild(new ButtonWidget(x2, y2, 200, 20, Text.literal(getText()), button -> {
                    autoReconnect.toggle();
                    if (!autoReconnect.isEnabled()) {
                        time = autoReconnect.timeSetting.get();
                        ((AbstractButtonWidgetAccessor) autoReconnectBtn).setText(Text.literal(getText()));
                    }
                }));
            }
        }
    }

    @Override
    public void tick() {
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect == null || LastServerInfo.getLastServer() == null) {
            return;
        }

        if (autoReconnect.isEnabled()) {
            if (time <= 0) {
                LastServerInfo.reconnect(parent);
            } else {
                time--;
            }
        } else {
            time = autoReconnect.timeSetting.get();
        }

        ((AbstractButtonWidgetAccessor) autoReconnectBtn).setText(Text.literal(getText()));
    }

    private String getText() {
        String autoReconnectText = "Auto Reconnect (" + String.format("%.1f" + "s", time / 20) + ")";
        if (Modules.get().isEnabled(AutoReconnect.class)) {
            autoReconnectText = "Reconnecting in " + String.format("%.1f" + "s...", time / 20);
        }

        return autoReconnectText;
    }
}