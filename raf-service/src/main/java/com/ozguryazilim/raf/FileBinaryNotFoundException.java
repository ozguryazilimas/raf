package com.ozguryazilim.raf;

public class FileBinaryNotFoundException extends RafException{
    public FileBinaryNotFoundException(String message) {
        super(message);
    }

    public FileBinaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
