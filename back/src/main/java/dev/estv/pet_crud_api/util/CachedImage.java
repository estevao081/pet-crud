package dev.estv.pet_crud_api.util;

import java.io.Serializable;

public class CachedImage implements Serializable {

    private final byte[] data;
    private final String contentType;

    public CachedImage(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}