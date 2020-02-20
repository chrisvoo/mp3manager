package it.chrisvoo.scanner;

import com.mpatric.mp3agic.*;
import it.chrisvoo.db.BitrateType;
import it.chrisvoo.db.FileDocument;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * mp3agic tests
 */
public class MusicMetadataTest {

    /**
     * It can read metadata from an MP3
     */
    @Test
    public void canReadMetadata() {
        try {
            Path path = Paths.get("./target/test-classes/Under The Ice (Scene edit).mp3");
            FileDocument musicFile = new FileDocument(new Mp3File(path));
            assertEquals(BitrateType.CONSTANT, musicFile.getBitrateType());
            assertEquals(5235428, musicFile.getSize());
            assertEquals(320, musicFile.getBitrate());
            assertEquals(
                    "." + File.separator +
                    "target" + File.separator +
                    "test-classes" + File.separator +
                    "Under The Ice (Scene edit).mp3",
                    musicFile.getFileName()
            );
            assertEquals(129, musicFile.getDuration());
            assertFalse(musicFile.hasCustomTag());
            assertFalse(musicFile.hasID3v1Tag());
            assertTrue(musicFile.hasID3v2Tag());
            assertEquals("Lives Of The Artists: Follow Me Down - Soundtrack", musicFile.getAlbumTitle());
            assertEquals("image/jpeg", musicFile.getAlbumImageMimeType());
            assertNull(musicFile.getGenre());
            assertEquals("UNKLE", musicFile.getArtist());
            assertEquals("Under The Ice (Scene edit)", musicFile.getTitle());
            assertEquals("2010-01-01", musicFile.getYear());
        } catch (Exception e) {
            fail("Cannot read the metadata", e);
        }
    }
}
