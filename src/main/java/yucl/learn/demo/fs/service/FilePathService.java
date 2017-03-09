package yucl.learn.demo.fs.service;

import java.nio.file.Path;

/**
 * Created by yuchunlei on 2017/3/9.
 */
public interface FilePathService {

    Path getFilePath(String fileId);

    Path getTempFilePath(String fileId);

    Path getTrackFilePath(String fileId);

}
