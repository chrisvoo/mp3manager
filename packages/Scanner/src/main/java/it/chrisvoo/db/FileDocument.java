package it.chrisvoo.db;

import com.mpatric.mp3agic.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This will be the class passed to MongoClient and maps the MP3 metadata.
 * By default all POJOs <b>must</b> include a public or protected, empty, no arguments, constructor.
 */
public class FileDocument {
    @BsonId
    private ObjectId id;

    private long size;
    private int bitrate;

    @BsonProperty(value = "bitrate_type")
    private BitrateType bitrateType;

    @BsonProperty(value = "filename")
    private String fileName;
    private long duration;

    @BsonProperty(value = "has_custom_tag")
    private boolean hasCustomTag;

    @BsonProperty(value = "has_id3v1_tag")
    private boolean hasID3v1Tag;

    @BsonProperty(value = "has_id3v2_tag")
    private boolean hasID3v2Tag;

    @BsonProperty(value = "album_title")
    private String albumTitle;

    @BsonProperty(value = "has_album_image")
    private boolean hasAlbumImage;

    @BsonProperty(value = "album_image_mime_type")
    private String albumImageMimeType;

    @BsonProperty(value = "album_image")
    private byte[] albumImage;

    private String genre;
    private String title;
    private String artist;
    private String year;

    /**
     * If you want to manually set all the properties, use this
     * constructor
     */
    public FileDocument() {}

    /**
     * Automatically initialize all the properties with the ones belonging
     * to the {@link Mp3File} file instance
     * @param file The Mp3File instance
     */
    public FileDocument(Mp3File file) throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {
        setBitrateType(file.isVbr() ? BitrateType.VARIABLE : BitrateType.CONSTANT).
        setBitrate(file.getBitrate()).
        setFileName(file.getFilename()).
        setSize(file.getLength()).
        setDuration(file.getLengthInSeconds()).
        setHasCustomTag(file.hasCustomTag()).
        setHasID3v1Tag(file.hasId3v1Tag()).
        setHasID3v2Tag(file.hasId3v2Tag()).
        setHasCustomTag(file.hasCustomTag());

        ID3Wrapper wrapper = new ID3Wrapper(file.getId3v1Tag(), file.getId3v2Tag());

        byte[] albumImage = wrapper.getAlbumImage();

        if (albumImage != null) {
           setHasAlbumImage(true).
           setAlbumImageMimeType(wrapper.getAlbumImageMimeType()).
           setAlbumImage(albumImage);
        }

        setGenre(wrapper.getGenreDescription());

        if (wrapper.getArtist() != null && !wrapper.getArtist().isBlank()) {
            setArtist(wrapper.getArtist().trim());
        } else if (wrapper.getAlbumArtist() != null && !wrapper.getAlbumArtist().isBlank()) {
            setArtist(wrapper.getAlbumArtist().trim());
        }

        setTitle(wrapper.getTitle()).
        setAlbumTitle(wrapper.getAlbum());

        String year = wrapper.getYear();
        if (year != null && !year.trim().isBlank()) {
            setYear(year.trim());
        } else if (hasID3v2Tag()) {
            AbstractID3v2Tag tag = ID3v2TagFactory.createTag(
                Files.readAllBytes(
                    Paths.get(file.getFilename())
                )
            );
            if (tag instanceof ID3v24Tag) {
                ID3v24Tag theTag = (ID3v24Tag) tag;
                setYear(theTag.getRecordingTime());
            }
        }
    }

    public long getSize() {
        return size;
    }

    public FileDocument setSize(long size) {
        this.size = size;
        return this;
    }

    public int getBitrate() {
        return bitrate;
    }

    public FileDocument setBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public FileDocument setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public FileDocument setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public boolean hasCustomTag() {
        return hasCustomTag;
    }

    public FileDocument setHasCustomTag(boolean hasCustomTag) {
        this.hasCustomTag = hasCustomTag;
        return this;
    }

    public boolean hasID3v1Tag() {
        return hasID3v1Tag;
    }

    public FileDocument setHasID3v1Tag(boolean hasID3v1Tag) {
        this.hasID3v1Tag = hasID3v1Tag;
        return this;
    }

    public boolean hasID3v2Tag() {
        return hasID3v2Tag;
    }

    public FileDocument setHasID3v2Tag(boolean hasID3v2Tag) {
        this.hasID3v2Tag = hasID3v2Tag;
        return this;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public FileDocument setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public FileDocument setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public boolean hasAlbumImage() {
        return hasAlbumImage;
    }

    public FileDocument setHasAlbumImage(boolean hasAlbumImage) {
        this.hasAlbumImage = hasAlbumImage;
        return this;
    }

    public String getAlbumImageMimeType() {
        return albumImageMimeType;
    }

    public FileDocument setAlbumImageMimeType(String albumImageMimeType) {
        this.albumImageMimeType = albumImageMimeType;
        return this;
    }

    public byte[] getAlbumImage() {
        return albumImage;
    }

    public FileDocument setAlbumImage(byte[] albumImage) {
        this.albumImage = albumImage;
        return this;
    }

    public String getGenre() {
        return genre;
    }

    public FileDocument setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public FileDocument setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getYear() {
        return year;
    }

    public FileDocument setYear(String year) {
        this.year = year;
        return this;
    }

    public BitrateType getBitrateType() {
        return bitrateType;
    }

    public FileDocument setBitrateType(BitrateType bitrateType) {
        this.bitrateType = bitrateType;
        return this;
    }

    public ObjectId getId() {
        return id;
    }

    public FileDocument setId(ObjectId id) {
        this.id = id;
        return this;
    }

    public boolean isHasCustomTag() {
        return hasCustomTag;
    }

    public boolean isHasID3v1Tag() {
        return hasID3v1Tag;
    }

    public boolean isHasID3v2Tag() {
        return hasID3v2Tag;
    }

    public boolean isHasAlbumImage() {
        return hasAlbumImage;
    }
}
