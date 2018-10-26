package com.townmc.mp.model;

import java.io.Serializable;

public class TemplateParam implements Serializable {

	private String key; //参数名称
	private String value; //参数值
	private String color; //参数颜色

	public TemplateParam() {

	}
	
	public TemplateParam(String key, String value, String color) {
		this.key = key;
		this.value = value;
		if (color == null || "".equals(color)) this.color = "#173177";
		else this.color = color;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
}
