package xyz.mathax.client.addons;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import xyz.mathax.client.MatHax;
import xyz.mathax.client.utils.network.versions.Version;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<MatHaxAddon> ADDONS = new ArrayList<>();

    public static void init() {
        {
            MatHax.ADDON = new MatHaxAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "xyz.mathax.client";
                }

                @Override
                public String getWebsite() {
                    return "https://mathaxclient.xyz";
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(MatHax.ID).get().getMetadata();

            MatHax.ADDON.name = metadata.getName();
            MatHax.ADDON.version = new Version(metadata.getVersion().getFriendlyString());
            MatHax.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(MatHax.ID + ":color")) {
                MatHax.ADDON.color.parse(metadata.getCustomValue(MatHax.ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                MatHax.ADDON.authors[i++] = author.getName();
            }
        }

        // Addons
        for (EntrypointContainer<MatHaxAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("mathax", MatHaxAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            MatHaxAddon addon = entrypoint.getEntrypoint();

            addon.name = metadata.getName();

            addon.version = new Version(metadata.getVersion().getFriendlyString());

            if (metadata.getAuthors().isEmpty()) {
                throw new RuntimeException("Addon %s requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
            }

            addon.authors = new String[metadata.getAuthors().size()];

            if (metadata.containsCustomValue(MatHax.ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(MatHax.ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}