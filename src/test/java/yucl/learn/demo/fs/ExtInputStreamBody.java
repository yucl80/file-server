package yucl.learn.demo.fs;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author chunlei.yu
 *
 */
public class ExtInputStreamBody extends InputStreamBody {
	private long len;

	public ExtInputStreamBody(InputStream in, ContentType contentType, String filename, long len) {
		super(in, contentType, filename);
		this.len = len;
	}

	public ExtInputStreamBody(InputStream in, String filename, long len) {
		super(in, filename);
		this.len = len;

	}

	/**
	 * @param in
	 * @param contentType
	 */
	public ExtInputStreamBody(InputStream in, ContentType contentType) {
		super(in, contentType);

	}

	@Override
	public InputStream getInputStream() {
		return super.getInputStream();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
	}

	@Override
	public String getTransferEncoding() {
		return super.getTransferEncoding();
	}

	@Override
	public long getContentLength() {
		return this.len;
	}

	@Override
	public String getFilename() {
		return super.getFilename();
	}

}
