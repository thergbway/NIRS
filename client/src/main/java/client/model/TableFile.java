package client.model;

import javafx.beans.property.SimpleStringProperty;
import nirs.api.Cipher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TableFile {

    public final SimpleStringProperty fileName;
    public final SimpleStringProperty created;
    public final SimpleStringProperty size;
    public final SimpleStringProperty cipher;
    public final SimpleStringProperty status;

    private String id;

    public TableFile(String fileName, Long createdTimestamp, Long size, Cipher cipher, String id) {
        this.id = id;
        this.fileName = new SimpleStringProperty(fileName);
        this.created = new SimpleStringProperty(LocalDateTime.ofEpochSecond(createdTimestamp, 0, ZoneOffset.UTC).toString());
        this.size = new SimpleStringProperty(convertToHumanReadable(size));
        this.cipher = new SimpleStringProperty(cipher.toString());
        this.status = new SimpleStringProperty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String convertToHumanReadable(Long size) {
        return String.format("%1.3f", size.doubleValue() / 1024.0 / 1024.0);
    }
}
