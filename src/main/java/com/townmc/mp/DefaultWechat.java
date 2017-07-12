package com.townmc.mp;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.townmc.mp.model.*;
import com.townmc.utils.Http;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.townmc.mp.json.JSONArray;
import com.townmc.mp.json.JSONObject;
import com.townmc.mp.utils.MpUtils;

public class DefaultWechat implements Wechat {
	private static final Log log = LogFactory.getLog(DefaultWechat.class);

	private static final int TOKEN_REFRESH_TIME = 1800000; // token刷新时间 半小时
	private static final String GET_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";
	private static final String SEND_MSG = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
	private static final String GET_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info";
	private static final String GET_OPENID_BY_CODE = "https://api.weixin.qq.com/sns/oauth2/access_token";
	private static final String GET_USER_LIST = "https://api.weixin.qq.com/cgi-bin/user/get";
	private static final String CREATE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/create";
	private static final String GET_MENU = "https://api.weixin.qq.com/cgi-bin/menu/get";
	private static final String DELETE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/delete";
	private static final String UPLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/upload";
	private static final String UPLOAD_NEWS = "https://api.weixin.qq.com/cgi-bin/media/uploadnews";
	private static final String SEND_MEDIA_ADVANCED = "https://api.weixin.qq.com/cgi-bin/message/mass/send";
	private static final String CREATE_QRCODE = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
	private static final String SHOW_QRCODE = "https://mp.weixin.qq.com/cgi-bin/showqrcode";
	private static final String SEND_TEMPLATE_MSG = "https://api.weixin.qq.com/cgi-bin/message/template/send";
	private static final String SEND_API_ADD_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/template/api_add_template?access_token={0}";
	private static final String PAY_UNIFIEDORDER="https://api.mch.weixin.qq.com/pay/unifiedorder";
	private static final String JSAPI_TICKET="https://api.weixin.qq.com/cgi-bin/ticket/getticket";
	private static final String ORDER_QUERY="https://api.mch.weixin.qq.com/pay/orderquery";
	private static final String RED_PACK="https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	
	private String appid;
	private String secret;
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}

	private TokenManager tokenManager;
	public void setTokenManager(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

	public DefaultWechat(String appid, String secret) {
		this.appid = appid;
		this.secret = secret;
	}
	
	/**
	 * 获得app_id
	 * @return
	 */
	public String getWechatAppId() {
		return this.getAppid();
	}
	

	/**
	 * 获得component_app_id
	 * @return
	 */
	public String getCompAppId() {
		return null;
	}

	public String getCompAccessToken() {
		return null;
	}
	
	/**
	 * 获得access_token，与微信接口交互的凭证
	 * @return
	 */
	public String getAccessToken() {
		Token token = tokenManager.get(this.appid);
		
		// 如果token超过定义的刷新的时间了，重新获取
		if(null == token || null == token.getAccessToken() || "".equals(token.getAccessToken()) || 
				System.currentTimeMillis() - token.getUpdateTime().getTime() > TOKEN_REFRESH_TIME) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("grant_type", "client_credential");
			params.put("appid", this.appid.trim());
			params.put("secret", this.secret.trim());
			
			log.debug("get access token form weixin. appid:" + this.appid + ". secret:" + this.secret);

			Http http = new Http();
			String responseStr = http.get(GET_TOKEN, params);
			http.close();
			
			if (responseStr == null || "".equals(responseStr)) {
				throw new MpException("get access token error! reson: responseStr is null, check secret space in tipdianka and dianka db?");
			}
			
			JSONObject json = new JSONObject(responseStr);
			
			if(!json.isNull("errcode")) {
				System.out.println("getAccessTokenResult="+json.toString());
				throw new MpException("get access token error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
			}
			
			String tokenValue = json.getString("access_token");
			token = new Token();
			token.setAppid(this.appid);
			token.setAccessToken(tokenValue);
			token.setUpdateTime(new Date());
			int expiresIn = json.getInt("expires_in");
			token.setExpireTime(new Date(System.currentTimeMillis() + expiresIn*1000));
			
			tokenManager.toStorage(token);
			
		}

		return token.getAccessToken();
	}

	/**
	 * 给某个用户发送文本消息
	 * @param openid 消息接收者的openid
	 * @param text 文本消息内容
	 */
	public void sendTextMsg(String openid, String text) {
		if(null == openid) {
			throw new MpException("parameter openid is null!");
		}
		if(null == text) {
			throw new MpException("message content is null!");
		}
		text = text.replaceAll("/换行", "\n");
		JSONObject json = new JSONObject().put("touser", openid).put("msgtype", "text").put("text", new JSONObject().put("content", text));
		Http http = new Http();
		http.post(SEND_MSG + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();
	}
	
	public void sendMpNewsMsg(List<String> openids, String mediaId) {
		this.sendNewsAdvanced(openids, mediaId);
	}
	
	public void sendMpNewsMsg(List<String> openids, String mediaId,String type) {
		this.sendNewsAdvancedByType(openids, mediaId, type);
	}

	/**
	 * 给某个用户发送多媒体消息
	 * @param openid 消息接收者的openid
	 * @param mediaType 多媒体消息的类型 image|voice|video
	 * @param mediaId 多媒体消息id
	 */
	public void sendMediaMsg(String openid, MsgType mediaType, String mediaId) {
		if(null == openid) {
			throw new MpException("parameter openid is null!");
		}
		if(null == mediaType) {
			throw new MpException("parameter mediaType is null!");
		}
		if(null == mediaId) {
			throw new MpException("parameter mediaId is null!");
		}
		
		JSONObject json = new JSONObject().put("touser", openid).put("msgtype", mediaType.toString()).put(mediaType.toString(), new JSONObject().put("media_id", mediaId));

		Http http = new Http();
		http.post(SEND_MSG + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();
	}

	/**
	 * 给某个用户发送多条图文消息
	 * @param openid 消息接收者的openid
	 * @param news
	 */
	public void sendNewsMsg(String openid, List<Article> news) {
		if(null == openid) {
			throw new MpException("parameter openid is null!");
		}
		if(null == news || news.size() == 0) {
			throw new MpException("parameter news is null!");
		}
		
		JSONArray arr = new JSONArray();
		for(int i = 0; i < news.size(); i++) {
			Article art = news.get(i);
			arr.put(i, new JSONObject().put("title", art.getTitle()).put("description", 
					art.getDescription()).put("url", art.getUrl()).put("picurl", art.getPicurl()));
		}
		
		JSONObject json = new JSONObject().put("touser", openid).put("msgtype", "news").put("news", new JSONObject().put("articles", arr));

		Http http = new Http();
		http.post(SEND_MSG + "?access_token=" + this.getAccessToken(), json.toString());
		
	}
	
	/**
	 * 获取用户基本信息
	 * @param openid 用户的openid
	 * @return
	 */
	public MpUser getUser(String openid) {
		if(null == openid) {
			throw new MpException("parameter openid is null!");
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", this.getAccessToken());
		params.put("openid", openid);

		Http http = new Http();
		String responseStr = http.get(GET_USER_INFO, params);
		http.close();

		JSONObject json = new JSONObject(responseStr);
		
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("get user info error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
		int subscribe = json.getInt("subscribe");
		
		MpUser user = new MpUser();
		user.setSubscribe(subscribe);
		user.setOpenid(json.getString("openid"));
		if(1 == subscribe) { // 如果用户取消关注了，是获取不到其他信息的
			user.setCity(json.getString("city"));
			user.setCountry(json.getString("country"));
			user.setHeadimgurl(json.getString("headimgurl"));
			user.setLanguage(json.getString("language"));
			user.setNickname(json.getString("nickname"));
			user.setProvince(json.getString("province"));
			user.setSex(json.getInt("sex"));
			user.setSubscribeTime(json.getInt("subscribe_time"));
			user.setUnionid(json.getString("unionid"));
			user.setRemark(json.getString("remark"));
			user.setGroupid(String.valueOf(json.getInt("groupid")));
		}
		return user;
	}

	/**
	 * 通过code换取openid
	 * @param code 微信网页回调地址是携带的code参数
	 * @return
	 */
	public String getOpenidByCode(String code) {
		if(null == code) {
			throw new MpException("parameter code is null!");
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("appid", this.appid.trim());
		params.put("secret", this.secret.trim());
		params.put("code", code);
		params.put("grant_type", "authorization_code");

		Http http = new Http();
		String responseStr = http.get(GET_OPENID_BY_CODE, params);

		JSONObject json = new JSONObject(responseStr);
		
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("get user info error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
		
		String openid = json.getString("openid");
		
		log.debug("get openid by code. code:" + code + ". openid:" + openid);
		
		return openid;
	}

	/**
	 * 获取关注者列表
	 * @param nextOpenid 第一个拉取的OPENID，为空默认从头开始拉取
	 * @return
	 */
	public MpUserList getUserList(String nextOpenid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", this.getAccessToken());
		if(null != nextOpenid) {
			params.put("next_openid", nextOpenid);
		}
		Http http = new Http();
		String responseStr = http.get(GET_USER_LIST, params);

		JSONObject json = new JSONObject(responseStr);
		
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("get user info error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
		
		MpUserList list = new MpUserList();
		list.setTotal(json.getInt("total"));
		list.setCount(json.getInt("count"));
		list.setNextOpenid(json.getString("next_openid"));
		JSONObject data = json.getJSONObject("data");
		JSONArray arr = data.getJSONArray("openid");
		
		List<String> openids = new ArrayList<String>();
		for(int i = 0; i < arr.length(); i++) {
			openids.add(arr.getString(i));
		}
		list.setOpenids(openids);
		return list;
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
		System.out.println("redirectUrl");
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s&component_appid=%s#wechat_redirect";
		try {
			redirectUrl = URLEncoder.encode(redirectUrl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return String.format(url, this.appid, redirectUrl, "snsapi_base", state, componentAppid);
	}
	
	public void createMenu(List<Menu> menus) {
		if(null == menus || menus.size() == 0) {
			throw new MpException("parameter menus is null!");
		}
		
		JSONObject body = new JSONObject().put("button", new JSONArray(menus));

		Http http = new Http();
		String response = http.post(CREATE_MENU + "?access_token=" + this.getAccessToken(), body.toString());
		http.close();

		JSONObject json = new JSONObject(response);
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("create menu error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
	}
	
	public List<Menu> getMenu() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", this.getAccessToken());

		Http http = new Http();
		String responseStr = http.get(GET_MENU, params);

		JSONObject json = new JSONObject(responseStr);
		
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("get menu error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
		
		JSONObject menu = json.getJSONObject("menu");
		JSONArray button = menu.getJSONArray("button");
		
		return this.parserMenu(button);
	}
	
	private List<Menu> parserMenu(JSONArray arr) {
		List<Menu> result = new ArrayList<Menu>();
		for(int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			Menu m = new Menu();
			if(!obj.isNull("type")) {
				m.setType(obj.getString("type"));
			}
			if(!obj.isNull("name")) {
				m.setName(obj.getString("name"));
			}
			if(!obj.isNull("key")) {
				m.setKey(obj.getString("key"));
			}
			if(!obj.isNull("url")) {
				m.setUrl(obj.getString("url"));
			}
			if(!obj.isNull("sub_button")) {
				JSONArray subBut = obj.getJSONArray("sub_button");
				m.setSubButton(this.parserMenu(subBut));
			}
			result.add(m);
		}
		return result;
	}
	
	public void deleteMenu() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", this.getAccessToken());

		Http http = new Http();
		String responseStr = http.get(DELETE_MENU, params);
		http.close();

		JSONObject json = new JSONObject(responseStr);
		
		if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
			throw new MpException("delete menu error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
		}
	}

	/**
	 *
	 * @param msgXml 接收到的信息流
	 * @param handler 消息处理者
	 * @return Map
	 * 	msgType (image/voice/video/location/link/event)
	 * 	openid
	 * 	msgTime
	 * 	isAutoReply (当消息类型为文本时)
	 */
	public Map<String, Object> receiveMsg(String msgXml, MsgHandler handler) {
		Map<String, Object> re = new HashMap<String, Object>();
		try {
			Document doc = DocumentHelper.parseText(msgXml);

			Element root = doc.getRootElement();

			String msgType = root.elementText("MsgType");
			String openid = root.elementText("FromUserName");
			int msgTime = Integer.valueOf(root.elementText("CreateTime"));

			re.put("ToUserName", root.elementText("ToUserName"));
			re.put("FromUserName", openid);
			re.put("CreateTime", msgTime);
			re.put("MsgType", msgType);
			re.put("openid", openid);

			if("text".equals(msgType)) {

				String content = root.elementText("Content");
				String msgId = root.elementText("MsgId");

				boolean isAutoReply = handler.textMessage(openid, msgTime, content, msgId);
				re.put("Content", content);
				re.put("isAutoReply", isAutoReply);

			} else if("image".equals(msgType)) {

				String picUrl = root.elementText("PicUrl");
				String mediaId = root.elementText("MediaId");
				String msgId = root.elementText("MsgId");

				handler.imageMessage(openid, msgTime, picUrl, mediaId, msgId);

				re.put("PicUrl", picUrl);
				re.put("MediaId", mediaId);
				re.put("MsgId", msgId);

			} else if("voice".equals(msgType)) {
				log.debug("=========== voice comming ============");
				String mediaId = root.elementText("MediaId");
				String format = root.elementText("Format");
				String msgId = root.elementText("MsgId");
				String recognition = root.elementText("Recognition");
				log.debug("=========== voice is : " + recognition + " ============");
				handler.voiceMessage(openid, msgTime, mediaId, format, msgId, recognition);

				re.put("MediaId", mediaId);
				re.put("Format", format);
				re.put("MsgId", msgId);
			} else if("video".equals(msgType)) {

				String mediaId = root.elementText("MediaId");
				String thumbMediaId = root.elementText("ThumbMediaId");
				String msgId = root.elementText("MsgId");

				handler.videoMessage(openid, msgTime, mediaId, thumbMediaId, msgId);

				re.put("MediaId", mediaId);
				re.put("ThumbMediaId", thumbMediaId);
				re.put("MsgId", msgId);
			} else if("location".equals(msgType)) {

				double longitude = Double.valueOf(root.elementText("Location_Y"));
				double latitude = Double.valueOf(root.elementText("Location_X"));
				int scale = Integer.valueOf(root.elementText("Scale"));
				String label = root.elementText("Label");
				String msgId = root.elementText("MsgId");

				handler.locationMessage(openid, msgTime, longitude, latitude, scale, label, msgId);

				re.put("Location_Y", longitude);
				re.put("Location_X", latitude);
				re.put("Scale", scale);
				re.put("Label", label);
				re.put("MsgId", msgId);
			} else if("link".equals(msgType)) {

				String title = root.elementText("Title");
				String description = root.elementText("Description");
				String url = root.elementText("Url");
				String msgId = root.elementText("MsgId");

				handler.linkMessage(openid, msgTime, title, description, url, msgId);

				re.put("Title", title);
				re.put("Description", description);
				re.put("Url", url);
				re.put("MsgId", msgId);
			} else if("event".equals(msgType)) {

				String event = root.elementText("Event");

				re.put("Event", event);
				if("subscribe".equals(event)) {

					String eventKey = root.elementText("EventKey");

					re.put("EventKey", eventKey);
					if(null != eventKey && !"".equals(eventKey)) {

						String ticket = root.elementText("Ticket");
						re.put("Ticket", ticket);
						handler.subscribeQrEvent(openid, msgTime, eventKey, ticket);

					} else {

						handler.subscribeEvent(openid, msgTime);

					}
				} else if("unsubscribe".equals(event)) {

					handler.unsubscribeEvent(openid, msgTime);

				} else if("SCAN".equals(event)) {

					String eventKey = root.elementText("EventKey");
					String ticket = root.elementText("Ticket");
					re.put("EventKey", eventKey);
					re.put("Ticket", ticket);

					handler.scanEvent(openid, msgTime, eventKey, ticket);

				} else if("LOCATION".equals(event)) {

					double latitude = Double.valueOf(root.elementText("Latitude"));
					double longitude = Double.valueOf(root.elementText("Longitude"));
					double precision = Double.valueOf(root.elementText("Precision"));
					re.put("Latitude", latitude);
					re.put("Longitude", longitude);
					re.put("Precision", precision);

					handler.locationEvent(openid, msgTime, longitude, latitude, precision);

				} else if("CLICK".equals(event)) {

					String eventKey = root.elementText("EventKey");
					re.put("EventKey", eventKey);

					handler.clickEvent(openid, msgTime, eventKey);

				} else if("VIEW".equals(event)) {

					String eventKey = root.elementText("EventKey");
					re.put("EventKey", eventKey);

					handler.viewEvent(openid, msgTime, eventKey);

				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return re;
	}

	public String uploadMedia(String type, File file) {
		String url = UPLOAD_MEDIA + "?access_token=" + this.getAccessToken() + "&type=" + type;

		String re = HTTP.uploadFile(url, file);
		
		if(null != re) {
			JSONObject json = new JSONObject(re);
			if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
				throw new MpException("upload media error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
			}
			if(type.equals("thumb")) {
				return json.getString("thumb_media_id");
			} else {
				return json.getString("media_id");
			}
		} else {
			throw new MpException("upload media error!");
		}
	}
	
	public String uploadNews(List<AdvancedArticle> articles) {
		JSONArray arr = new JSONArray();
		for(int i = 0; i < articles.size(); i++) {
			AdvancedArticle art = articles.get(i);
			arr.put(i, new JSONObject().put("thumb_media_id", art.getThumbMediaId())
					.put("author", art.getAuthor()).put("title", art.getTitle())
					.put("content_source_url", art.getContentSourceUrl())
					.put("content", art.getContent()).put("digest", art.getDigest())
					.put("show_cover_pic", art.getShowCoverPic()));
		}
		
		JSONObject json = new JSONObject().put("articles", arr);

		Http http = new Http();
		String re = http.post(UPLOAD_NEWS + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();

		if(null != re) {
			JSONObject reJson = new JSONObject(re);
			if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("upload news error! reson: " + reJson.getInt("errcode") + ". " + reJson.getString("errmsg"));
			}
			return reJson.getString("media_id");
		} else {
			throw new MpException("upload news error!");
		}
		
	}
	
	public void sendNewsAdvanced(List<String> openids, String mediaId) {
		this.sendNewsAdvancedByType(openids, mediaId, "mpnews");
	}
	
	public void sendNewsAdvancedByType(List<String> openids, String mediaId,String type) {
		JSONArray arr = new JSONArray();
		for(int i = 0; i < openids.size(); i++) {
			arr.put(i, openids.get(i));
		}
		JSONObject json  = new JSONObject();
		if("text".equals(type)){
			 json = new JSONObject().put("touser", arr).put("text", new JSONObject().put("content", mediaId)).put("msgtype", "text");
		}else if("voice".equals(type)){
			 json = new JSONObject().put("touser", arr).put("voice", new JSONObject().put("media_id", mediaId)).put("msgtype", "voice");
		}else if("image".equals(type)){
			 json = new JSONObject().put("touser", arr).put("image", new JSONObject().put("media_id", mediaId)).put("msgtype", "image");
		}else{
			 json = new JSONObject().put("touser", arr).put("mpnews", new JSONObject().put("media_id", mediaId)).put("msgtype", "mpnews");
		}
		Http http = new Http();
		String re = http.post(SEND_MEDIA_ADVANCED + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();

		if(null != re) {
			JSONObject reJson = new JSONObject(re);
			if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("send News Advanced error! reson: " + reJson.getInt("errcode") + ". " + reJson.getString("errmsg"));
			}
		} else {
			throw new MpException("send News Advanced error!");
		}
	}
	
	/**
	 * 创建参数的永久二维码
	 * @param sceneId 场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）
	 * @return
	 */
	public String createQrCode(int sceneId) {
		
		JSONObject json = new JSONObject().put("action_name", "QR_LIMIT_SCENE").put("action_info", new JSONObject().put("scene", new JSONObject().put("scene_id", sceneId)));

		Http http = new Http();
		String re = http.post(CREATE_QRCODE + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();

		if(null != re) {
			JSONObject reJson = new JSONObject(re);
			if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("upload news error! reson: " + reJson.getInt("errcode") + ". " + reJson.getString("errmsg"));
			}
			return reJson.getString("ticket");
		} else {
			throw new MpException("upload news error!");
		}
	}
	
	/**
	 * 创建带参数的临时性二维码
	 * @param sceneId 场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）
	 * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过1800
	 * @return
	 */
	public String createExpireQrCode(int sceneId, int expireSeconds) {
		JSONObject json = new JSONObject().put("expire_seconds", expireSeconds).put("action_name", "QR_SCENE").put("action_info", new JSONObject().put("scene", new JSONObject().put("scene_id", sceneId)));

		Http http = new Http();
		String re = http.post(CREATE_QRCODE + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();

		if(null != re) {
			JSONObject reJson = new JSONObject(re);
			if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("upload news error! reson: " + reJson.getInt("errcode") + ". " + reJson.getString("errmsg"));
			}
			return reJson.getString("ticket");
		} else {
			throw new MpException("upload news error!");
		}
	}
	
	/**
	 * 通过ticket获得二维码
	 * @param ticket
	 * @return
	 */
	public String showQrCodeUrl(String ticket) {
		return SHOW_QRCODE + "?ticket=" + ticket;
	}

	public String apiAddTemplate(String templateIdShort) {
		JSONObject json = new JSONObject().put("template_id_short", templateIdShort);

		String url = MessageFormat.format(SEND_API_ADD_TEMPLATE, this.getAccessToken());
		Http http = new Http();
		String re = http.post(url, json.toString());
		http.close();

		if(null != re) {
			JSONObject reJson = new JSONObject(re);
			if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("upload news error! reson: " + reJson.getInt("errcode") + ". " + reJson.getString("errmsg"));
			}
			return reJson.getString("template_id");
		} else {
			throw new MpException("upload news error!");
		}
	}

	/**
	 * 
	 * 给某个用户发送模版消息
	 * @param openid 消息接收者的openid
	 * @param templateId
	 * @param jumpUrl
	 * @param paramList
	 */
	public boolean sendTemplateMsg(String openid, String templateId, String jumpUrl, List<TemplateParam> paramList) {
		if (null == openid) {
			throw new MpException("parameter openid is null!");
		}
		if (null == paramList || paramList.size() == 0) {
			throw new MpException("parameter is null!");
		}

		JSONObject dataObj = new JSONObject();
		for(int i = 0; i < paramList.size(); i++) {
			TemplateParam templateParam = paramList.get(i);
			String color = templateParam.getParamKeyColor();
			dataObj.put(
					templateParam.getParamKeyName(),
					new JSONObject().put("value",
							templateParam.getParamKeyValue()).put("color",
							(color == null) ? "#173177" : color));

		}
		
		JSONObject json = new JSONObject().put("touser", openid).put("template_id", templateId).put("url", jumpUrl).put("topcolor", "#FF0000").put("data", dataObj);
		System.out.println(json.toString());

		Http http = new Http();
		String result = http.post(SEND_TEMPLATE_MSG + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();
		if (null != result) {
			JSONObject reJson = new JSONObject(result);
			if (!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("send trmplate msg error! reson: "
						+ reJson.getInt("errcode") + ". "
						+ reJson.getString("errmsg"));
			}
			return true;
		} else {
			throw new MpException("send trmplate msg error!");
		}
	}
	

	/**
	 * 统一下单获取prepay_id
	 * 返回appId、timeStamp、nonceStr、package、signType、paySign
	 * @param unifiedorder 统一下单参数
	 * @param key  商户的key
	 */
	public Map<String, Object> unifiedOrder(Unifiedorder unifiedorder, String key){
		if(unifiedorder.getAppid()==null||unifiedorder.getMch_id()==null||unifiedorder.getBody()==null||
				unifiedorder.getOut_trade_no()==null||unifiedorder.getTotal_fee()==null||unifiedorder.getSpbill_create_ip()==null||
				unifiedorder.getNotify_url()==null||unifiedorder.getTrade_type()==null)
			throw new MpException("param_error","some params could not be null!");
		String nonceStr = MpUtils.getNonceStr();
		unifiedorder.setNonce_str(nonceStr);
		String payStr = "";
		try {
			payStr = MpUtils.getPayXmlStr(unifiedorder, key);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		log.debug("payStr:"+payStr);
		String accessToken = this.getAccessToken();
		String re = HTTPS.post(PAY_UNIFIEDORDER + "?access_token=" + accessToken, payStr);
		
		if(null != re) {
		  Map<String, Object> reMap = MpUtils.parse(re);
		  if(!"SUCCESS".equals(reMap.get("return_code"))){
			  throw new MpException("failed to get prepay_id!", "return_code=" + reMap.get("return_code"));
		  }else if("签名失败".equals(reMap.get("return_msg"))){
			  throw new MpException("sign failed from unifiedOrder!");
		  }else if("XML格式错误".equals(reMap.get("return_msg"))){
			  throw new MpException("incorrect XML format");
		  }
		  long timeStamp = new Date().getTime();
		  Map<String, Object> signMap = new HashMap<String, Object>();
				  signMap.put("appId", reMap.get("appid"));
				  signMap.put("timeStamp", timeStamp);
				  signMap.put("nonceStr", reMap.get("nonce_str"));
				  signMap.put("package", "prepay_id="+reMap.get("prepay_id"));
				  signMap.put("signType", "MD5");
			String[] paySignStr = new String[]{"appId","timeStamp","nonceStr","package","signType"};	  
			String paySign = MpUtils.getSign(signMap, paySignStr, key);
			
			reMap.put("timeStamp", timeStamp);
			reMap.put("paySign", paySign);
		  return reMap;
		  }
		return null;
	}
	
	/**
	 * jsSDK签名
	 * @return
	 */
	public String getJsApiTicket(String accessToken){
		Http http = new Http();
		String re = http.get(JSAPI_TICKET+ "?access_token=" + accessToken+"&type=jsapi");
		if (null != re) {
			JSONObject reJson = new JSONObject(re);
			if (!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("send trmplate msg error! reson: "
						+ reJson.getInt("errcode") + ". "
						+ reJson.getString("errmsg"));
			}
			System.out.println("jsticket:"+re);
			return reJson.getString("ticket");
		} else {
			throw new MpException("send trmplate msg error!");
		}
	}
	
	public Map<String, Object> queryOrder(OrderQuery orderQuery, String key) {
		if(orderQuery.getAppid()==null||orderQuery.getMch_id()==null) throw new MpException("param_error", "some params could not be null!");
		String nonceStr = MpUtils.getNonceStr();
		orderQuery.setNonce_str(nonceStr);
		String orderStr="";
		try {
			orderStr = MpUtils.getPayXmlStr(orderQuery, key);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		Http http = new Http();
		String re = http.post(ORDER_QUERY+ "?access_token=" +this.getAccessToken(), orderStr);
		if(null != re) {
			  Map<String, Object> reMap = MpUtils.parse(re);
			  if(!"SUCCESS".equals(reMap.get("return_code"))){
				  throw new MpException("failed to get prepay_id!");
			  }else if("签名失败".equals(reMap.get("return_msg"))){
				  throw new MpException("sign failed from unifiedOrder!");
			  }else if("XML格式错误".equals(reMap.get("return_msg"))){
				  throw new MpException("incorrect XML format");
			  }
			  Map<String, Object> returnMap = new HashMap<String, Object>();
			  returnMap.put("return_code", reMap.get("return_code"));
			  returnMap.put("result_code", reMap.get("result_code"));
			  returnMap.put("return_msg", reMap.get("return_msg"));
			  if("SUCCESS".equals(reMap.get("return_code"))&&!"SUCCESS".equals(reMap.get("result_code"))){
				  returnMap.put("appid", reMap.get("appid"));
				  returnMap.put("mch_id", reMap.get("mch_id"));
				  returnMap.put("nonce_str", reMap.get("nonce_str"));
				  returnMap.put("sign", reMap.get("sign"));
				  returnMap.put("result_code", reMap.get("result_code"));
				  returnMap.put("err_code", reMap.get("err_code"));
				  returnMap.put("err_code_des", reMap.get("err_code_des"));
			  }
			  if("SUCCESS".equals(reMap.get("return_code"))&&"SUCCESS".equals(reMap.get("result_code"))){
				  returnMap.put("device_info", reMap.get("device_info"));
				  returnMap.put("openid", reMap.get("openid"));
				  returnMap.put("is_subscribe", reMap.get("is_subscribe"));
				  returnMap.put("trade_type", reMap.get("trade_type"));
				  returnMap.put("trade_state", reMap.get("trade_state"));
				  returnMap.put("bank_type", reMap.get("bank_type"));
				  returnMap.put("total_fee", reMap.get("total_fee"));
				  returnMap.put("fee_type", reMap.get("fee_type"));
				  returnMap.put("cash_fee", reMap.get("cash_fee"));
				  returnMap.put("cash_fee_type", reMap.get("cash_fee_type"));
				  returnMap.put("coupon_fee", reMap.get("coupon_fee"));
				  returnMap.put("coupon_count", reMap.get("coupon_count"));
				  returnMap.put("transaction_id", reMap.get("transaction_id"));
				  returnMap.put("out_trade_no", reMap.get("out_trade_no"));
				  returnMap.put("attach", reMap.get("attach"));
				  returnMap.put("time_end", reMap.get("time_end"));
				  returnMap.put("trade_state_desc", reMap.get("trade_state_desc"));
			  }
			  return returnMap;
	  }
		return null;
	}

	public Map<String, Object> sendRedPack(RedPack redPack,String keyFile, String keyPassWord) {
		String nonceStr = MpUtils.getNonceStr();
		redPack.setNonce_str(nonceStr);
		String redPackStr = null;
		try {
			redPackStr = MpUtils.getPayXmlStr(redPack, "herisonherisonherisonherisonheri");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println("redPackStr:"+redPackStr);
		String accessToken = this.getAccessToken();
		String re = "";
		try {
			re = HTTPS.postWithCer(RED_PACK + "?access_token=" + accessToken, redPackStr, keyFile,  keyPassWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("sendRedPack return:"+re);
		if(null != re) {
			  Map<String, Object> reMap = MpUtils.parse(re);
//			  if(!"SUCCESS".equals(reMap.get("return_code"))){
//				  throw new MpException("return_code:"+reMap.get("return_code")+" return_msg:" + reMap.get("return_msg"));
//			  }
			  return reMap;
		}
		return null;
	}
	
	public Map<String, Object> getJsConfig(String appId,String url) {
		String nonceStr = MpUtils.getNonceStr();
		String ticket = this.getJsApiTicket(this.getAccessToken());
		long timeStamp = new Date().getTime()/1000; //这里的时间戳是秒级的，而java中默认是毫秒级的
		String[] signStr = new String[4];
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jsapi_ticket", ticket);
		map.put("noncestr", nonceStr);
		map.put("timestamp", timeStamp);
		map.put("url", url);
		signStr[0] = "jsapi_ticket";
		signStr[1] = "noncestr";
		signStr[2] = "timestamp";
		signStr[3] = "url";
		String signature = MpUtils.getSha1Sign(map, signStr, null);
		
		Map<String, Object> re = new HashMap<String, Object>();
		re.put("appId", appId);
		re.put("timestamp", timeStamp);
		re.put("nonceStr", nonceStr);
		re.put("signature", signature.toLowerCase());
		return re;
	}

	public Map<String, Object> refund(Refund refund, String accessToken, String keyFile, String keyPassWord, String apiKey) {
		String refundStr = null;
		try {
			String nonceStr = MpUtils.getNonceStr();
			refund.setNonce_str(nonceStr);
			refundStr = MpUtils.getPayXmlStr(refund, apiKey);
			System.out.println("refundStr:" + refundStr);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		String re = "";
		try {
			re = HTTPS.postWithCer("https://api.mch.weixin.qq.com/secapi/pay/refund?access_token=" + accessToken, refundStr, keyFile, keyPassWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(re);

		if (null != re) {
			Map reMap = MpUtils.parse(re);
			return reMap;
		}
		return null;
	}
}
