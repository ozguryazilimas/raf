/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Büyük dosya aktarımı sırasında toplanan chunkları saklar.
 * 
 * 
 * @author oyas
 */
public class ChunkStorage {
   
     private static final Logger log = LoggerFactory.getLogger(ChunkStorage.class);

    private Path basePath;

    public ChunkStorage() {
        //FIXME: Bunun için belki daha iyi bişi düşünmek lazım.
        basePath = Paths.get(ConfigResolver.getPropertyValue("raf.tempStorage", "/tmp/raf/"));
    }
    
    


    public void save(UploadRequest ur) throws RafException {

        /*
        if (ur.getFile().isEmpty()) {
            throw new StorageException(String.format("File with uuid = [%s] is empty", ur.getUuid().toString()));
        }*/

        
        Path targetFile;
        if (ur.isChunkRequest()) {
            targetFile = basePath.resolve(ur.getUuid()).resolve(String.format("%s_%05d", ur.getUuid(), ur.getPartIndex()));
        } else {
            targetFile = basePath.resolve(ur.getUuid()).resolve(ur.getFileName());
        }
        
        try {
            Files.createDirectories(targetFile.getParent());
            Files.copy(ur.getData().getInputStream(), targetFile);
        } catch (IOException e) {
            String errorMsg = String.format("Error occurred when saving file with uuid = [%s]", ur);
            log.error(errorMsg, e);
            //FIXME: burayı düzelt.
            throw new RafException(e);
        }
        

    }

    
    public void delete(String uuid) throws IOException {
        File targetDir = basePath.resolve(uuid).toFile();
        FileUtils.deleteDirectory(targetDir);
    }

    
    /**
     * FIXME: Burada aslında gene temp altında bir dosya da birleştirip, geriye input stream dönemli. Ve alan kişi işi bitince bu dosyayı da silmeli.
     * 
     * @param uuid
     * @param fileName
     * @param totalParts
     * @param totalFileSize
     * @throws RafException 
     */
    public InputStream mergeChunks(String uuid, String fileName, int totalParts, long totalFileSize) throws RafException, FileNotFoundException {
        
        File targetFile = basePath.resolve(uuid).resolve(fileName).toFile();
        try (FileChannel dest = new FileOutputStream(targetFile, true).getChannel()) {
            for (int i = 0; i < totalParts; i++) {
                File sourceFile = basePath.resolve(uuid).resolve(String.format("%s_%05d", uuid, i)).toFile();
                try (FileChannel src = new FileInputStream(sourceFile).getChannel()) {
                    dest.position(dest.size());
                    src.transferTo(0, src.size(), dest);
                }
                sourceFile.delete();
            }
        } catch (IOException e) {
            String errorMsg = String.format("Error occurred when merging chunks for uuid = [%s]", uuid);
            log.error(errorMsg, e);
            //FIXME: burayı düzelt
            throw new RafException(e);
        }
        
        return new FileInputStream( targetFile );
    }
    
}
