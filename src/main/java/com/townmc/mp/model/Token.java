package com.townmc.mp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 公众号的全局唯一票据
 * @author meng
 *
 */
public class Token implements Serializable {
	private static final long serialVersionUID = 8063688221013092863L;
	private String appid; // 账号
	private String accessToken; // access_token的值
	private Date updateTime; // 最后更新时间
	private Date expireTime; // 到期时间

	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String value) {
		this.accessToken = value;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	
}
