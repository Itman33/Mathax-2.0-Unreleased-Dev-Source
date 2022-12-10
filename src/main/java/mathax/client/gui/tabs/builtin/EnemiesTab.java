package mathax.client.gui.tabs.builtin;

import mathax.client.systems.enemies.Enemies;
import mathax.client.systems.enemies.Enemy;
import mathax.client.systems.themes.Theme;
import mathax.client.gui.tabs.Tab;
import mathax.client.gui.tabs.TabScreen;
import mathax.client.gui.tabs.WindowTabScreen;
import mathax.client.gui.widgets.containers.WHorizontalList;
import mathax.client.gui.widgets.containers.WTable;
import mathax.client.gui.widgets.input.WTextBox;
import mathax.client.gui.widgets.pressable.WMinus;
import mathax.client.gui.widgets.pressable.WPlus;
import mathax.client.utils.network.Executor;
import net.minecraft.client.gui.screen.Screen;

public class EnemiesTab extends Tab {
    public EnemiesTab() {
        super("Enemies");
    }

    @Override
    public TabScreen createScreen(Theme theme) {
        return new EnemiesScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof EnemiesScreen;
    }

    private static class EnemiesScreen extends WindowTabScreen {
        public EnemiesScreen(Theme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = add(theme.table()).expandX().minWidth(400).widget();
            initTable(table);

            add(theme.horizontalSeparator()).expandX();

            // New
            WHorizontalList list = add(theme.horizontalList()).expandX().widget();

            WTextBox nameW = list.add(theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
            nameW.setFocused(true);

            WPlus add = list.add(theme.plus()).widget();
            add.action = () -> {
                String name = nameW.get().trim();
                Enemy enemy = new Enemy(name);
                if (Enemies.get().add(enemy)) {
                    nameW.set("");
                    reload();

                    Executor.execute(() -> {
                        enemy.updateInfo();
                        reload();
                    });
                }
            };

            enterAction = add.action;
        }

        private void initTable(WTable table) {
            table.clear();

            if (Enemies.get().isEmpty()) {
                return;
            }

            for (Enemy enemy : Enemies.get()) {
                table.add(theme.texture(32, 32, enemy.getHead().needsRotate() ? 90 : 0, enemy.getHead()));
                table.add(theme.label(enemy.getName()));

                WMinus remove = table.add(theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Enemies.get().remove(enemy);
                    reload();
                };

                table.row();
            }
        }
    }
}