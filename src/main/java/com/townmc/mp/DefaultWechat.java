package com.townmc.mp;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
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

import com.townmc.mp.json.JSONArray;
import com.townmc.mp.json.JSONObject;
import com.townmc.mp.utils.MpUtils;

/**
 * 微信公众交互功能，包括获得token，发消息等等
 * 公众号有两种开发模式，一种是开发者模式，一种是第三方授权开发模式。因此本类抽象公用功能，两种模式决定的差异用具体的DevWechat和ComponentWechat实现。
 */
abstract class DefaultWechat {
	private static final Log log = LogFactory.getLog(DefaultWechat.class);

	public static final String GET_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";
	public static final String SEND_MSG = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
	public static final String GET_USER_INFO = "https://api.weixin.qq.com/cgi-bin/user/info";
	public static final String GET_OPENID_BY_CODE = "https://api.weixin.qq.com/sns/oauth2/access_token";
	public static final String GET_USER_LIST = "https://api.weixin.qq.com/cgi-bin/user/get";
	public static final String CREATE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/create";
	public static final String GET_MENU = "https://api.weixin.qq.com/cgi-bin/menu/get";
	public static final String DELETE_MENU = "https://api.weixin.qq.com/cgi-bin/menu/delete";
	public static final String UPLOAD_MEDIA = "https://api.weixin.qq.com/cgi-bin/material/add_material";
	public static final String UPLOAD_NEWS = "https://api.weixin.qq.com/cgi-bin/material/add_news";
	public static final String SEND_MEDIA_ADVANCED = "https://api.weixin.qq.com/cgi-bin/message/mass/send";
	public static final String CREATE_QRCODE = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
	public static final String SHOW_QRCODE = "https://mp.weixin.qq.com/cgi-bin/showqrcode";
	public static final String SEND_TEMPLATE_MSG = "https://api.weixin.qq.com/cgi-bin/message/template/send";
	public static final String SEND_API_ADD_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/template/api_add_template?access_token={0}";
	public static final String PAY_UNIFIEDORDER="https://api.mch.weixin.qq.com/pay/unifiedorder";
	public static final String JSAPI_TICKET="https://api.weixin.qq.com/cgi-bin/ticket/getticket";
	public static final String ORDER_QUERY="https://api.mch.weixin.qq.com/pay/orderquery";
	public static final String RED_PACK="https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";

	protected String appid;

