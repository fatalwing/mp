package com.townmc.mp.model;

import java.io.Serializable;
import java.util.List;

import com.townmc.mp.json.JsonKey;

/**
 * 目前自定义菜单最多包括3个一级菜单，每个一级菜单最多包含5个二级菜单。<br />
 * 一级菜单最多4个汉字，二级菜单最多7个汉字，多出来的部分将会以“...”代替。<br />
 * 请注意，创建自定义菜单后，由于微信客户端缓存，需要24小时微信客户端才会展现出来。<br />
 * 建议测试时可以尝试取消关注公众账号后再次关注，则可以看到创建后的效果。
 * 
 * @author meng
 *
 */
public class Menu implements Serializable {
	private static final long serialVersionUID = -1891313700072413473L;
	public static final String MENU_TYPE_CLICK = "click";
	public static final String MENU_TYPE_VIEW = "view";
	public static final String OPEN_WX_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=null&component_appid=%s#wechat_redirect";

	public String type; // 菜单的响应动作类型，目前有click、view两种类型。当为二级菜单的入口时，type为空
	public String name; // 菜单标题，不超过16个字节，子菜单不超过40个字节
	public String key; // click类型时为EVENT的KEY值，用于消息接口推送，不超过128字节
	public String url; // view类型时为网页链接，用户点击菜单可打开链接，不超过256字节
	@JsonKey("sub_button") 
	public List<Menu> subButton; // 二级菜单数组，个数应为1~5个

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<Menu> getSubButton() {
		return subButton;
	}
	public void setSubButton(List<Menu> subButton) {
		this.subButton = subButton;
	}
}
