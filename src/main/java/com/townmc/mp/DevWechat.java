package com.townmc.mp;

import com.townmc.mp.json.JSONObject;
import com.townmc.mp.model.Token;
import com.townmc.utils.Http;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供给开发者模式时使用
 * Created by meng on 2017/7/13.
 */
public class DevWechat extends DefaultWechat implements Wechat {
    private static final Logger log = LoggerFactory.getLogger(DevWechat.class);

    private String secret;

    public DevWechat(String appId, String secret, TokenManager tokenManage) {
        this.appid = appId;
        this.secret = secret;
        this.tokenManager = tokenManage;
    }

    /**
     * 获得access_token，与微信接口交互的凭证
     * @return
     */
    public String getAccessToken() {
        Token token = tokenManager.get(this.appid);

        // 如果token超过定义的刷新的时间了，重新获取
        if(null == token || null == token.getAccessToken() || "".equals(token.getAccessToken()) ||
                token.getExpireTime().getTime() - System.currentTimeMillis() < 360000) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("grant_type", "client_credential");
            params.put("appid", this.appid.trim());
            params.put("secret", this.secret.trim());

            log.debug("get access token form weixin. appid:" + this.appid + ". secret:" + this.secret);

            Http http = new Http();
            String responseStr = http.get(GET_TOKEN, params);
            http.close();

            if (responseStr == null || "".equals(responseStr)) {
                throw new MpException("get access token error! reson: responseStr is null, check secret space in tipdianka and dianka db?");
            }

            JSONObject json = new JSONObject(responseStr);

            if(!json.isNull("errcode")) {
                log.debug("getAccessTokenResult="+json.toString());
                throw new MpException("get access token error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
            }

            String tokenValue = json.getString("access_token");
            token = new Token();
            token.setAppid(this.appid);
            token.setAccessToken(tokenValue);
            token.setUpdateTime(new Date());
            int expiresIn = json.getInt("expires_in");
            token.setExpireTime(new Date(System.currentTimeMillis() + expiresIn*1000));

            tokenManager.toStorage(token);

        }

        return token.getAccessToken();
    }

    /**
     * 通过code换取openid
     * @param code 微信网页回调地址是携带的code参数
     * @return
     */
    public String getOpenidByCode(String code) {
        if(null == code) {
            throw new MpException("parameter code is null!");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", this.appid.trim());
        params.put("secret", this.secret.trim());
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        Http http = new Http();
        String responseStr = http.get(GET_OPENID_BY_CODE, params);

        JSONObject json = new JSONObject(responseStr);

        if(!json.isNull("errcode") && 0 != json.getInt("errcode")) {
            throw new MpException("get user info error! reson: " + json.getInt("errcode") + ". " + json.getString("errmsg"));
        }

        String openid = json.getString("openid");

        log.debug("get openid by code. code:" + code + ". openid:" + openid);

        return openid;
    }

    public String redirectUrl(String redirectUrl, String state) {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect";
        try {
            redirectUrl = URLEncoder.encode(redirectUrl,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, this.appid, redirectUrl, "snsapi_base", state);
    }

}
