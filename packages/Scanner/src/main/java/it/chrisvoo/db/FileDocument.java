package it.chrisvoo.db;

/**
 * This will be the class passed to MongoClient
 */
public class FileDocument {
    private int size;
    private int bitrate;
    private String fileName;
    private long duration;
    private boolean hasCustomTag;
    private boolean hasID3v1Tag;
    private boolean hasID3v2Tag;
    private String albumTitle;
    private String hasAlbumImage;
    private String albumImageMymeType;
    private byte[] albumImage;
    private String genre;
    private String title;
    private int year;

    public int getSize() {
        return size;
    }

    public FileDocument setSize(int size) {
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

    public boolean isHasID3v1Tag() {
        return hasID3v1Tag;
    }

    public FileDocument setHasID3v1Tag(boolean hasID3v1Tag) {
        this.hasID3v1Tag = hasID3v1Tag;
        return this;
    }

    public boolean isHasID3v2Tag() {
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

    public String getHasAlbumImage() {
        return hasAlbumImage;
    }

    public FileDocument setHasAlbumImage(String hasAlbumImage) {
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

    public int getYear() {
        return year;
    }

    public FileDocument setYear(int year) {
        this.year = year;
        return this;
    }
}
