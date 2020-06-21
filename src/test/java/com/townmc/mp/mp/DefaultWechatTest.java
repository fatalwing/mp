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
import java.util.Map;

import com.townmc.mp.DevWechat;
import com.townmc.mp.Wxapp;
import com.townmc.mp.model.*;
import com.townmc.utils.JsonUtil;
import org.junit.Before;
import org.junit.Test;

import com.townmc.mp.TokenManager;
import com.townmc.mp.Wechat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for DefaultWechat.
 */
public class DefaultWechatTest {
	private static final Logger log = LoggerFactory.getLogger(DefaultWechatTest.class);

	private Wechat wechat;

	//@Before
	public void init() {
		wechat = new DevWechat("abcdef", "123456", new TokenManager() {
			public void toStorage(Token token) {
				//
			}

			public Token get(String appid) {
				Token token = new Token();
				token.setAppid("abcdef");
				token.setUpdateTime(new Date());
				token.setExpireTime(new Date());
				token.setAccessToken("z1EnG5bQLrIbVZgNf5EQrHySt6_7gLa1E8b47jY5rKtNV9R8v6AqJReeO85trLx3BxDQtKyH4JJL-4wqkMnXGg");
				return null;
			}
		});
//		wechat = new DefaultWechat("wxfdbf277961c37911", "8e0cb639bf70c7b6a2897ac927e384bc");
	}

	//@Test
	public void sendTextMsgTest() {
		wechat.sendTextMsg("b5c65283d6b14ac2b4991371eaa062f7", "��ã�����");
	}

	//@Test
	public void sentNewsTest() {
		List<Article> news = new ArrayList<Article>();
		Article art = new Article();
		art.setTitle("test");
		art.setDescription("test");
		art.setUrl("http://meishi.qq.com/beijing");
		art.setPicurl("http://qqfood.tc.qq.com/meishishop/15/c4a47f52-3d52-4ef7-8ef9-8c05e9c02d2d/200");
		news.add(art);
		wechat.sendNewsMsg("oc4Grt7cMpH5eYbd2chyKGrx8bV0", news);
	}

	//@Test
	public void getUserTest() {
		MpUser user = wechat.getUser("aoc4Grt7cMpH5eYbd2chyKGrx8bV0");

		log.info(user.getCity());
		log.info(user.getCountry());
		log.info(user.getHeadimgurl());
		log.info(user.getLanguage());
		log.info(user.getNickname());
		log.info(user.getOpenid());
		log.info(user.getProvince());
		log.info("" + user.getSex());
		log.info("" + user.getSubscribe());
		log.info("" + user.getSubscribeTime());
	}

	//@Test
	public void redirectUrlTest() {
		//log.info(wechat.redirectUrl("http://124.212.121.36/wechat/user/card", "mycard"));
	}

	//@Test
	public void getUserListTest() {
		MpUserList users = wechat.getUserList(null);

		log.info("" + users.getTotal());
		log.info("" + users.getCount());
		log.info(users.getNextOpenid());

		for(String openid : users.getOpenids()) {
			log.info(openid);
		}
	}

	//@Test
	public void createMenuTest() {
		List<Menu> menus = new ArrayList<Menu>();
		Menu m1 = new Menu();
		m1.setType(Menu.MENU_TYPE_VIEW);
		m1.setName("点菜");
		//String redirectUri = "http://124.212.121.36/index.html";
		String redirectUri = "http://124.212.121.36/card.html?mid=01058121104";
		m1.setUrl("http://124.212.121.36/cdn/wechat/dish.html?merchantId=01058121104&v=1.1");
		menus.add(m1);

		Menu m2 = new Menu();
		m2.setType(Menu.MENU_TYPE_VIEW);
		m2.setName("会员卡");
		//String redir = "http://124.212.121.36/wechat/user/entry";
		String redir = "http://124.212.121.36/wechat/user/entry";
		m2.setUrl("http://124.212.121.36/cdn/wechat/card.html?merchantId=01058121104&v=1.3");
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
		wechat.sendMediaMsg("o2FJ5jsz0F5nI50EUaMYxCzfVFFM", MsgType.image, "789X2RilxEVpe17cPCft88Gb7weD3_6dJ3ptArSP4w9H0b1bTFwB5XM5bfjGrJFm");
	}

	//@Test
	public void uploadMediaTest() {
		String mediaId = wechat.uploadMedia("thumb", new File("D:/Pictures/nexusS/IMG_20120211_132647.jpg"));
		log.info("upload media : " + mediaId);
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
		log.info(mid);
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
		log.info(ticket);
	}

	private void printMenu(List<Menu> menus) {
		log.info("===========");
		for(Menu m : menus) {
			if(null != m.getType()) log.info("type:" + m.getType());
			if(null != m.getName()) log.info("name:" + m.getName());
			if(null != m.getKey()) log.info("key:" + m.getKey());
			if(null != m.getUrl()) log.info("url:" + m.getUrl());
			if(null != m.getSubButton()) this.printMenu(m.getSubButton());

		}
	}

}
