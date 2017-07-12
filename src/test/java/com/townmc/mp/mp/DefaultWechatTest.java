package com.townmc.mp.mp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.townmc.mp.DefaultWechat;
import com.townmc.mp.TokenManager;
import com.townmc.mp.Wechat;
import com.townmc.mp.model.AdvancedArticle;
import com.townmc.mp.model.Article;
import com.townmc.mp.model.Menu;
import com.townmc.mp.model.Token;
import com.townmc.mp.model.MpUser;
import com.townmc.mp.model.MpUserList;

/**
 * Unit test for DefaultWechat.
 */
public class DefaultWechatTest {
	private DefaultWechat wechat;
	
	//@Before
	public void init() {
		wechat = new DefaultWechat("wx938cf8cc98af75d4", "ac3127d511e0b6e43341cd1bd54ad0ac");
//		wechat = new DefaultWechat("wxfdbf277961c37911", "8e0cb639bf70c7b6a2897ac927e384bc");
		wechat.setTokenManager(new TokenManager() {
			public void toStorage(Token token) {
				//
			}
			
			public Token get(String appid) {
				Token token = new Token();
				token.setAppid("wx938cf8cc98af75d4");
				token.setUpdateTime(new Date());
				token.setExpireTime(new Date());
				token.setAccessToken("z1EnG5bQLrIbVZgNf5EQrHySt6_7gLa1E8b47jY5rKtNV9R8v6AqJReeO85trLx3BxDQtKyH4JJL-4wqkMnXGg");
				return null;
			}
		});
		
	}
	
	//@Test
	public void getAccessTokenTest() {
		String tokenStr = wechat.getAccessToken();
		System.out.println(tokenStr);
	}
	
	//@Test
	public void sendTextMsgTest() {
		wechat.sendTextMsg("b5c65283d6b14ac2b4991371eaa062f7", "��ã�����");
	}
	
	//@Test
	public void sentNewsTest() {
		List<Article> news = new ArrayList<Article>();
		Article art = new Article();
		art.setTitle("����ͼ����Ϣ");
		art.setDescription("ΰ����л����񹲺͹���������������");
		art.setUrl("http://meishi.qq.com/beijing");
		art.setPicurl("http://qqfood.tc.qq.com/meishishop/15/c4a47f52-3d52-4ef7-8ef9-8c05e9c02d2d/200");
		news.add(art);
		wechat.sendNewsMsg("oc4Grt7cMpH5eYbd2chyKGrx8bV0", news);
	}
	
	//@Test
	public void getUserTest() {
		MpUser user = wechat.getUser("aoc4Grt7cMpH5eYbd2chyKGrx8bV0");
		
		System.out.println(user.getCity());
		System.out.println(user.getCountry());
		System.out.println(user.getHeadimgurl());
		System.out.println(user.getLanguage());
		System.out.println(user.getNickname());
		System.out.println(user.getOpenid());
		System.out.println(user.getProvince());
		System.out.println(user.getSex());
		System.out.println(user.getSubscribe());
		System.out.println(user.getSubscribeTime());
	}
	
	//@Test
	public void redirectUrlTest() {
		//System.out.println(wechat.redirectUrl("http://114.215.101.31/wechat/user/card", "mycard"));
	}
	
	//@Test
	public void getUserListTest() {
		MpUserList users = wechat.getUserList(null);
		
		System.out.println(users.getTotal());
		System.out.println(users.getCount());
		System.out.println(users.getNextOpenid());
		
		for(String openid : users.getOpenids()) {
			System.out.println(openid);
		}
	}
	
