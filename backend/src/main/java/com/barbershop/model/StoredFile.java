package com.barbershop.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stored_files")
public class StoredFile {
    @Id
    private String id;

    private String fileName;
    private String contentType;

    @Column(name = "data")
    private byte[] data;

    public StoredFile() {
    }

    public StoredFile(String fileName, String contentType, byte[] data) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
