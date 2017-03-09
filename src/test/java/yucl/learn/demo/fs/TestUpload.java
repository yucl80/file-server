package yucl.learn.demo.fs;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

/**
 * @author chunlei.yu
 *
 */
public class TestUpload {

	public static void main(String[] args) throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httppost = new HttpPost("http://192.168.16.152:18080/im-web-server/files");
			// HttpPost httppost = new HttpPost("http://localhost:8090/files");
			FileBody bin = new FileBody(new File("d:/tmp/ss.jar"));

			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("source", new StringBody("xx", ContentType.TEXT_PLAIN))
					.addPart("contentType", new StringBody("image/png", ContentType.TEXT_PLAIN))
					.addPart("fileName", new StringBody("gg.png", ContentType.TEXT_PLAIN))
					.addPart("receiverType", new StringBody("1", ContentType.TEXT_PLAIN))
					.addPart("receiverId", new StringBody("1", ContentType.TEXT_PLAIN)).addPart("file", bin).build();
			httppost.addHeader("Authorization", "564ada7223eae36b2dd552cc");
			httppost.setEntity(reqEntity);

			System.out.println("executing request " + httppost.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					System.out.println("Response content length: " + resEntity.getContentLength());
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public static  void test1(){
		 /*HttpPost httpPost = new HttpPost(peerNodeUri)
			FileBody file = new FileBody(new File(fileInfo.getPath()),
					ContentType.create(fileInfo.getContentType()), fileInfo.getFileName());
			ContentType textContentType = ContentType.create("text/plain", Consts.UTF_8);
			HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", file)
					.addPart("fileId", new StringBody(fileInfo.getId(), textContentType))
					.addPart("contentType", new StringBody(fileInfo.getContentType(), textContentType))
					.addPart("fileName", new StringBody(fileInfo.getFileName(), textContentType)).build();
			httppost.setEntity(reqEntity);

			try(CloseableHttpResponse response = httpclient.execute(httppost)){
				if (response.getStatusLine().getStatusCode() != 200) {
					log.error("replicate file :" + fileInfo.getId() + " to " + peerNodeUri + " failed",
							new RuntimeException(response.getStatusLine().toString()));
				}
				HttpEntity resEntity = response.getEntity();
				EntityUtils.consume(resEntity);
			}*/
	}
}
