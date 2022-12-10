package mathax.client.systems.hud.elements;

import mathax.client.settings.ColorSetting;
import mathax.client.settings.EnumSetting;
import mathax.client.settings.Setting;
import mathax.client.settings.SettingGroup;
import mathax.client.systems.hud.Hud;
import mathax.client.systems.hud.TripleTextHudElement;
import mathax.client.utils.render.color.SettingColor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WelcomeHudElement extends TripleTextHudElement {
    private final SettingGroup generalSettings = settings.createGroup("General");

    // General

    private final Setting<Message> messageSetting = generalSettings.add(new EnumSetting.Builder<Message>()
            .name("Message")
            .description("Determines what message style to use.")
            .defaultValue(Message.Welcome)
            .build()
    );

    //TODO: Rainbow doesnt update :)))
    private final Setting<SettingColor> usernameColorSetting = generalSettings.add(new ColorSetting.Builder()
            .name("Username Color")
            .description("Color of the username.")
            .defaultValue(new SettingColor(true))
            .build()
    );

    public WelcomeHudElement(Hud hud) {
        super(hud, "Welcome", "Displays a welcome message.", true);
        centerColor = usernameColorSetting.get();
    }

    @Override
    protected String getLeft() {
        switch (messageSetting.get()) {
            case Using -> {
                return "You are using MatHax, ";
            }
            case Time -> {
                return getTime() + ", ";
            }
            case Retarded_Time -> {
                return getRetardedTime() + ", ";
            }
            case Sussy -> {
                return "You are a sussy baka, ";
            }
            default -> {
                return "Welcome to MatHax, ";
            }
        }
    }

    @Override
    protected String getCenter() {
        return mc.getSession().getProfile().getName();
    }

    @Override
    public String getRight() {
        return "!";
    }

    private String getTime() {
        int hour = Integer.parseInt(new SimpleDateFormat("k").format(new Date()));

        if (hour < 6) {
            return "Good Night";
        }

        if (hour < 12) {
            return "Good Morning";
        }

        if (hour < 17) {
            return "Good Afternoon";
        }

        if (hour < 20) {
            return "Good Evening";
        }

        return "Good Night";
    }

    private String getRetardedTime() {
        int hour = Integer.parseInt(new SimpleDateFormat("k").format(new Date()));

        if (hour < 3) {
            return "Why are you killing newfags at this hour retard";
        }

        if (hour < 6) {
            return "You really need get some sleep retard";
        }

        if (hour < 9) {
            return "Ur awake already? such a retard";
        }

        if (hour < 12) {
            return "Retard moment";
        }

        if (hour < 14) {
            return "Go eat lunch retard";
        }

        if (hour < 17) {
            return "Retard playing minecraft";
        }

        return "Time to sleep retard";
    }

    public enum Message {
        Welcome("Welcome"),
        Using("Using"),
        Time("Time"),
        Retarded_Time("Retarded time"),
        Sussy("Sussy");

        private final String title;

        Message(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}