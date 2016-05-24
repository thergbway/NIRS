package nirs.api.model;

import nirs.api.Cipher;

import java.io.Serializable;

public class FileInfo implements Serializable{

    private String id;
    private String filename;
    private Long createdTimestamp;
    private Long size;
    private Cipher cipher;

    private FileInfo(String id, String filename, Long createdTimestamp, Long size, Cipher cipher) {
        this.id = id;
        this.filename = filename;
        this.createdTimestamp = createdTimestamp;
        this.size = size;
        this.cipher = cipher;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
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
        private Long createdTimestamp;
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

        public FileInfoBuilder createdTimestamp(Long createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
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
            return new FileInfo(id, filename, createdTimestamp, size, cipher);
        }
    }
}
