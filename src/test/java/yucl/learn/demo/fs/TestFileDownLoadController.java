package yucl.learn.demo.fs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import yucl.learn.demo.fs.controller.MultipartFileSender;
import yucl.learn.demo.fs.domain.FileInfo;
import yucl.learn.demo.fs.service.FilePathService;
import yucl.learn.demo.fs.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by yuchunlei on 2017/3/9.
 */
@RestController
public class TestFileDownLoadController {
    @Autowired
    private FileService fileService;

    @Autowired
    private FilePathService filePathService;

    @RequestMapping(value = "/files2/{fileId}", method = RequestMethod.GET)
    public FileSystemResource downloadFile2(@PathVariable("fileId") String fileId, HttpServletResponse response,
                                            @RequestHeader(value = "Range", required = false) String range) throws IOException {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        Path filePath = filePathService.getFilePath(fileId);
        response.setContentType(fileInfo.getContentType());
        response.setContentLengthLong(filePath.toFile().length());
        response.addHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "attachment; filename="
                + new String(fileInfo.getFileName().getBytes("UTF-8"), "ISO8859-1"));
        return new FileSystemResource(new File(filePath.toFile().getPath()));
    }

    @RequestMapping(value = "/files3/{fileId}", method = RequestMethod.GET)
    public void downloadFile3(@PathVariable("fileId") String fileId, HttpServletRequest request , HttpServletResponse response,
                              @RequestHeader(value = "Range", required = false) String range) throws IOException {
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        Path filePath = filePathService.getFilePath(fileId);
        response.setContentType(fileInfo.getContentType());
        response.setContentLengthLong(filePath.toFile().length());
        response.addHeader("Accept-Ranges", "bytes");
        try {
            new MultipartFileSender().with(request).with(response).serveResource();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
