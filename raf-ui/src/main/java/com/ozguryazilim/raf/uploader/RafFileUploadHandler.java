package com.ozguryazilim.raf.uploader;

import com.ozguryazilim.telve.uploader.ui.FileUploadHandler;

public interface RafFileUploadHandler extends FileUploadHandler {

    /**
     * Upload with check zip files (if decompress true, zip is automatically decompress and extract same directory)
     *
     * @param uri
     * @param decompress
     */
    void handleFileUpload(String uri, boolean decompress);

}
