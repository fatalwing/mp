package com.townmc.mp.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.townmc.mp.json.JSONObject;
import com.townmc.mp.model.Unifiedorder;
import com.townmc.mp.wxpay.common.XMLParser;

public class MpUtils {

	/**
	 * md5加密
	 * @param text
	 * @return
	 */
	public static String getMD5(String text) {
        String result = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buf = text.getBytes();
            byte[] dig = md5.digest(buf);
            String hex = null;
            for (int i = 0; i < dig.length; i++) {
                int n = dig[i] < 0 ? (256 + dig[i]) : dig[i];
                hex = Integer.toHexString(n);
                if (hex.length() < 2)
                    hex = "0" + hex;
                result += hex;
            }
        } catch (NoSuchAlgorithmException e) {
            result = null;
        }
        return result;
    }
	
	/**
	 * 获取微信支付随机数
	 * @return
	 */
	public static String getNonceStr(){
		String random = UUID.randomUUID().toString();
		random = random.replaceAll("-", "");
		return random.toUpperCase();
	}
	
	/**
	 * 微信支付签名
	 * @param map
	 * @param signStr
	 * @param key
	 * @return
	 */
	public static String getSign(Map<String, Object> map, String[] signStr, String key){
		String[] unNullStr = new String[map.size()];
		for(int i=0;i<map.size();i++){
			unNullStr[i] = signStr[i];
		}
		Arrays.sort(unNullStr);
		StringBuffer tempA = new StringBuffer();
		for(int i=0;i<unNullStr.length;i++){
			tempA.append(unNullStr[i]).append("=").append(map.get(unNullStr[i])).append("&");
		}
		if(key!=null&&!"".equals(key)){
			tempA.append("key=").append(key);
		}else{
			tempA.delete(tempA.length()-1, tempA.length());
		}
		System.out.println("tempA:"+tempA);
		return getMD5(tempA.toString()).toUpperCase();
	}
	
	public static String getSha1Sign(Map<String, Object> map, String[] signStr, String key){
		String[] unNullStr = new String[map.size()];
		for(int i=0;i<map.size();i++){
			unNullStr[i] = signStr[i];
		}
		Arrays.sort(unNullStr);
		StringBuffer tempA = new StringBuffer();
		for(int i=0;i<unNullStr.length;i++){
			tempA.append(unNullStr[i]).append("=").append(map.get(unNullStr[i])).append("&");
		}
		if(key!=null&&!"".equals(key)){
			tempA.append("key=").append(key);
		}else{
			tempA.delete(tempA.length()-1, tempA.length());
		}
		System.out.println("tempA:"+tempA);
		return getSha1(tempA.toString());
	}

	public static String getSha1(String content){
		MessageDigest md = null;
        String tmpStr = null;

        try
        {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return tmpStr;
	}
	
	/**
     * 将字节数组转换为十六进制字符串
     * @param digest
     * @return
     */
    private static String byteToStr(byte[] digest)
    {
        // TODO Auto-generated method stub
        String strDigest = "";
        for (int i = 0; i < digest.length; i++)
        {
            strDigest += byteToHexStr(digest[i]);
        }
        return strDigest;
    }
	 /**
     * 将字节转换为十六进制字符串
     * @param b
     * @return
     */
    private static String byteToHexStr(byte b)
    {
        // TODO Auto-generated method stub
        char[] Digit =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
                'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(b >>> 4) & 0X0F];
        tempArr[1] = Digit[b & 0X0F];

        String s = new String(tempArr);
        return s;
    }
	
	/**
	 * 微信支付post的xml
	 * @param <T>
	 * @param unifiedorder
	 * @param key
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static <T> String getPayXmlStr(T unifiedorder, String key) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		StringBuffer orderStr = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] field = unifiedorder.getClass().getDeclaredFields();
		String[] signStr = new String[field.length];
		int i = 0;
		//构造xml format
		orderStr.append("<xml>");
		for(int j=0; j<field.length; j++){
			String name = field[j].getName();
			String upName = name.substring(0,1).toUpperCase()+name.substring(1); //首字母大写，构造get/set函数
			String type = field[j].getGenericType().toString();
			if(type.equals("class java.lang.String")){
				Method m = unifiedorder.getClass().getMethod("get"+upName);
				String value = (String)m.invoke(unifiedorder);
				if(value!=null && !"".equals(value)){
					orderStr.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
					map.put(name, value);
					signStr[i] = name; i++;
				}
			}else if(type.equals("class java.lang.Integer")){
				Method m = unifiedorder.getClass().getMethod("get"+upName);
				Integer value = (Integer)m.invoke(unifiedorder);
				if(value!=null && !"".equals(value)){
					orderStr.append("<").append(name).append(">").append(value).append("</").append(name).append(">");
					map.put(name, value);
					signStr[i] = name; i++;
				}
			}
		}
		String sign = getSign(map,signStr,key);
		orderStr.append("<sign>").append(sign).append("</sign>");
		orderStr.append("</xml>");
	
		return orderStr.toString();
	}
	
	public static Map<String, Object> parse(String protocolXML) { 
		
        try {
			return XMLParser.getMapFromXML(protocolXML);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
        return null;
    }

	public static void main(String[] args){
		String aa = "jsapi_ticket=sM4AOVdWfPE4DxkXGEs8VD5s2KHl9B1a85vBaiPeXCWIfc5N-L8nAg32VCaeI592KG7eHbqpK0oEqmiPiVdHUQ&noncestr=DF12B17B33054493911887BE53453139&timestamp=1446620696&url=http://wx.qulianjie.net/card/card.html?merchantId=10011000002";
		String bb = "jsapi_ticket=sM4AOVdWfPE4DxkXGEs8VMCPGGVi4C3VM0P37wVUCFvkVAy_90u5h9nbSlYy3-Sl-HhTdfl2fzFy1AOcHKP7qg&noncestr=Wm3WZYTPz0wzccnW&timestamp=1414587457&url=http://mp.weixin.qq.com?params=value";
		System.out.println(getSha1(aa));
		System.out.println(getSha1(bb));
	}

}