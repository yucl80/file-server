package yucl.learn.demo.fs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yucl.learn.demo.fs.cfg.AppProperties;
import yucl.learn.demo.fs.domain.FileId;

import java.text.SimpleDateFormat;

/**
 * Created by yuchunlei on 2017/3/8.
 */
@Service
public class FileUtil {
    @Autowired
    private AppProperties appProperties;

    public  String extractDirPath(FileId fileId) {
        SimpleDateFormat sdf = new SimpleDateFormat(appProperties.getFilePathPattern());
        return appProperties.getBaseFilePath() + "/" + fileId.getschema() + "/" + sdf.format(fileId.getCreatedTime());
    }

    public final String extractFilePath(String fileId) {
        FileId _fileId = new FileId(fileId);
        SimpleDateFormat sdf = new SimpleDateFormat(appProperties.getFilePathPattern());
        return appProperties.getBaseFilePath() + "/" + _fileId.getschema() + "/" + sdf.format(_fileId.getCreatedTime()) + "/" + fileId;
    }


}
