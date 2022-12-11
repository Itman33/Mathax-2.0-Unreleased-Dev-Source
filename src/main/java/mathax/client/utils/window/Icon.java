package mathax.client.utils.window;

import mathax.client.utils.misc.MatHaxIdentifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;

import static mathax.client.MatHax.mc;

public class Icon {
    public static boolean iconChanged = false;

    public static void setIcon(MatHaxIdentifier icon1, MatHaxIdentifier icon2) {
        mc.getWindow().setIcon(MinecraftClient.getInstance().getResourceManager().getResource(icon1).get().getPack().open(ResourceType.CLIENT_RESOURCES, icon1), MinecraftClient.getInstance().getResourceManager().getResource(icon2).get().getPack().open(ResourceType.CLIENT_RESOURCES, icon2));
        iconChanged = true;
    }

    public static void setMinecraft() {
        mc.getWindow().setIcon(MinecraftClient.getInstance().getDefaultResourcePack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_16x16.png")), MinecraftClient.getInstance().getDefaultResourcePack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_32x32.png")));
        iconChanged = false;
    }
}
