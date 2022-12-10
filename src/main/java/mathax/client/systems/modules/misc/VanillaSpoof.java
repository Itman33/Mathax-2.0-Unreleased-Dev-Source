package mathax.client.systems.modules.misc;

import io.netty.buffer.Unpooled;
import mathax.client.MatHax;
import mathax.client.eventbus.EventHandler;
import mathax.client.events.packets.PacketEvent;
import mathax.client.mixin.CustomPayloadC2SPacketAccessor;
import mathax.client.settings.Setting;
import mathax.client.settings.SettingGroup;
import mathax.client.settings.StringSetting;
import mathax.client.systems.modules.Category;
import mathax.client.systems.modules.Module;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class VanillaSpoof extends Module {
    private final SettingGroup generalSettings = settings.createGroup("General");

    // General

    private final Setting<String> brandSetting = generalSettings.add(new StringSetting.Builder()
            .name("Brand")
            .description("Specify the brand that will be sent to the server.")
            .defaultValue("vanilla")
            .build()
    );

    public VanillaSpoof(Category category) {
        super(category, "Vanilla Spoof", "Spoofs the client name when connecting to a server.");

        MatHax.EVENT_BUS.subscribe(new Listener());
    }

    private class Listener {
        @EventHandler
        private void onPacketSend(PacketEvent.Send event) {
            if (!isEnabled() || !(event.packet instanceof CustomPayloadC2SPacket)) {
                return;
            }

            String customBrand = brandSetting.get();
            CustomPayloadC2SPacketAccessor packet = (CustomPayloadC2SPacketAccessor) event.packet;
            Identifier id = packet.getChannel();

            if (id.equals(CustomPayloadC2SPacket.BRAND)) {
                packet.setData(new PacketByteBuf(Unpooled.buffer()).writeString(customBrand));
            } else if (StringUtils.containsIgnoreCase(packet.getData().toString(StandardCharsets.UTF_8), "fabric") && customBrand.equalsIgnoreCase("fabric")) {
                event.cancel();
            }
        }
    }
}