	//@Test
	public void createMenuTest() {
		List<Menu> menus = new ArrayList<Menu>();
		Menu m1 = new Menu();
		m1.setType(Menu.MENU_TYPE_VIEW);
		m1.setName("点菜");
		//String redirectUri = "http://182.92.104.63/index.html";
		String redirectUri = "http://114.215.101.31/card.html?mid=01058121104";
		m1.setUrl("http://182.92.104.63/cdn/wechat/dish.html?merchantId=01058121104&v=1.1");
		menus.add(m1);
		
		Menu m2 = new Menu();
		m2.setType(Menu.MENU_TYPE_VIEW);
		m2.setName("会员卡");
		//String redir = "http://182.92.104.63/wechat/user/entry";
		String redir = "http://114.215.101.31/wechat/user/entry";
		m2.setUrl("http://182.92.104.63/cdn/wechat/card.html?merchantId=01058121104&v=1.3");
		menus.add(m2);
		
//		Menu m3 = new Menu();
//		m3.setName("����1��c");
//		List<Menu> m3Arr = new ArrayList<Menu>();
//		Menu m31 = new Menu();
//		m31.setType(Menu.MENU_TYPE_VIEW);
//		m31.setName("����2��a");
//		m31.setUrl("http://manage.life.qq.com");
//		m3Arr.add(m31);
//		Menu m32 = new Menu();
//		m32.setType(Menu.MENU_TYPE_CLICK);
//		m32.setName("����2��b");
//		m32.setKey("bbbbbbbbbbbaaa");
//		m3Arr.add(m32);
//		
//		m3.setSubButton(m3Arr);
//		
//		menus.add(m3);
		
		wechat.createMenu(menus);
	}
	
	//@Test
	public void deleteMenuTest() {
		wechat.deleteMenu();
	}
	
	//@Test
	public void getMenuTest() {
		List<Menu> menus = wechat.getMenu();
		this.printMenu(menus);
	}
	
	//@Test
	public void sedMediaMsgTest() {
		wechat.sendMediaMsg("o2FJ5jsz0F5nI50EUaMYxCzfVFFM", Wechat.MsgType.image, "789X2RilxEVpe17cPCft88Gb7weD3_6dJ3ptArSP4w9H0b1bTFwB5XM5bfjGrJFm");
	}
	
	//@Test
	public void uploadMediaTest() {
		String mediaId = wechat.uploadMedia("thumb", new File("D:/Pictures/nexusS/IMG_20120211_132647.jpg"));
		System.out.println("upload media : " + mediaId);
	}
	
	//@Test
	public void uploadNewsTest() {
		List<AdvancedArticle> arts = new ArrayList<AdvancedArticle>();
		AdvancedArticle a1 = new AdvancedArticle();
		a1.setThumbMediaId("FtJPwI8ABFFrnaX3AYcqk5EI-WbkzINrxj7reAnjR7gW_ri7jqxlwL0al3Wg7tdB");
		a1.setTitle("不要心慌，我在进行测试");
		a1.setContent("内部测试，没人看到，除了自己人，哈哈哈哈哈哈哈哈！！！！！！！！");
		a1.setContentSourceUrl("http://mp.weixin.qq.com");
		a1.setDigest("进行测试，没事，淡定！");
		a1.setShowCoverPic(AdvancedArticle.SHOW_COVER_PIC_YES);
		
		arts.add(a1);
		
		String mid = wechat.uploadNews(arts);
		System.out.println(mid);
	}
	
	//@Test
	public void sendNewsAdTest() {
		List<String> openids = new ArrayList<String>();
		openids.add("o2FJ5jsz0F5nI50EUaMYxCzfVFFM");
		openids.add("o2FJ5jtrBi-YkmbCvEhd5NJl1n0k");
		openids.add("o2FJ5js_enXsMdMf87UrF3QjMT2s");
		openids.add("o2FJ5jh6EN-ckIk5apbiSkL_zVMk");
		openids.add("o2FJ5jsw6vtH7xl_lbhq2dLf-oCc");
		openids.add("o2FJ5jtEBKcqQRjQWjcSi6_lmmL4");
		openids.add("o2FJ5joOpCWkdl7cUBnUVeDgcyFs");
		
		wechat.sendNewsAdvanced(openids, "k3APV7Q86wHxhqXcgfUCyf25x1vn-gNL4rWxR2fcIp8NPpUcdGt2V0tP7Jn9u3kj");
	}
	
	//@Test
	public void createQrCodeTest() {
		String ticket = wechat.createQrCode(1);
		System.out.println(ticket);
	}
	
	private void printMenu(List<Menu> menus) {
		System.out.println("===========");
		for(Menu m : menus) {
			if(null != m.getType()) System.out.println("type:" + m.getType());
			if(null != m.getName()) System.out.println("name:" + m.getName());
			if(null != m.getKey()) System.out.println("key:" + m.getKey());
			if(null != m.getUrl()) System.out.println("url:" + m.getUrl());
			if(null != m.getSubButton()) this.printMenu(m.getSubButton());
			
		}
	}

}
