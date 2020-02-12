package it.chrisvoo;

import com.mpatric.mp3agic.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
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
            Mp3File mp3 = new Mp3File(path);

            assertFalse(mp3.isVbr());
            assertEquals(5235428, mp3.getLength());
            assertEquals(320, mp3.getBitrate());
            assertEquals("Joint stereo",  mp3.getChannelMode());
            assertEquals(
                    "." + File.separator +
                    "target" + File.separator +
                    "test-classes" + File.separator +
                    "Under The Ice (Scene edit).mp3",
                    mp3.getFilename()
            );
            assertEquals(129, mp3.getLengthInSeconds());
            assertEquals(44100, mp3.getSampleRate());
            assertFalse(mp3.hasCustomTag());
            assertFalse(mp3.hasId3v1Tag());
            assertTrue(mp3.hasId3v2Tag());

            ID3Wrapper wrapper = new ID3Wrapper(mp3.getId3v1Tag(), mp3.getId3v2Tag());
            assertEquals("Lives Of The Artists: Follow Me Down - Soundtrack", wrapper.getAlbum());
            assertEquals("image/jpeg", wrapper.getAlbumImageMimeType());
            assertNull(wrapper.getGenreDescription());
            assertEquals("UNKLE", wrapper.getArtist());
            assertEquals("Under The Ice (Scene edit)", wrapper.getTitle());
            assertNull(wrapper.getYear());

            AbstractID3v2Tag tag = ID3v2TagFactory.createTag(Files.readAllBytes(path));
            assertTrue(tag instanceof ID3v24Tag);
            ID3v24Tag theTag = (ID3v24Tag) tag;
            assertEquals("2010-01-01", theTag.getRecordingTime());
        } catch (Exception e) {
            fail("Cannot read the metadata", e);
        }
    }
}
