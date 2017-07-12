package com.townmc.mp.model;

import java.io.Serializable;

public class MenuEvent implements Serializable {
	private static final long serialVersionUID = 4181031782490678299L;
	public static final String EVENT_CLICK = "CLICK";
	public static final String EVENT_VIEW = "VIEW";

	private String toUserName; // 开发者微信号
	private String fromUserName; // 发送方帐号（一个OpenID）
	private String msgType; // 消息类型，event
	private String event; // 事件类型，CLICK或者VIEW
	private String eventKey; // 事件KEY值，与自定义菜单接口中KEY值对应
	private int createTime; // 消息创建时间 （整型）

	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getEventKey() {
		return eventKey;
	}
	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}
	public int getCreateTime() {
		return createTime;
	}
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	
}
