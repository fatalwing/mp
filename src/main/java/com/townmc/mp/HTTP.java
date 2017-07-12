package com.townmc.mp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

class HTTP {
	private static final Log log = LogFactory.getLog(HTTP.class);

	public static InputStream getStream(String url, Map<String, Object> params) {
		System.setProperty ("jsse.enableSNIExtension", "false");
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			StringBuilder sb = new StringBuilder();
			if(null != params) {
				for(Map.Entry<String, Object> entry : params.entrySet()) {
					if(sb.length() > 0) {
						sb.append("&");
					}
					sb.append(entry.getKey()).append("=").append(entry.getValue());
				}
			}
			
			if(sb.length() > 0) {
				url += url.indexOf("?") > 0 ? "&" : "?";
				url += sb.toString();
			}
            HttpGet httpget = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
            httpget.setConfig(requestConfig);

            ResponseHandler<InputStream> responseHandler = new ResponseHandler<InputStream>() {

                public InputStream handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        InputStream in = entity.getContent();
                        return in;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            return httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		return null;
		
	}
	
	public static String get(String url, Map<String, Object> params) {
		System.setProperty ("jsse.enableSNIExtension", "false");
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = "";
		try {
			StringBuilder sb = new StringBuilder();
			if(null != params) {
				for(Map.Entry<String, Object> entry : params.entrySet()) {
					if(sb.length() > 0) {
						sb.append("&");
					}
					sb.append(entry.getKey()).append("=").append(entry.getValue());
				}
			}
			
			if(sb.length() > 0) {
				url += url.indexOf("?") > 0 ? "&" : "?";
				url += sb.toString();
			}
            HttpGet httpget = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
            httpget.setConfig(requestConfig);

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity, Charset.forName("UTF-8")) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            result = httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		log.debug(url + " response content: " + result);
		return result;
	}
	
	public static String post(String url, String postContent) {
		System.setProperty ("jsse.enableSNIExtension", "false");
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = "";
		try {
			StringEntity entity = new StringEntity(postContent,
			        ContentType.create("plain/text", Consts.UTF_8));
			entity.setChunked(true);
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(entity);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000).build();
			httppost.setConfig(requestConfig);
			
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity, Charset.forName("UTF-8")) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            
            result = httpclient.execute(httppost, responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static String uploadFile(String url, File file) {
		String re = null;
		HttpClient httpclient = new DefaultHttpClient();  
        HttpPost post = new HttpPost(url);  
        FileBody fileBody = new FileBody(file);  
        MultipartEntity entity = new MultipartEntity();  
        entity.addPart("file", fileBody);  
        post.setEntity(entity);  
        HttpResponse response;
		try {
			response = httpclient.execute(post);
			if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()){    
				
				HttpEntity entitys = response.getEntity();  
				if (entity != null) {  
					re = (EntityUtils.toString(entitys));  
				}  
			}  
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
        httpclient.getConnectionManager().shutdown();  
		return re;
	}
	
}
