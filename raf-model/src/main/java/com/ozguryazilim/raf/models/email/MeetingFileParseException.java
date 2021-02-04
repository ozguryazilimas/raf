package com.ozguryazilim.raf.models.email;

/**
 *
 * @author oyas
 */
public class MeetingFileParseException extends Exception {

    public MeetingFileParseException() {
    }

    public MeetingFileParseException(String message) {
        super(message);
    }

    public MeetingFileParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MeetingFileParseException(Throwable cause) {
        super(cause);
    }

    public MeetingFileParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}