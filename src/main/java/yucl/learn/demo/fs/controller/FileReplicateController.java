package yucl.learn.demo.fs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yucl.learn.demo.fs.domain.FileInfo;
import yucl.learn.demo.fs.service.FileService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


/**
 * @author chunlei.yu
 */
@RestController
public class FileReplicateController {

    private static final Logger logger = LoggerFactory.getLogger(FileReplicateController.class);

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/inner/replicate", method = RequestMethod.POST)
    public ResponseEntity uploadSingleFile(@RequestParam(value = "fileId") String fileId,
                                           @RequestParam(value = "contentType") String contentType, @RequestParam(value = "fileName") String fileName,
                                           @RequestParam(value = "file") MultipartFile file)
            throws IOException, InterruptedException, ExecutionException {
        String owner = "";


        return new ResponseEntity<>(Collections.singletonMap("fileId", fileId), HttpStatus.OK);
    }

   /* @RequestMapping(value = "/inner/files/{fileId}", method = RequestMethod.GET)
    public void downloadFile(@PathVariable("fileId") String fileId, HttpServletResponse response) throws IOException {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        if (fileInfo != null) {
            try (final FileChannel inputChannel = FileChannel.open(),
                    StandardOpenOption.READ);
                 final WritableByteChannel outputChannel = Channels.newChannel(response.getOutputStream())) {
                response.setContentType("application/octet-stream");
                response.setContentLengthLong(inputChannel.size());
                response.setHeader("Content-Disposition", "attachment; filename="
                        + new String(fileInfo.getFileName().getBytes("UTF-8"), "ISO8859-1"));
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }*/

}
