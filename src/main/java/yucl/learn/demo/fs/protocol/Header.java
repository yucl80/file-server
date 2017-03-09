package yucl.learn.demo.fs.protocol;

/**
 * Created by yuchunlei on 2017/3/7.
 */
public class Header {
    /**
     * attachment, filename="name of the file being uploaded"
     */
   public static final String  CONTENT_DISPOSITION="Content-Disposition";

    /**
     * mime type of a file being uploaded (must not be multipart/form-data);
     */
    public static final String CONTENT_TYPE=" Content-Type";

    /**
     * byte range of a segment being uploaded;
     */
    public static final String CONTENT_RANGE= "Content-Range";

    /**
     * 	identifier of a session of a file being uploaded ;
     * 	In order to identify requests containing segments of a file, a user agent sends a unique session identified in headers X-Session-ID
     * 	or Session-ID. User agent is responsible for making session identifiers unique
     */
    public static final String  SESSION_ID = "Session-ID";



}
