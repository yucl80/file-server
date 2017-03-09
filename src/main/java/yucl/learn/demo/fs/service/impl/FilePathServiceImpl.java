package yucl.learn.demo.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yucl.learn.demo.fs.cfg.AppProperties;
import yucl.learn.demo.fs.domain.FileId;
import yucl.learn.demo.fs.service.FilePathService;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;

/**
 * Created by yuchunlei on 2017/3/9.
 */
@Service
public class FilePathServiceImpl implements FilePathService {
    @Autowired
    private AppProperties appProperties;

    private FileSystem fileSystem = FileSystems.getDefault();


    private String extractDirPath(String fileId) {
        FileId _fileId = new FileId(fileId);
        SimpleDateFormat sdf = new SimpleDateFormat(appProperties.getFilePathPattern());
        return appProperties.getBaseFilePath() + "/" + _fileId.getschema() + "/" + sdf.format(_fileId.getCreatedTime());
    }

    @Override
    public Path getTempFilePath(String fileId) {
        String dirPath = extractDirPath(fileId);
        return fileSystem.getPath(dirPath + "/" + fileId + ".tmp");
    }

    @Override
    public Path getTrackFilePath(String fileId) {
        String dirPath = extractDirPath(fileId);
        return fileSystem.getPath(dirPath + "/" + fileId + ".track");
    }

    @Override
    public Path getFilePath(String fileId) {
        String dirPath = extractDirPath(fileId);
        return fileSystem.getPath(dirPath + "/" + fileId);
    }


}
