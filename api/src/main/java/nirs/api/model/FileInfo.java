package nirs.api.model;

import nirs.api.Cipher;

import java.time.Instant;

public class FileInfo {
    private final String id;
    private final String filename;
    private final Instant createdInstant;
    private final Long size;
    private final Cipher cipher;

    public FileInfo(String id, String filename, Instant createdInstant, Long size, Cipher cipher) {
        this.id = id;
        this.filename = filename;
        this.createdInstant = createdInstant;
        this.size = size;
        this.cipher = cipher;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Instant getCreatedInstant() {
        return createdInstant;
    }

    public Long getSize() {
        return size;
    }

    public Cipher getCipher() {
        return cipher;
    }
}
