package mathax.client.mixin;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    public GenericContainerScreenMixin(GenericContainerScreenHandler container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    /*@Override
    protected void init() {
        super.init();

        InventoryTweaks inventoryTweaks = Modules.get().get(InventoryTweaks.class);
        if (inventoryTweaks.isEnabled() && inventoryTweaks.showButtons()) {
            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 88,
                y + 3,
                40,
                12,
                Text.literal("Steal"),
                button -> inventoryTweaks.steal(handler))
            );

            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 46,
                y + 3,
                40,
                12,
                Text.literal("Dump"),
                button -> inventoryTweaks.dump(handler))
            );
        }
    }*/
}
