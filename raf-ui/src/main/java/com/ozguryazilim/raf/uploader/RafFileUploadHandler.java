package com.ozguryazilim.raf.uploader;

import com.ozguryazilim.telve.uploader.ui.FileUploadHandler;

public interface RafFileUploadHandler extends FileUploadHandler {

    String getRafCode();

    /**
     * Upload with check zip files (if decompress true, zip is automatically decompress and extract same directory)
     *
     * @param uri
     * @param decompress
     */
    void handleFileUpload(String uri, boolean decompress, boolean generatePreview);

    /**
     * Upload new version of file with version comment
     *
     * @param uri
     * @param versionComment
     */
    void handleFileUpload(String uri, String versionComment);

}
