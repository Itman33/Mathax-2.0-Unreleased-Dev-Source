package xyz.mathax.client.mixin;

import xyz.mathax.client.gui.renderer.OverlayRenderer;
import xyz.mathax.client.systems.Systems;
import xyz.mathax.client.systems.modules.Modules;
import xyz.mathax.client.systems.modules.misc.NameProtect;
import xyz.mathax.client.systems.proxies.Proxies;
import xyz.mathax.client.systems.proxies.Proxy;
import xyz.mathax.client.systems.themes.Theme;
import xyz.mathax.client.systems.themes.Themes;
import xyz.mathax.client.utils.network.LastServerInfo;
import xyz.mathax.client.utils.render.LoggedProxyText;
import xyz.mathax.client.utils.render.TitleScreenCredits;
import xyz.mathax.client.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    public MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        Theme theme = Systems.get(Themes.class).getTheme();

        addDrawableChild(new ButtonWidget.Builder(Text.literal("Accounts"), button -> client.setScreen(theme.accountsScreen())).position(this.width - 75 - 3, 3).size(75, 20).build());

        addDrawableChild(new ButtonWidget.Builder(Text.literal("Proxies"), button -> client.setScreen(theme.proxiesScreen())).position(this.width - 75 - 3 - 75 - 2, 3).size(75, 20).build());

        if (LastServerInfo.getLastServer() != null) {
            addDrawableChild(new ButtonWidget.Builder(Text.literal("Accounts"), button -> client.setScreen(theme.accountsScreen())).position(this.width / 2 - 154, 10).size(100, 20).build());
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo info) {
        LoggedProxyText.render(matrixStack);
    }

    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/network/ServerInfo;)V")
    private void onConnect(ServerInfo serverInfo, CallbackInfo info) {
        LastServerInfo.setLastServer(serverInfo);
    }
}