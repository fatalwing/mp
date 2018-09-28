package com.townmc.mp;

import com.townmc.mp.json.JSONArray;
import com.townmc.mp.json.JSONObject;
import com.townmc.mp.model.*;
import com.townmc.mp.utils.MpUtils;
import com.townmc.utils.Http;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * 提供给第三方开发者模式时使用
 */
public class ComponentWechat extends DefaultWechat implements Wechat {
	private static final Log log = LogFactory.getLog(ComponentWechat.class);

	private String accessToken;
	private String componentAppid;
	private String componentAccessToken;

	public ComponentWechat(String appid, String accessToken, String componentAppid, String componentAccessToken) {
		this.appid = appid;
		this.accessToken = accessToken;
		this.componentAppid = componentAppid;
		this.componentAccessToken = componentAccessToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * 通过code换取openid
	 * @param code 微信网页回调地址是携带的code参数
	 * @return
	 */
	public String getOpenidByCode(String code) {
		Map<String, Object> re = Component.getAccessTokenByCode(componentAppid, this.appid, code, componentAccessToken);

		log.debug("get openid by code. code:" + code + ". openid:" + re.get("openid"));

		return (String)re.get("openid");
	}

	/**
	 * 微信第三方平台跳转
	 *
	 * @param redirectUrl
	 * @param state
	 * @return
	 */
	public String redirectUrl(String redirectUrl, String state) {
		log.debug("redirectUrl");
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&component_appid=%s#wechat_redirect";
		try {
			redirectUrl = URLEncoder.encode(redirectUrl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return String.format(url, this.appid, redirectUrl, "snsapi_base", state, this.componentAppid);
	}

}
