package yucl.learn.demo.fs.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yucl80@163.com
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String maxFileSize;
    private String maxRequestSize;
    private String baseFilePath;
    private String peerNodeUri;
    private String filePathPattern;


    private String clusterId;

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public String getBaseFilePath() {
        return baseFilePath;
    }

    public void setBaseFilePath(String filePath) {
        this.baseFilePath = filePath;
    }

    public String getPeerNodeUri() {
        return peerNodeUri;
    }

    public void setPeerNodeUri(String peerNode) {
        this.peerNodeUri = peerNode;
    }

    public String getFilePathPattern() {
        return filePathPattern;
    }

    public void setFilePathPattern(String filePathPattern) {
        this.filePathPattern = filePathPattern;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

}
