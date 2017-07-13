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

public class ComponentWechat extends DefaultWechat implements Wechat {
	private static final Log log = LogFactory.getLog(ComponentWechat.class);

	private String appId;
	private String accessToken;
	private String componentAppid;
	private String componentAccessToken;

	public String getAppId() {
		return appId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getComponentAppid() {
		return componentAppid;
	}

	public String getComponentAccessToken() {
		return componentAccessToken;
	}

	public ComponentWechat(String appid, String accessToken, String componentAppid, String componentAccessToken) {
		this.appId = appId;
		this.accessToken = accessToken;
		this.componentAppid = componentAppid;
		this.componentAccessToken = componentAccessToken;
	}

	/**
	 * 获得app_id
	 * @return
	 */
	public String getWechatAppId() {
		return this.getAppId();
	}

	/**
	 * 获得component_app_id
	 * @return
	 */
	public String getCompAppId() {
		return this.getComponentAppid();
	}

	public String getCompAccessToken() {
		return this.getComponentAccessToken();
	}

	/**
	 * 通过code换取openid
	 * @param code 微信网页回调地址是携带的code参数
	 * @return
	 */
	public String getOpenidByCode(String code) {
		Map<String, Object> re = Component.getAccessTokenByCode(componentAppid, appId, code, componentAccessToken);

		log.debug("get openid by code. code:" + code + ". openid:" + re.get("openid"));

		return (String)re.get("openid");
	}

	/**
	 * 微信第三方平台跳转
	 *
	 * @param redirectUrl
	 * @param state
	 * @param componentAppid
	 * @return
	 */
	public String redirectUrl(String redirectUrl, String state, String componentAppid) {
		log.debug("redirectUrl");
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&component_appid=%s#wechat_redirect";
		try {
			redirectUrl = URLEncoder.encode(redirectUrl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return String.format(url, this.appId, redirectUrl, "snsapi_base", state, componentAppid);
	}

}
