package yucl.learn.demo.fs.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yucl.learn.demo.fs.service.FileAttrService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author chunlei.yu
 */
@Service
public class FileAttrServiceImpl implements FileAttrService {
    private static final Logger logger = LoggerFactory.getLogger(FileAttrServiceImpl.class);

    public void setAttributes(String filePath, String attrName, String attrValue) throws IOException {
        Path _path = FileSystems.getDefault().getPath(filePath);
        setAttribute(_path, attrName, attrValue);
    }

    public void setAttribute(Path filePath, String attrName, String attrValue) throws IOException {
        UserDefinedFileAttributeView udfav = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        udfav.write(attrName, UTF_8.encode(attrValue));
    }

    public String getAttribute(String filePath, String attrName) throws IOException {
        Path _path = FileSystems.getDefault().getPath(filePath);
        return getAttribute(_path, attrName);
    }

    public String getAttribute(Path filePath, String attrName) throws IOException {
        UserDefinedFileAttributeView udfav = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        return getAttribute(udfav, attrName);
    }

    @Override
    public Map<String, String> getFileExtAttrs(Path filePath) throws IOException {
        Map<String, String> fileExtAttrs = new HashMap<>();
        try {
            UserDefinedFileAttributeView udfav = Files.getFileAttributeView(filePath,
                    UserDefinedFileAttributeView.class);
            List<String> keys = udfav.list();
            for (String key : keys) {
                fileExtAttrs.put(key, getAttribute(udfav, key));
            }
            return fileExtAttrs;
        } catch (NoSuchFileException e) {
            logger.error("getFileExtAttr file:" + filePath + " failed", e);
            return null;
        }
    }


    public void setFileExtAttrs(Path filePath, Map<String, String> fileExtAttrs) throws IOException {
        UserDefinedFileAttributeView udfav = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        for (Map.Entry<String, String> entity : fileExtAttrs.entrySet()) {
            if(entity.getValue() != null) {
                udfav.write(entity.getKey(), UTF_8.encode(entity.getValue()));
            }else {
                logger.error(filePath.toString()+" : "+entity.getKey() +" is null");
            }
        }
    }


    public String getAttribute(UserDefinedFileAttributeView udfav, String attrName) throws IOException {
        int size = udfav.size(attrName);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
        udfav.read(attrName, byteBuffer);
        byteBuffer.flip();
        return UTF_8.decode(byteBuffer).toString();

    }


}
