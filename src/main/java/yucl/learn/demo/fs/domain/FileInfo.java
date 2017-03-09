package yucl.learn.demo.fs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chunlei.yu
 */
public class FileInfo {
    public static final String FILE_NAME = "fileName";
    public static final String CONTENT_TYPE = "contentType";
    public static final String FILE_OWNER = "owner";

    private String id;
    private Map<String, String> fileExtAttrs = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map getFileExtAttrs() {
        return fileExtAttrs;
    }

    @JsonIgnore
    public String getContentType() {
        return fileExtAttrs.get("Content-Type");
    }

    public void setFileExtAttrs(Map fileExtAttrs) {
        this.fileExtAttrs = fileExtAttrs;
    }

    public FileInfo(String id,  Map<String, String> fileExtAttrs) {
        super();
        this.id = id;
        this.fileExtAttrs = fileExtAttrs;
    }

    public FileInfo() {
        super();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileInfo [id=").append(id).append(", fileExtAttrs=").append(fileExtAttrs).append("]");
        return builder.toString();
    }

    @JsonIgnore
    public String getFileName() {
        return fileExtAttrs.get("File-Name");
    }
}
