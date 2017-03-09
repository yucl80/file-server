package yucl.learn.demo.fs.service;

import yucl.learn.demo.fs.domain.FileInfo;
import yucl.learn.demo.fs.domain.TrackRecord;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author chunlei.yu
 *
 */
public interface FileService {
	String getNewFileId(String schema, String fileName, long fileLength,  Map<String, String> fileExtAttrs) throws IOException;

    Map<String,String> resumableUploadHandle(String fileId, InputStream inputStream, long position, long count) throws IOException;

    Map<String,String>  uploadHandle(String schema, InputStream inputStream,long fileLength, Map<String,String> fileExtAttrs) throws IOException;

    void updateFileAttrs(String fileId,Map<String,String> fileExtAttrs);

    FileInfo getFileInfo(String filedId);

    String getFileUploadProgress(String fileId) ;
}