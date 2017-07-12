package com.townmc.mp.model;

public class TemplateParam {
	
	public TemplateParam(String key, String value, String color) {
		this.paramKeyName = key;
		this.paramKeyValue = value;
		if (color == null || "".equals(color)) this.paramKeyColor = "#173177";
		else this.paramKeyColor = color;
	}
	
	private String paramKeyName; //参数名称
	private String paramKeyValue; //参数值
	private String paramKeyColor; //参数颜色
	public String getParamKeyName() {
		return paramKeyName;
	}
	public void setParamKeyName(String paramKeyName) {
		this.paramKeyName = paramKeyName;
	}
	public String getParamKeyValue() {
		return paramKeyValue;
	}
	public void setParamKeyValue(String paramKeyValue) {
		this.paramKeyValue = paramKeyValue;
	}
	public String getParamKeyColor() {
		return paramKeyColor;
	}
	public void setParamKeyColor(String paramKeyColor) {
		this.paramKeyColor = paramKeyColor;
	}
}
