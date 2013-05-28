package edu.kaist.wst660.bmaingret.android;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HTTPUploader {
	private DefaultHttpClient mHttpClient;
	private String url;


	public HTTPUploader(String url) {
		this.url = url;
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		mHttpClient = new DefaultHttpClient(params);
	}


	public void uploadUserPhoto(File image) {
		try {

			HttpPost httppost = new HttpPost("some url");

			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
			multipartEntity.addPart("Title", new StringBody("Title"));
			multipartEntity.addPart("Image", new FileBody(image));
			httppost.setEntity(multipartEntity);

			mHttpClient.execute(httppost, new PhotoUploadResponseHandler());

		} catch (Exception e) {
			Log.e(HTTPUploader.class.getName(), e.getLocalizedMessage(), e);
		}
	}

	private class PhotoUploadResponseHandler implements ResponseHandler {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {

			HttpEntity r_entity = response.getEntity();
			String responseString = EntityUtils.toString(r_entity);
			Log.d("UPLOAD", responseString);

			return null;
		}

	}
}
