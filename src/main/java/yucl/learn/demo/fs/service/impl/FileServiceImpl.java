package yucl.learn.demo.fs.service.impl;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import yucl.learn.demo.fs.domain.FileId;
import yucl.learn.demo.fs.domain.FileInfo;
import yucl.learn.demo.fs.service.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * @author chunlei.yu
 */
@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    FilePathService filePathService;

    @Autowired
    private FileAttrService fileAttrService;

    @Autowired
    private ReplicateService replicateService;

    @Override
    public String getNewFileId(String schema, String fileName, long fileLength, Map<String, String> fileExtAttrs) throws IOException {
        String fileId = FileId.get(replicateService.getClusterId(), schema).getId();
        File dir = filePathService.getFilePath(fileId).getParent().toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path tempFilePath = filePathService.getTempFilePath(fileId);
        File file = tempFilePath.toFile();
        if (!file.exists()) {
            RandomAccessFile targetFile = new RandomAccessFile(file, "rw");
            targetFile.setLength(fileLength);
            targetFile.close();
            fileAttrService.setFileExtAttrs(tempFilePath, fileExtAttrs);
        }
        File trackFile = filePathService.getTrackFilePath(fileId).toFile();
        if (!trackFile.exists()) {
            trackFile.createNewFile();
        }
        return fileId;
    }


    public Map<String, String> resumableUploadHandle(String fileId, InputStream inputStream, long position, long count) throws IOException {
        Path tempFilePath = filePathService.getTempFilePath(fileId);
        File trackFile = filePathService.getTrackFilePath(fileId).toFile();
        Map<String, String> resultMap = new HashMap<>();
        // MessageDigest messageDigest = getMessageDigest("SHA-1");
        try (final HashingInputStream hashingInputStream = new HashingInputStream(Hashing.md5(), inputStream);
             //final DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
             final ReadableByteChannel inputChannel = Channels.newChannel(hashingInputStream);
             final FileChannel outputChannel = FileChannel.open(tempFilePath, StandardOpenOption.WRITE)) {
            long size = outputChannel.transferFrom(inputChannel, position, count);
            try (FileOutputStream fileOutputStream = new FileOutputStream(trackFile, true)) {
                fileOutputStream.write(new StringBuilder().append(String.valueOf(position)).append(" ").append(String.valueOf(position + size - 1)).append("\n").toString().getBytes(StandardCharsets.UTF_8));
                fileOutputStream.flush();
            }
            resultMap.put("ContentRange", position + "-" + size);
            resultMap.put("ContentHash", "md5:" + hashingInputStream.hash().toString());
        }
        if (isUploadComplete(tempFilePath.toFile(), trackFile)) {
            tempFilePath.toFile().renameTo(filePathService.getFilePath(fileId).toFile());
            trackFile.delete();
            doUploadComplete(fileId);
        }
        return resultMap;
    }


    @Override
    public void updateFileAttrs(String fileId, Map<String, String> attrs) {
        Path filePath = filePathService.getFilePath(fileId);
        try {
            if (filePath.toFile().exists()) {
                fileAttrService.setFileExtAttrs(filePath, attrs);
            } else {
                filePath = filePathService.getTempFilePath(fileId);
                fileAttrService.setFileExtAttrs(filePath, attrs);
            }
        } catch (IOException e) {
            logger.error("Update File Attrs Failed " + attrs.toString(), e);
        }

    }


    private boolean isUploadComplete(File file, File trackFile) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(trackFile))) {
            String line = null;
            List<TrackRecord> trackList = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(" ");
                if (data.length == 2) {
                    trackList.add(new TrackRecord(Long.parseLong(data[0]), Long.parseLong(data[1])));
                }
            }
            trackList.sort(new Comparator<TrackRecord>() {
                @Override
                public int compare(TrackRecord o1, TrackRecord o2) {
                    if (o1.begin > o2.begin)
                        return 1;
                    if (o1.begin < o2.begin)
                        return -1;
                    return 0;
                }
            });
            if (trackList.get(0).begin != 0) {
                return false;
            }
            if (trackList.get(trackList.size() - 1).end + 1 != file.length()) {
                return false;
            }
            for (int i = 0; i < trackList.size() - 1; i++) {
                if (trackList.get(i).end + 1 != trackList.get(i + 1).begin) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public Map<String, String> uploadHandle(String schema, InputStream inputStream, long fileLength, Map<String, String> fileExtAttrs) throws IOException {
        String fileId = FileId.get(replicateService.getClusterId(), schema).toString();
        File dir = filePathService.getFilePath(fileId).getParent().toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("FileId", fileId);
        Path tempFilePath = filePathService.getTempFilePath(fileId);
        try (final HashingInputStream hashingInputStream = new HashingInputStream(Hashing.md5(), inputStream);
             final ReadableByteChannel inputChannel = Channels.newChannel(hashingInputStream);
             final FileChannel outputChannel = FileChannel.open(tempFilePath, StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE)) {
            outputChannel.lock();
            outputChannel.transferFrom(inputChannel, 0, fileLength);
            resultMap.put("ContentHash", "md5:" + hashingInputStream.hash().toString());
        }
        fileAttrService.setFileExtAttrs(tempFilePath, fileExtAttrs);
        tempFilePath.toFile().renameTo(filePathService.getFilePath(fileId).toFile());
        doUploadComplete(fileId);
        return resultMap;
    }

    @Override
    public Map<String, String> uploadHandle(String schema, MultipartFile multipartFile, Map<String, String> fileExtAttrs) throws IOException {
        String fileId = FileId.get(replicateService.getClusterId(), schema).toString();
        File dir = filePathService.getFilePath(fileId).getParent().toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("FileId", fileId);
        Path filePath = filePathService.getFilePath(fileId);
        File  file = filePath.toFile();
        multipartFile.transferTo(file);
        HashCode md5 = Files.hash(file, Hashing.md5());
        fileAttrService.setFileExtAttrs(filePath, fileExtAttrs);
        resultMap.put("ContentHash", "md5:" + md5.toString());
        doUploadComplete(fileId);
        return resultMap;
    }


    @Override
    public String getFileUploadProgress(String fileId)  {
        try {
            File trackFile = filePathService.getTrackFilePath(fileId).toFile();
            List<TrackRecord> trackList = new ArrayList<>();
            if (trackFile.exists()) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(trackFile))) {
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] data = line.split(" ");
                        if (data.length == 2) {
                            trackList.add(new TrackRecord(Long.parseLong(data[0]), Long.parseLong(data[1])));
                        }
                    }
                }
                return null;
            }
            File file = filePathService.getFilePath(fileId).toFile();
            if (file.exists()) {
                trackList.add(new TrackRecord(0, file.length() - 1));
            }
        }catch (IOException e){
            logger.error("Get File Upload Progress Failed "+fileId,e);
        }

        return null;
    }

    private MessageDigest getMessageDigest(String algorithm) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return messageDigest;
    }


    private void doUploadComplete(String fileId) {
        replicateService.replicate(fileId);
    }

    public FileInfo getFileInfo(String fileId) {
        Path filePath = filePathService.getFilePath(fileId);
        /*if (Files.notExists(filePath, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})*/
        if (!filePath.toFile().exists()) {
            replicateService.replicateFileFromPeer(fileId);
        }
        if (filePath.toFile().exists()) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(fileId);
            try {
                fileInfo.setFileExtAttrs(fileAttrService.getFileExtAttrs(filePath));
                return fileInfo;
            } catch (IOException e) {
                logger.error("Get File Info: " + fileId, e);
            }
        }
        return null;
    }




}

class TrackRecord {
    public long begin;

    public long end;

    public TrackRecord(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }


}