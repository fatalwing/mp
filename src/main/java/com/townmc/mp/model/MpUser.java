package com.townmc.mp.model;

import java.io.Serializable;

/**
 * 用户基本信息
 * @author meng
 *
 */
public class MpUser implements Serializable {

	private static final long serialVersionUID = -8886650913945748412L;
	private String openid; // 用户的标识，对当前公众号唯一
	private int subscribe; // 用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息
	private String nickname; // 用户的昵称
	private int sex; //用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
	private String language; // 用户的语言，简体中文为zh_CN
	private String country; // 用户所在国家
	private String province; // 用户所在省份
	private String city; // 用户所在城市
	private String headimgurl; // 用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空
	private int subscribeTime; // 用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
	private String unionid;
	private String remark;
	private String groupid;

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getGroupid() {
		return groupid;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public int getSubscribe() {
		return subscribe;
	}
	public void setSubscribe(int subscribe) {
		this.subscribe = subscribe;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	public int getSubscribeTime() {
		return subscribeTime;
	}
	public void setSubscribeTime(int subscribeTime) {
		this.subscribeTime = subscribeTime;
	}
	
}
