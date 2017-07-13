package com.townmc.mp;

/**
 * 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次
 * 消息排重，推荐使用msgId排重
 * 事件的排重，推荐使用推荐使用openid + msgTime
 *  
 * @author meng
 *
 */
public interface MsgHandler {

	/**
	 * 文本消息 
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param content 文本消息内容
	 * @param msgId 消息id，64位整型
	 * @return boolean 是否自动回复了消息
	 */
	public String textMessage(String openid, int msgTime, String content, String msgId);

	/**
	 * 图片消息
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param picUrl 图片链接
	 * @param mediaId 图片消息媒体id，可以调用多媒体文件下载接口拉取数据
	 * @param msgId 消息id，64位整型
	 */
	public String imageMessage(String openid, int msgTime, String picUrl,
			String mediaId, String msgId);

	/**
	 * 语音消息
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param mediaId 语音消息媒体id，可以调用多媒体文件下载接口拉取数据
	 * @param format 语音格式，如amr，speex等
	 * @param msgId 消息id，64位整型
	 * @param recognition 语音识别结果（开通语音识别功能的前提下）
	 */
	public String voiceMessage(String openid, int msgTime, String mediaId,
			String format, String msgId, String recognition);

	/**
	 * 视频消息
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param mediaId 视频消息媒体id，可以调用多媒体文件下载接口拉取数据
	 * @param thumbMediaId 视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据
	 * @param msgId 消息id，64位整型
	 */
	public String videoMessage(String openid, int msgTime, String mediaId,
			String thumbMediaId, String msgId);

	/**
	 * 地理位置消息
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param longitude 地理位置经度
	 * @param latitude 地理位置维度
	 * @param scale 地图缩放大小
	 * @param label 地理位置信息
	 * @param msgId 消息id，64位整型
	 */
	public String locationMessage(String openid, int msgTime, double longitude,
			double latitude, int scale, String label, String msgId);

	/**
	 * 链接消息
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param title 消息标题
	 * @param description 消息描述
	 * @param url 消息链接
	 * @param msgId 消息id，64位整型
	 */
	public String linkMessage(String openid, int msgTime, String title,
			String description, String url, String msgId);

	/**
	 * 关注事件
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 */
	public String subscribeEvent(String openid, int msgTime);

	/**
	 * 取消关注事件
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 */
	public String unsubscribeEvent(String openid, int msgTime);

	/**
	 * 扫描带参数二维码事件（用户未关注时，进行关注后的事件推送）
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param eventKey 事件KEY值，qrscene_为前缀，后面为二维码的参数值。如qrscene_123123
	 * @param ticket 二维码的ticket，可用来换取二维码图片
	 */
	public String subscribeQrEvent(String openid, int msgTime, String eventKey, String ticket);

	/**
	 * 扫描带参数二维码事件（用户已关注时的事件推送）
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param eventKey 事件KEY值，是一个32位无符号整数，即创建二维码时的二维码scene_id
	 * @param ticket 二维码的ticket，可用来换取二维码图片
	 */
	public String scanEvent(String openid, int msgTime, String eventKey, String ticket);

	/**
	 * 上报地理位置事件
	 * 用户同意上报地理位置后，每次进入公众号会话时，都会在进入时上报地理位置，<br />
	 * 或在进入会话后每5秒上报一次地理位置，公众号可以在公众平台网站中修改以上设置。<br />
	 * 上报地理位置时，微信会将上报地理位置事件推送到开发者填写的URL。
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param longitude 地理位置经度
	 * @param latitude 地理位置纬度
	 * @param precision 地理位置精度
	 */
	public String locationEvent(String openid, int msgTime, double longitude,
			double latitude, double precision);

	/**
	 * 自定义菜单事件
	 * 用户点击自定义菜单后，微信会把点击事件推送给开发者，请注意，点击菜单弹出子菜单，不会产生上报
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param eventKey 事件KEY值，与自定义菜单接口中KEY值对应
	 */
	public String clickEvent(String openid, int msgTime, String eventKey);

	/**
	 * 点击菜单跳转链接时的事件推送
	 * 
	 * @param openid 发送方帐号
	 * @param msgTime 消息创建时间
	 * @param eventKey 事件KEY值，设置的跳转URL
	 */
	public String viewEvent(String openid, int msgTime, String eventKey);
}
