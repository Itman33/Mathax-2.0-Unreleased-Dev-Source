package mathax.client.utils.notebot.decoder;

import mathax.client.systems.modules.Modules;
import mathax.client.systems.modules.misc.Notebot;
import mathax.client.utils.notebot.NotebotUtils;
import mathax.client.utils.notebot.song.Note;
import mathax.client.utils.notebot.song.Song;
import net.minecraft.block.enums.Instrument;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SongDecoders {
    private static final Map<String, SongDecoder> decoders = new HashMap<>(); // file extension -> song decoder

    static {
        registerDecoder("nbs", new NBSSongDecoder());
        registerDecoder("txt", new TextSongDecoder());
        // TODO Maybe a midi decoder in the future
    }

    public static void registerDecoder(String extension, SongDecoder songDecoder) {
        decoders.put(extension, songDecoder);
    }

    public static SongDecoder getDecoder(File file) {
        return decoders.get(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean hasDecoder(File file) {
        return decoders.containsKey(FilenameUtils.getExtension(file.getName()));
    }

    public static boolean hasDecoder(Path path) {
        return hasDecoder(path.toFile());
    }

    public static Song parse(File file) {
        if (!hasDecoder(file)) {
            return null;
        }

        SongDecoder decoder = getDecoder(file);
        Song song = decoder.parse(file);

        fixSong(song);

        song.finishLoading();

        return song;
    }

    private static void fixSong(Song song) {
        Notebot notebot = Modules.get().get(Notebot.class);
        var iterator = song.getNotesMap().entries().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int tick = entry.getKey();
            Note note = entry.getValue();
            int noteLevel = note.getNoteLevel();
            if (noteLevel < 0 || noteLevel > 24) {
                if (notebot.roundOutOfRangeSetting.get()) {
                    note.setNoteLevel(noteLevel < 0 ? 0 : 24);
                } else {
                    notebot.warning("Note at tick %d out of range.", tick);
                    iterator.remove();
                    continue;
                }
            }

            if (notebot.modeSetting.get() == NotebotUtils.NotebotMode.Exact_Instruments) {
                Instrument newInstrument = notebot.getMappedInstrument(note.getInstrument());
                if (newInstrument != null) {
                    note.setInstrument(newInstrument);
                }
            } else {
                note.setInstrument(null);
            }
        }
    }
}
