package com.townmc.mp;
 
import java.io.BufferedReader;  
import java.io.File;
import java.io.FileInputStream;  
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;  
import java.io.Reader;
import java.net.MalformedURLException;  
import java.net.URL;  
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;  
import java.security.KeyManagementException;
import java.security.KeyStore;  
  
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;  
import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.KeyManagerFactory;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.TrustManagerFactory;  

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
  
public class HTTPS {  
  
    /**
     * 发送请求
     * @param httpsUrl
     * @param xmlStr
     * @return
     */
    public static String post(String httpsUrl, String xmlStr) {  
        HttpsURLConnection urlCon = null;  
        String result = "";
        try {  
            urlCon = (HttpsURLConnection) (new URL(httpsUrl)).openConnection();  
            urlCon.setDoInput(true);  
            urlCon.setDoOutput(true);  
            urlCon.setRequestMethod("POST");  
            urlCon.setRequestProperty("Content-Length",  
                    String.valueOf(xmlStr.getBytes().length));  
            urlCon.setUseCaches(false);  
            //编码为utf-8
            urlCon.getOutputStream().write(xmlStr.getBytes("utf-8"));  
            urlCon.getOutputStream().flush();  
            urlCon.getOutputStream().close();  
            BufferedReader in = new BufferedReader(new InputStreamReader(  
                    urlCon.getInputStream()));  
            String line;  
            while ((line = in.readLine()) != null) {  
            	result += line;
            }  
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return result;
    }  
    
    public static String postWithCer(String httpsUrl, String xmlStr, String keyFile, String keyPassWord) throws Exception {  
    	KeyStore keyStore  = KeyStore.getInstance("PKCS12");
    	FileInputStream instream = new FileInputStream(new File(keyFile));//P12文件目录
//    	InputStream instream = HTTPS.class.getResourceAsStream("/apiclient_cert.p12");
    	try {
			keyStore.load(instream, keyPassWord.toCharArray());
		} finally {
            instream.close();
        }
    	SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, keyPassWord.toCharArray())//这里也是写密码的
                .build();
    	SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
        		SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {
		        HttpPost httpost = new HttpPost(httpsUrl); // 设置响应头信息
		    	httpost.addHeader("Connection", "keep-alive");
		    	httpost.addHeader("Accept", "*/*");
		    	httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    	httpost.addHeader("Host", "api.mch.weixin.qq.com");
		    	httpost.addHeader("X-Requested-With", "XMLHttpRequest");
		    	httpost.addHeader("Cache-Control", "max-age=0");
		    	httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
				httpost.setEntity(new StringEntity(xmlStr, "UTF-8"));
		        CloseableHttpResponse response = httpclient.execute(httpost);
		        
		        try {
		            HttpEntity entity = response.getEntity();
		            String jsonStr = toStringInfo(response.getEntity(),"UTF-8");
		            
		            //微信返回的报文时GBK，直接使用httpcore解析乱码
		          //  String jsonStr = EntityUtils.toString(response.getEntity(),"UTF-8");
		            EntityUtils.consume(entity);
		           return jsonStr;
		        } finally {
		            response.close();
		        }
        } finally {
            httpclient.close();
        }
        
    }
    
    private static String toStringInfo(HttpEntity entity, String defaultCharset) throws Exception, IOException{
		final InputStream instream = entity.getContent();
	    if (instream == null) {
	        return null;
	    }
	    try {
	        Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
	                "HTTP entity too large to be buffered in memory");
	        int i = (int)entity.getContentLength();
	        if (i < 0) {
	            i = 4096;
	        }
	        Charset charset = null;
	        
	        if (charset == null) {
	            charset = Charset.forName(defaultCharset);
	        }
	        if (charset == null) {
	            charset = org.apache.http.protocol.HTTP.DEF_CONTENT_CHARSET;
	        }
	        final Reader reader = new InputStreamReader(instream, charset);
	        final CharArrayBuffer buffer = new CharArrayBuffer(i);
	        final char[] tmp = new char[1024];
	        int l;
	        while((l = reader.read(tmp)) != -1) {
	            buffer.append(tmp, 0, l);
	        }
	        return buffer.toString();
	    } finally {
	        instream.close();
	    }
	}
    
    /**
     * HTTPS.GET请求
     * @param httpsUrl
     * @return
     */
    public static String get(String httpsUrl) {  
        HttpsURLConnection urlCon = null;  
        String result = "";
        try {  
            urlCon = (HttpsURLConnection) (new URL(httpsUrl)).openConnection();  
            urlCon.setDoInput(true);  
            urlCon.setDoOutput(true);  
            urlCon.setRequestMethod("GET");  
            urlCon.setUseCaches(false);  
            //编码为utf-8
            urlCon.getOutputStream().flush();  
            urlCon.getOutputStream().close();  
            BufferedReader in = new BufferedReader(new InputStreamReader(  
                    urlCon.getInputStream()));  
            String line;  
            while ((line = in.readLine()) != null) {  
            	result += line;
            }  
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return result;
    }  
    
    
    /**
     * HTTPS.GET请求
     * @param httpsUrl
     * @return
     */
    public static String get(String httpsUrl, Map<String, Object> params) {
        HttpsURLConnection urlCon = null;
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
                httpsUrl += httpsUrl.indexOf("?") > 0 ? "&" : "?";
                httpsUrl += sb.toString();
            }

            urlCon = (HttpsURLConnection) (new URL(httpsUrl)).openConnection();
            urlCon.setDoInput(true);
            urlCon.setDoOutput(true);
            urlCon.setRequestMethod("GET");
            urlCon.setUseCaches(false);
            //编码为utf-8
            urlCon.getOutputStream().flush();
            urlCon.getOutputStream().close();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlCon.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
  
}  