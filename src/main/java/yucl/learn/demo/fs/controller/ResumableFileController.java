package yucl.learn.demo.fs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yucl.learn.demo.fs.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chunlei.yu
 */
@RestController
public class ResumableFileController {
    private static final Logger logger = LoggerFactory.getLogger(ResumableFileController.class);
    private static final Pattern contentRangePattern = Pattern.compile("bytes ([0-9]+)-([0-9]+)/([0-9]+)");

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/newFile", method = RequestMethod.POST)
    public ResponseEntity createNewFile(Principal principal,
                                        @RequestParam(value = "fileName", required = true) String fileName,
                                        @RequestParam(value = "fileLength", required = true) long fileLength,
                                        @RequestParam(value = "fileType", required = true) String contentType)
            throws IOException, InterruptedException, ExecutionException {
        String schema = "00";
        Map<String, String> fileExtAttrs = new HashMap<>();
        fileExtAttrs.put("Content-Type", contentType);
        fileExtAttrs.put("File-Name", fileName);
        String fileId = fileService.getNewFileId(schema, fileName, fileLength, fileExtAttrs);
        return new ResponseEntity<>(Collections.singletonMap("FileId", fileId), HttpStatus.OK);
    }

    @RequestMapping(value = "/uploadPartFile", method = RequestMethod.POST, headers = "content-type!=multipart/form-data")
    public ResponseEntity uploadChunked(final HttpServletRequest request, final HttpServletResponse response,
                                        @RequestHeader(value = "Content-Range", required = true) String contentRange,
                                        @RequestHeader(value = "Content-Length", required = true) long contentLength,
                                        @RequestHeader(value = "Content-Disposition", required = true) String contentDisposition) throws IOException {
        Matcher contentRangeMatcher = contentRangePattern.matcher(contentRange);
        String fileId = extractFileName(contentDisposition);
        if (contentRangeMatcher.find() && fileId != null) {
            long position = Long.parseLong(contentRangeMatcher.group(1));
            long fileSize = Long.parseLong(contentRangeMatcher.group(3));
            Map resultMap = fileService.resumableUploadHandle(fileId, request.getInputStream(), position, contentLength, fileSize);
            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/uploadPartFile", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public ResponseEntity uploadMultipart(Principal principal,
                                          @RequestHeader(value = "Content-Range", required = true) String contentRange,
                                          @RequestHeader(value = "Content-Disposition", required = true) String contentDisposition,
                                          @RequestParam(value = "file") MultipartFile multipartFile)
            throws IOException, InterruptedException, ExecutionException {
        Matcher contentRangeMatcher = contentRangePattern.matcher(contentRange);
        String fileId = extractFileName(contentDisposition);
        if (contentRangeMatcher.find() && fileId != null) {
            long position = Long.parseLong(contentRangeMatcher.group(1));
            long fileSize = Long.parseLong(contentRangeMatcher.group(3));
            Map resultMap = fileService.resumableUploadHandle(fileId, multipartFile.getInputStream(), position, multipartFile.getSize(),fileSize);
            return new ResponseEntity<>(resultMap, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private String extractFileName(String contentDisp) {
        String[] items = contentDisp.split(";");
        for (String s : items) {
            String token = s.trim();
            if (token.startsWith("filename=")) {
                return token.substring(token.indexOf("=") + 1);
            }
        }
        return null;
    }

}
