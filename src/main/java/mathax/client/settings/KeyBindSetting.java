package mathax.client.settings;

import mathax.client.MatHax;
import mathax.client.eventbus.EventHandler;
import mathax.client.eventbus.EventPriority;
import mathax.client.events.mathax.KeyEvent;
import mathax.client.events.mathax.MouseButtonEvent;
import mathax.client.gui.widgets.WKeybind;
import mathax.client.utils.input.KeyBind;
import mathax.client.utils.input.KeyAction;
import mathax.client.utils.settings.IVisible;
import org.json.JSONObject;

import java.util.function.Consumer;

public class KeyBindSetting extends Setting<KeyBind> {
    private final Runnable action;

    public WKeybind widget;

    public KeyBindSetting(String name, String description, KeyBind defaultValue, Consumer<KeyBind> onChanged, Consumer<Setting<KeyBind>> onModuleActivated, IVisible visible, Runnable action) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.action = action;

        MatHax.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Release && widget != null && widget.onAction(true, event.key)) {
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMouseButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && widget != null && widget.onAction(false, event.button)) {
            event.cancel();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release && get().matches(true, event.key) && (module == null || module.isEnabled()) && action != null) {
            action.run();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && get().matches(false, event.button) && (module == null || module.isEnabled()) && action != null) {
            action.run();
        }
    }

    @Override
    public void resetImpl() {
        if (value == null) {
            value = defaultValue.copy();
        } else {
            value.set(defaultValue);
        }

        if (widget != null) {
            widget.reset();
        }
    }

    @Override
    protected KeyBind parseImpl(String string) {
        try {
            return KeyBind.fromKey(Integer.parseInt(string.trim()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(KeyBind value) {
        return true;
    }

    @Override
    public JSONObject save(JSONObject json) {
        json.put("value", get().toJson());

        return json;
    }

    @Override
    public KeyBind load(JSONObject json) {
        get().fromJson(json.getJSONObject("value"));

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, KeyBind, KeyBindSetting> {
        private Runnable action;

        public Builder() {
            super(KeyBind.none());
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        @Override
        public KeyBindSetting build() {
            return new KeyBindSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, action);
        }
    }
}
