package yucl.learn.demo.fs.service.impl;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yucl.learn.demo.fs.cfg.AppProperties;
import yucl.learn.demo.fs.service.FileAttrService;
import yucl.learn.demo.fs.service.FilePathService;
import yucl.learn.demo.fs.service.ReplicateService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * @author chunlei.yu
 */
@Service
public class ReplicateServiceImpl implements ReplicateService {
    private static final Logger logger = LoggerFactory.getLogger(ReplicateServiceImpl.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private FileAttrService fileAttrService;

    @Autowired
    private FilePathService filePathService;


    @Override
    public void replicate(String fileId) {
        if (appProperties.getPeerNodeUri() != null) {
            logger.debug("start sync file :" + fileId);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    asynReplicateFileFromPeer(fileId);
                }
            });

        }
    }

    public void replicateFileFromPeer(String fileId) {
        String peerNodeUri = getPeerNodeUri(fileId);
        try {
            HttpGet httpGet = new HttpGet(peerNodeUri);
            try (CloseableHttpClient httpclient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpclient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    logger.error("replicate file :" + fileId + " to " + peerNodeUri + " failed",
                            new RuntimeException(response.getStatusLine().toString()));
                }
                handleResponse(fileId, response);
            }

        } catch (IOException e) {
            logger.error("replicate file :" + fileId + " from " + peerNodeUri + " failed", e);
        }

    }


    public void asynReplicateFileFromPeer(String fileId) {
        String peerNodeUri = getPeerNodeUri(fileId);
        try {
            try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault()) {
                httpclient.start();
                HttpGet httpGet = new HttpGet(peerNodeUri);
                Future<HttpResponse> future = httpclient.execute(httpGet, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(HttpResponse result) {
                        handleResponse(fileId, result);
                    }

                    @Override
                    public void failed(Exception ex) {
                        logger.error("sync file " + fileId + " faild", ex);
                    }

                    @Override
                    public void cancelled() {
                        logger.error("sync file " + fileId + " cancelled");
                    }
                });
            }
        } catch (IOException e) {
            logger.error("downloadFileFromPeerNode failed", e);
        }
    }

    private void handleResponse(String fileId, HttpResponse response) {
        try {
            Header[] headers = response.getAllHeaders();
            Map<String, String> fileExtAttrs = new HashMap<>();
            for (Header header : headers) {
                if (header.getName().startsWith("X-")) {
                    fileExtAttrs.put(header.getName().substring(2), header.getValue());
                }
            }
            long fileLength = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
            writeFile(fileId, response.getEntity().getContent(), fileLength, fileExtAttrs);
        } catch (IOException e) {
            logger.error("" + fileId, e);
        }
    }

    public Map<String, String> writeFile(String fileId, InputStream inputStream, long fileLength, Map<String, String> fileExtAttrs) throws IOException {
        File dir = filePathService.getFilePath(fileId).getParent().toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Map<String, String> resultMap = new HashMap<>();
        Path tempFilePath = filePathService.getTempFilePath(fileId);
        try (final HashingInputStream hashingInputStream = new HashingInputStream(Hashing.md5(), inputStream);
             final ReadableByteChannel inputChannel = Channels.newChannel(hashingInputStream);
             final FileChannel outputChannel = FileChannel.open(tempFilePath, StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE)) {
            outputChannel.lock();
            outputChannel.transferFrom(inputChannel, 0, fileLength);
            fileExtAttrs.put("Content-Hash", "md5 " + hashingInputStream.hash().toString());
            fileAttrService.setFileExtAttrs(tempFilePath, fileExtAttrs);
            outputChannel.force(true);
        }
        tempFilePath.toFile().renameTo(filePathService.getFilePath(fileId).toFile());
        return resultMap;
    }

    public String getClusterId() {
        return appProperties.getClusterId();
    }

    private String getPeerNodeUri(String fileId) {
        return appProperties.getPeerNodeUri() + "/inner/replicate" + "/" + fileId;
    }

}
