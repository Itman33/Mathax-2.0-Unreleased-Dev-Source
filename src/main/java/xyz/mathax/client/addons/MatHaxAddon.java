package xyz.mathax.client.addons;

import xyz.mathax.client.utils.network.versions.Version;
import xyz.mathax.client.utils.render.color.Color;

public abstract class MatHaxAddon {
    public String name;

    public Version version;

    public String[] authors;

    public final Color color = new Color(255, 255, 255);

    public abstract void onInitialize();

    public void onRegisterCategories() {}

    public abstract String getPackage();

    public String getWebsite() {
        return null;
    }
}