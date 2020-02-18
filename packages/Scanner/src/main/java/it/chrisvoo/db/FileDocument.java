package it.chrisvoo.db;

import com.mpatric.mp3agic.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This will be the class passed to MongoClient and maps the MP3 metadata
 */
public class FileDocument {
    private long size;
    private int bitrate;
    private BitrateType bitrateType;
    private String fileName;
    private long duration;
    private boolean hasCustomTag;
    private boolean hasID3v1Tag;
    private boolean hasID3v2Tag;
    private String albumTitle;
    private boolean hasAlbumImage;
    private String albumImageMymeType;
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
           setAlbumImageMymeType(wrapper.getAlbumImageMimeType()).
           setAlbumImage(albumImage);
        }

        setGenre(wrapper.getGenreDescription()).
        setArtist(
           wrapper.getArtist() != null && !wrapper.getArtist().isBlank()
            ? wrapper.getArtist().trim()
            : wrapper.getAlbumArtist().trim()
        ).
        setTitle(wrapper.getTitle());

        String year = wrapper.getYear();
        if (year != null && !year.trim().isBlank()) {
            setYear(year.trim());
        } else if (hasID3v2Tag()) {
            AbstractID3v2Tag tag = ID3v2TagFactory.createTag(
                Files.readAllBytes(
                    Paths.get(file.getFilename())
                )
            );
            ID3v24Tag theTag = (ID3v24Tag) tag;
            setYear(theTag.getRecordingTime());
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

    public boolean isHasCustomTag() {
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

    public String getAlbumImageMymeType() {
        return albumImageMymeType;
    }

    public FileDocument setAlbumImageMymeType(String albumImageMymeType) {
        this.albumImageMymeType = albumImageMymeType;
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
}
