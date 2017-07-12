package com.townmc.mp.model;

import java.io.Serializable;

/**
 * 多条图文内容
 * @author meng
 *
 */
public class Article implements Serializable {

	private static final long serialVersionUID = 7461994896526615811L;
	private String title; // 标题
	private String description; // 描述
	private String url; // 点击后跳转的链接
	private String picurl; // 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPicurl() {
		return picurl;
	}
	public void setPicurl(String picurl) {
		this.picurl = picurl;
	}
	
}
