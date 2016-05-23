package nirs.api.model;

import nirs.api.Cipher;

import java.io.Serializable;
import java.time.Instant;

public class FileInfo implements Serializable{

    private final String id;
    private final String filename;
    private final Instant createdInstant;
    private final Long size;
    private final Cipher cipher;

    private FileInfo(String id, String filename, Instant createdInstant, Long size, Cipher cipher) {
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

    public static FileInfoBuilder builder() {
        return new FileInfoBuilder();
    }

    public static class FileInfoBuilder {

        private String id;
        private String filename;
        private Instant createdInstant;
        private Long size;
        private Cipher cipher;

        public FileInfoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FileInfoBuilder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public FileInfoBuilder createdInstant(Instant createdInstant) {
            this.createdInstant = createdInstant;
            return this;
        }

        public FileInfoBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public FileInfoBuilder cipher(Cipher cipher) {
            this.cipher = cipher;
            return this;
        }

        public FileInfo build() {
            return new FileInfo(id, filename, createdInstant, size, cipher);
        }
    }
}
