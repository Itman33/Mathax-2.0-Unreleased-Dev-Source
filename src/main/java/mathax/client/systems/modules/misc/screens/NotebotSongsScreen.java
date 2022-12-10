package mathax.client.systems.modules.misc.screens;

import mathax.client.MatHax;
import mathax.client.gui.WindowScreen;
import mathax.client.gui.widgets.containers.WTable;
import mathax.client.gui.widgets.input.WTextBox;
import mathax.client.gui.widgets.pressable.WButton;
import mathax.client.systems.modules.Modules;
import mathax.client.systems.modules.misc.Notebot;
import mathax.client.systems.themes.Theme;
import mathax.client.utils.Utils;
import mathax.client.utils.notebot.decoder.SongDecoders;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotebotSongsScreen extends WindowScreen {
    private static final Notebot notebot = Modules.get().get(Notebot.class);

    private WTextBox filter;

    private String filterText = "";

    private WTable table;

    public NotebotSongsScreen(Theme theme) {
        super(theme, "Notebot Songs");
    }

    @Override
    public void initWidgets() {
        WButton randomSong = add(theme.button("Random Song")).minWidth(400).expandX().widget();
        randomSong.action = notebot::playRandomSong;

        filter = add(theme.textBox("Search for the songs...")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initSongsTable();
        };

        table = add(theme.table()).widget();

        initSongsTable();
    }

    private void initSongsTable() {
        AtomicBoolean noSongsFound = new AtomicBoolean(true);
        try {
            Files.list(MatHax.FOLDER.toPath().resolve("Notebot")).forEach(path -> {
                if (SongDecoders.hasDecoder(path)) {
                    String name = path.getFileName().toString();
                    if (Utils.searchTextDefault(name, filterText, false)){
                        addPath(path);
                        noSongsFound.set(false);
                    }
                }
            });
        } catch (IOException exception) {
            table.add(theme.label("Missing MatHax/Notebot folder.")).expandCellX();
            table.row();
        }

        if (noSongsFound.get()) {
            table.add(theme.label("No songs found.")).expandCellX().center();
        }
    }

    private void addPath(Path path) {
        table.add(theme.horizontalSeparator()).expandX().minWidth(400);
        table.row();

        table.add(theme.label(FilenameUtils.getBaseName(path.getFileName().toString()))).expandCellX();
        WButton load = table.add(theme.button("Load")).right().widget();
        load.action = () -> {
            notebot.loadSong(path.toFile());
        };
        WButton preview = table.add(theme.button("Preview")).right().widget();
        preview.action = () -> {
            notebot.previewSong(path.toFile());
        };

        table.row();
    }
}