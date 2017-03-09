package yucl.learn.demo.fs.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


/**
 * @author chunlei.yu
 *
 */
public interface FileAttrService {

	Map<String,String> getFileExtAttrs(Path filePath) throws IOException;

	void setFileExtAttrs(Path filePath, Map<String,String> fileExtAttrs) throws IOException;

}