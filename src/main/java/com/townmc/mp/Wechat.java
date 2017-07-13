package com.townmc.mp;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.townmc.mp.model.*;

public interface Wechat {

	/**
	 * 给某个用户发送文本消息
	 * @param openid 消息接收者的openid
	 * @param text 文本消息内容
	 */
	public void sendTextMsg(String openid, String text);

	/**
	 * 给某个用户发送多媒体消息
	 * @param openid 消息接收者的openid
	 * @param mediaType 多媒体消息的类型 image|voice|video
	 * @param mediaId 多媒体消息id
	 */
	public void sendMediaMsg(String openid, MsgType mediaType, String mediaId);

	/**
	 * 给某个用户发送多条图文消息
	 * @param openid 消息接收者的openid
	 * @param news
	 */
	public void sendNewsMsg(String openid, List<Article> news);

	/**
	 * 高级群发接口
	 * @param openids
	 * @param mediaId
	 */
	public void sendMpNewsMsg(List<String> openids, String mediaId);
	
	/**
	 * 高级群发接口
	 * @param openids
	 * @param mediaId
	 * @param type
	 */
	public void sendMpNewsMsg(List<String> openids, String mediaId, String type);
	
	/**
	 * 获取用户基本信息
	 * @param openid 用户的openid
	 * @return
	 */
	public MpUser getUser(String openid);

	/**
	 * 通过code换取openid
	 * @param code 微信网页回调地址是携带的code参数
	 * @return
	 */
	public String getOpenidByCode(String code);

	/**
	 * 获取关注者列表
	 * @param nextOpenid 第一个拉取的OPENID，为空默认从头开始拉取
	 * @return
	 */
	public MpUserList getUserList(String nextOpenid);

	/**
	 * 微信第三方平台跳转 可获得code的跳转url地址
	 * 
	 * @param redirectUrl
	 * @param state
	 * @param componentAppid
	 * @return
	 */
	public String redirectUrl(String redirectUrl, String state, String componentAppid);
	
	/**
	 * 创建自定义菜单
	 * @param menus
	 */
	public void createMenu(List<Menu> menus);
	
	/**
	 * 查询自定义菜单的结构
	 * @return
	 */
	public List<Menu> getMenu();
	
	/**
	 * 删除自定义菜单
	 */
	public void deleteMenu();

	/**
	 * 响应微信推送过来的通知
	 * @param msgXml String
	 * @param handler MsgHandler
	 */
	public Map<String, Object> receiveMsg(String msgXml, MsgHandler handler);

	/**
	 * 上传多媒体消息
	 * @param type 图片（image）、语音（voice）、视频（video）和缩略图（thumb）
	 * @param file
	 * @return media_id
	 */
	public String uploadMedia(String type, File file);
	
	/**
	 * 上传图文消息素材用于高级群发接口
	 * @param articles
	 * @return
	 */
	public String uploadNews(List<AdvancedArticle> articles);
	
	/**
	 * 利用高级群发接口，发送多条图文消息
	 * @param openids
	 * @param mediaId
	 */
	public void sendNewsAdvanced(List<String> openids, String mediaId);
	
	/**
	 * 利用高级群发接口，发送各类型消息
	 * @param openids
	 * @param mediaId
	 * @param type(text:文本、mpnews:图文、voice:语音、image:图片)
	 */
	public void sendNewsAdvancedByType(List<String> openids, String mediaId, String type);
	
	/**
	 * 创建参数的永久二维码tiket
	 * @param sceneId 扫描关注事件返回的eventKey
	 * @return ticket
	 */
	public String createQrCode(int sceneId);
	
	/**
	 * 创建带参数的临时性二维码ticket
	 * @param sceneId 扫描关注事件返回的eventKey
	 * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过1800
	 * @return ticket
	 */
	public String createExpireQrCode(int sceneId, int expireSeconds);
	
	/**
	 * 通过ticket获得二维码展示地址
	 * @param ticket
	 * @return 
	 */
	public String showQrCodeUrl(String ticket);

	/**
	 * 从行业模板库选择模板到帐号后台，获得模板ID的过程可在MP中完成。为方便第三方开发者，提供通过接口调用的方式来获取模板ID
	 * @param templateIdShort 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式
	 * @return template_id
	 */
	public String apiAddTemplate(String templateIdShort);
	
	/**
	 * 
	 * 给某个用户发送模版消息
	 * @param openid 消息接收者的openid
	 * @param templateId
	 * @param jumpUrl
	 * @param paramList
	 */
	public boolean sendTemplateMsg(String openid, String templateId, String jumpUrl, List<TemplateParam> paramList);
	
	/**
	 * 统一下单获取prepay_id
	 * 返回appId、timeStamp、nonceStr、package、signType、paySign
	 * @param unifiedorder 统一下单参数
	 * @param key  商户的key
	 */
	public Map<String, Object> unifiedOrder(Unifiedorder unifiedorder, String key);
	
	/**
	 * jsSDK签名
	 * @return
	 */
	public String getJsApiTicket(String accessToken);
	
	public Map<String, Object> queryOrder(OrderQuery orderQuery, String key);
	
	public Map<String, Object> sendRedPack(RedPack redPack,String keyFile, String keyPassWord);
	
	public Map<String, Object> getJsConfig(String appId,String url);

	public Map<String, Object> refund(Refund refund, String accessToken, String keyFile, String keyPassWord, String apiKey);
	
}