	protected abstract String getAccessToken();

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
			user.setUnionid((!json.isNull("unionid")) ? json.getString("unionid") : "");
			user.setRemark(json.getString("remark"));
			user.setGroupid(String.valueOf(json.getInt("groupid")));
		}
		return user;
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
		log.debug("receive msg : " + msgXml);
		Map<String, Object> re = new HashMap<String, Object>();
		try {
			Document doc = DocumentHelper.parseText(msgXml);

			Element root = doc.getRootElement();
			String encrypt = root.elementText("Encrypt");

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

				String resp = handler.textMessage(openid, msgTime, content, msgId);
				re.put("Content", content);
				re.put("isAutoReply", false);
				re.put("ReplyContent", resp);

			} else if("image".equals(msgType)) {

				String picUrl = root.elementText("PicUrl");
				String mediaId = root.elementText("MediaId");
				String msgId = root.elementText("MsgId");

				String resp = handler.imageMessage(openid, msgTime, picUrl, mediaId, msgId);
				re.put("ReplyContent", resp);

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
				String resp = handler.voiceMessage(openid, msgTime, mediaId, format, msgId, recognition);
				re.put("ReplyContent", resp);

				re.put("MediaId", mediaId);
				re.put("Format", format);
				re.put("MsgId", msgId);
			} else if("video".equals(msgType)) {

				String mediaId = root.elementText("MediaId");
				String thumbMediaId = root.elementText("ThumbMediaId");
				String msgId = root.elementText("MsgId");

				String resp = handler.videoMessage(openid, msgTime, mediaId, thumbMediaId, msgId);
				re.put("ReplyContent", resp);

				re.put("MediaId", mediaId);
				re.put("ThumbMediaId", thumbMediaId);
				re.put("MsgId", msgId);
			} else if("location".equals(msgType)) {

				double longitude = Double.valueOf(root.elementText("Location_Y"));
				double latitude = Double.valueOf(root.elementText("Location_X"));
				int scale = Integer.valueOf(root.elementText("Scale"));
				String label = root.elementText("Label");
				String msgId = root.elementText("MsgId");

				String resp = handler.locationMessage(openid, msgTime, longitude, latitude, scale, label, msgId);
				re.put("ReplyContent", resp);

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

				String resp = handler.linkMessage(openid, msgTime, title, description, url, msgId);
				re.put("ReplyContent", resp);

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
						String resp = handler.subscribeQrEvent(openid, msgTime, eventKey, ticket);
						re.put("ReplyContent", resp);

					} else {

						String resp = handler.subscribeEvent(openid, msgTime);
						re.put("ReplyContent", resp);

					}
				} else if("unsubscribe".equals(event)) {

					handler.unsubscribeEvent(openid, msgTime);

				} else if("SCAN".equals(event)) {

					String eventKey = root.elementText("EventKey");
					String ticket = root.elementText("Ticket");
					re.put("EventKey", eventKey);
					re.put("Ticket", ticket);

					String resp = handler.scanEvent(openid, msgTime, eventKey, ticket);
					re.put("ReplyContent", resp);

				} else if("LOCATION".equals(event)) {

					double latitude = Double.valueOf(root.elementText("Latitude"));
					double longitude = Double.valueOf(root.elementText("Longitude"));
					double precision = Double.valueOf(root.elementText("Precision"));
					re.put("Latitude", latitude);
					re.put("Longitude", longitude);
					re.put("Precision", precision);

					String resp = handler.locationEvent(openid, msgTime, longitude, latitude, precision);
					re.put("ReplyContent", resp);

				} else if("CLICK".equals(event)) {

					String eventKey = root.elementText("EventKey");
					re.put("EventKey", eventKey);

					String resp = handler.clickEvent(openid, msgTime, eventKey);
					re.put("ReplyContent", resp);

				} else if("VIEW".equals(event)) {

					String eventKey = root.elementText("EventKey");
					re.put("EventKey", eventKey);

					String resp = handler.viewEvent(openid, msgTime, eventKey);
					re.put("ReplyContent", resp);

				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return re;
	}

	public String uploadMedia(String type, File file) {
		String url = UPLOAD_MEDIA + "?access_token=" + this.getAccessToken() + "&type=" + type;

		Http http = new Http();
		String re = http.uploadFile(url, "media", file);
		
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
	 * 创建参数的永久二维码
	 * @param sceneStr 场景值ID，场景值ID（字符串形式的ID），字符串类型，长度限制为1到64
	 * @return
	 */
	public String createQrCode(String sceneStr) {

		JSONObject json = new JSONObject().put("action_name", "QR_LIMIT_STR_SCENE").put("action_info", new JSONObject().put("scene", new JSONObject().put("scene_str", sceneStr)));

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
	 * 创建带参数的临时性二维码
	 * @param sceneStr 场景值ID，场景值ID（字符串形式的ID），字符串类型，长度限制为1到64
	 * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过1800
	 * @return
	 */
	public String createExpireQrCode(String sceneStr, int expireSeconds) {
		JSONObject json = new JSONObject().put("expire_seconds", expireSeconds).put("action_name", "QR_STR_SCENE").put("action_info", new JSONObject().put("scene", new JSONObject().put("scene_str", sceneStr)));

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
			String color = templateParam.getColor();
			dataObj.put(
					templateParam.getKey(),
					new JSONObject().put("value",
							templateParam.getValue()).put("color",
							(color == null) ? "#173177" : color));

		}
		
		JSONObject json = new JSONObject().put("touser", openid).put("template_id", templateId).put("url", jumpUrl).put("topcolor", "#FF0000").put("data", dataObj);
		log.debug(json.toString());

		Http http = new Http();
		String result = http.post(SEND_TEMPLATE_MSG + "?access_token=" + this.getAccessToken(), json.toString());
		http.close();
		if (null != result) {
			JSONObject reJson = new JSONObject(result);
			if (!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("send template msg error! reson: "
						+ reJson.getInt("errcode") + ". "
						+ reJson.getString("errmsg"));
			}
			return true;
		} else {
			throw new MpException("send template msg error!");
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
		Http http = new Http();
		String re = http.post(PAY_UNIFIEDORDER + "?access_token=" + accessToken, payStr);

		if(null != re) {
			Map<String, Object> reMap = MpUtils.parse(re);
			if(!"SUCCESS".equals(reMap.get("return_code"))){
				throw new MpException("failed to get prepay_id!", "return_code=" + reMap.get("return_code") + ". return_msg:" + reMap.get("return_msg"));
			}else if(!"SUCCESS".equals(reMap.get("result_code"))){
				throw new MpException("failed to get prepay_id!" + ". err_code_des:" + reMap.get("err_code_des"));
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
	public String getJsApiTicket(){
		Http http = new Http();
		String re = http.get(JSAPI_TICKET+ "?access_token=" + this.getAccessToken() +"&type=jsapi");
		if (null != re) {
			JSONObject reJson = new JSONObject(re);
			if (!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
				throw new MpException("send trmplate msg error! reson: "
						+ reJson.getInt("errcode") + ". "
						+ reJson.getString("errmsg"));
			}
			log.debug("jsticket:"+re);
			return reJson.getString("ticket");
		} else {
			throw new MpException("send trmplate msg error!");
		}
	}

	/**
	 * 获得试用JS-SDK页面需要注入的配置信息
	 * 所有需要使用JS-SDK的页面必须先注入配置信息，否则将无法调用。
	 * @param debug 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
	 * @param jsApiList 需要使用的JS接口列表
	 * @param url 当前网页的URL，不包含#及其后面部分
	 * @return
	 */
	public Map<String, Object> getJsConfig(Boolean debug, String[] jsApiList, String url) {
		String nonceStr = MpUtils.getNonceStr();
		String ticket = this.getJsApiTicket();
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
		if(null != debug) {
			debug = false;
		}
		re.put("debug", debug);
		re.put("appId", this.appid);
		re.put("timestamp", timeStamp);
		re.put("nonceStr", nonceStr);
		re.put("signature", signature.toLowerCase());
		re.put("jsApiList", jsApiList);

		return re;
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
				throw new MpException("failed to get prepay_id!" + ". return_msg:" + reMap.get("return_msg"));
			}else if(!"SUCCESS".equals(reMap.get("result_code"))){
				throw new MpException("failed to get prepay_id!" + ". err_code_des:" + reMap.get("err_code_des"));
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
		log.debug("redPackStr:"+redPackStr);
		String accessToken = this.getAccessToken();
		String re = "";
		try {
			Http http = new Http();
			re = http.postWithCer(RED_PACK + "?access_token=" + accessToken, redPackStr, keyFile,  keyPassWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("sendRedPack return:"+re);
		if(null != re) {
			  Map<String, Object> reMap = MpUtils.parse(re);
//			  if(!"SUCCESS".equals(reMap.get("return_code"))){
//				  throw new MpException("return_code:"+reMap.get("return_code")+" return_msg:" + reMap.get("return_msg"));
//			  }
			  return reMap;
		}
		return null;
	}

	public Map<String, Object> refund(Refund refund, String keyFile, String keyPassWord, String apiKey) {
		String refundStr = null;
		try {
			String nonceStr = MpUtils.getNonceStr();
			refund.setNonce_str(nonceStr);
			refundStr = MpUtils.getPayXmlStr(refund, apiKey);
			log.debug("refundStr:" + refundStr);
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
			Http http = new Http();
			re = http.postWithCer("https://api.mch.weixin.qq.com/secapi/pay/refund?access_token=" + this.getAccessToken(), refundStr, keyFile, keyPassWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug(re);

		if (null != re) {
			Map reMap = MpUtils.parse(re);
			return reMap;
		}
		return null;
	}

	private boolean isAutoResponse(String txt) {
		txt = txt.trim();
		if(txt.startsWith("<xml>") && txt.endsWith("</xml>")) {
			return true;
		} else {
			return false;
		}
	}
}
