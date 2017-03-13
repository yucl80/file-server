package yucl.learn.demo.fs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yucl.learn.demo.fs.domain.FileInfo;
import yucl.learn.demo.fs.service.FilePathService;
import yucl.learn.demo.fs.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


/**
 * @author chunlei.yu
 */
@RestController
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final Pattern contentRangePattern = Pattern.compile("bytes ([0-9]+)-([0-9]+)/([0-9]+)");

    @Autowired
    private FileService fileService;

    @Autowired
    private FilePathService filePathService;


    @RequestMapping(value = "/files", method = RequestMethod.POST, headers = "content-type!=multipart/form-data")
    public ResponseEntity uploadChunked(Principal principal, final HttpServletRequest request, final HttpServletResponse response,
                                        @RequestHeader(value = "Content-Disposition", required = true) String contentDisposition,
                                        @RequestHeader(value = "Content-Type", required = true) String contentType,
                                        @RequestHeader(value = "Content-Length", required = true) long contentLength) throws IOException {
        String schema = "00";
        Map<String, String> fileExtAttrs = new HashMap<>();
        fileExtAttrs.put("Content-Type", contentType);
        fileExtAttrs.put("File-Name", extractFileNameFromContentDisposition(contentDisposition));
        Map resultMap = fileService.uploadHandle(schema, request.getInputStream(), contentLength, fileExtAttrs);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }


    @RequestMapping(value = "/files", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public ResponseEntity uploadMultipart(Principal principal,
                                          @RequestParam(value = "file") MultipartFile multipartFile)
            throws IOException, InterruptedException, ExecutionException {
        String schema = "00";
        Map<String, String> fileExtAttrs = new HashMap<>();
        fileExtAttrs.put("Content-Type", multipartFile.getContentType());
        fileExtAttrs.put("File-Name", extractFileNameFromMultipartFile(multipartFile));
        Map resultMap = fileService.uploadHandle(schema, multipartFile, fileExtAttrs);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);

    }

    @RequestMapping(value = "/multfiles", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public ResponseEntity uploadMultiFile(Principal principal,
                                          @RequestParam(value = "file") MultipartFile[] multipartFiles)
            throws IOException, InterruptedException, ExecutionException {
        String schema = "00";
        List<Map<String, String>> resultMapList = new ArrayList<>();
        for (int i = 0; i < multipartFiles.length; i++) {
            MultipartFile multipartFile = multipartFiles[i];
            Map<String, String> fileExtAttrs = new HashMap<>();
            fileExtAttrs.put("Content-Type", multipartFile.getContentType());
            fileExtAttrs.put("File-Name", extractFileNameFromMultipartFile(multipartFile));
            Map<String, String> resultMap = fileService.uploadHandle(schema, multipartFile, fileExtAttrs);
            resultMapList.add(resultMap);
        }
        return new ResponseEntity<>(resultMapList, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/files/{fileId}", method = RequestMethod.GET)
    public void downloadFile(Principal principal,@PathVariable("fileId") String fileId, HttpServletResponse response,
                             @RequestHeader(value = "Range", required = false) String range) throws IOException {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        if (fileInfo != null) {
            try (final FileChannel inputChannel = FileChannel.open(filePathService.getFilePath(fileId), StandardOpenOption.READ);
                 final WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {
                // response.setContentType("application/octet-stream");
                //response.addHeader("Accept-Ranges", "bytes");
                response.setContentType(fileInfo.getContentType());
                response.setContentLengthLong(inputChannel.size());
                response.setHeader("Content-Disposition", "attachment; filename="
                        + new String(fileInfo.getFileName().getBytes("UTF-8"), "ISO8859-1"));
                Map<String, String> fileExtAttrs = fileInfo.getFileExtAttrs();
                for (Map.Entry<String, String> entry : fileExtAttrs.entrySet()) {
                    response.addHeader("X-" + entry.getKey(), entry.getValue());
                }
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/files/{fileId}/info", method = RequestMethod.GET)
    public ResponseEntity getFileInfo(Principal principal,@PathVariable("fileId") String fileId){
        return new ResponseEntity<>(fileService.getFileInfo(fileId), HttpStatus.OK);
    }

    @RequestMapping(value = "/files/{fileId}/stat", method = RequestMethod.GET)
    public ResponseEntity getFileUploadProgress(Principal principal,@PathVariable("fileId") String fileId){
        return new ResponseEntity<>(fileService.getFileUploadProgress(fileId), HttpStatus.OK);
    }

    private String extractFileNameFromMultipartFile(MultipartFile multipartFile) {
        String fn = multipartFile.getOriginalFilename();
        int idx = fn.lastIndexOf("/");
        if (idx != -1) {
            return fn.substring(idx + 1);
        }
        return fn;
    }

    private String extractFileNameFromContentDisposition(String contentDisposition) {
        String[] items = contentDisposition.split(";");
        for (String s : items) {
            String token = s.trim();
            if (token.startsWith("filename=")) {
                return token.substring(token.indexOf("=") + 1);
            }
        }
        return null;
    }

}
