package com.townmc.mp;


import com.townmc.mp.aes.AesException;
import com.townmc.mp.aes.WXBizMsgCrypt;
import com.townmc.mp.json.JSONArray;
import com.townmc.mp.json.JSONObject;
import com.townmc.utils.Http;
import com.townmc.utils.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方开发者流程中使用到的帮助方法
 */
public class Component {
    private static final Log log = LogFactory.getLog(Component.class);

    private static final String COMPONENT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
    private static final String PRE_AUTH_CODE_URL = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token={0}";
    private static final String API_QUERY_AUTH_URL = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token={0}";
    private static final String API_AUTHORIZER_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/component/api_authorizer_token?component_access_token={0}";
    private static final String GET_ACCESSTOKEN_BY_CODE_URL = "https://api.weixin.qq.com/sns/oauth2/component/access_token?appid={0}&code={1}&grant_type=authorization_code&component_appid={2}&component_access_token={3}";
    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/component/jscode2session?appid={0}&js_code={1}&grant_type=authorization_code&component_appid={2}&component_access_token={3}";
    private static final String API_GET_AUTHORIZER_INFOl_URL = "https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info?component_access_token={0}";

    /**
     * 1. 在公众号第三方平台创建审核通过后，微信服务器会向其“授权事件接收URL”每隔10分钟定时推送component_verify_ticket <br />
     * 2. 当公众号对第三方平台进行授权、取消授权、更新授权后，微信服务器会向第三方平台方的授权事件接收URL（创建第三方平台时填写）推送相关通知。
     * 使用这个方法可以对推送的xml内容进行解析和解密,获得需要的几个参数 <br />
     * @param componentAppid
     * @param aesToken
     * @param aesKey
     * @param callBackBody
     * @return 请参看微信文档,map结构对应返回的json
     * @throws AesException
     * @throws DocumentException
     */
    public static Map<String, Object> callback(String componentAppid, String aesToken, String aesKey,
                                                     String callBackBody) throws AesException, DocumentException {

        WXBizMsgCrypt pc = new WXBizMsgCrypt(aesToken, aesKey, componentAppid);

        Document document = DocumentHelper.parseText(callBackBody);
        Element rootEle = document.getRootElement();
        String appid = rootEle.element("AppId").getText().trim();
        String encryptMsg = rootEle.element("Encrypt").getText();
        String encryptBody = pc.decrypt(encryptMsg);

        Document pushDoc = DocumentHelper.parseText(encryptBody);
        Element rootPush = pushDoc.getRootElement();
        String createTime = rootPush.element("CreateTime").getText().trim();
        String infoType = rootPush.element("InfoType").getText().trim();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("AppId", appid);
        result.put("CreateTime", createTime);
        result.put("InfoType", infoType);

        // 每过10分钟的ticket推送
        if("component_verify_ticket".equals(infoType)) { // 取消授权通知
            String componentVerifyTicket = rootPush.element("ComponentVerifyTicket").getText().trim();
            result.put("ComponentVerifyTicket", componentVerifyTicket);
        } else if("unauthorized".equals(infoType)) {
            String authorizerAppid = rootPush.element("AuthorizerAppid").getText().trim();
            result.put("AuthorizerAppid", authorizerAppid);
        } else if("authorized".equals(infoType)) { // 授权成功通知
            String authorizerAppid = rootPush.element("AuthorizerAppid").getText().trim();
            result.put("AuthorizerAppid", authorizerAppid);
            String authorizationCode = rootPush.element("AuthorizationCode").getText().trim();
            result.put("AuthorizationCode", authorizationCode);
            int authorizationCodeExpiredTime = Integer.valueOf(rootPush.element("AuthorizationCodeExpiredTime").getText().trim());
            result.put("AuthorizationCodeExpiredTime", authorizationCodeExpiredTime);
        } else if("updateauthorized".equals(infoType)) { // 授权更新通知
            String authorizerAppid = rootPush.element("AuthorizerAppid").getText().trim();
            result.put("AuthorizerAppid", authorizerAppid);
            String authorizationCode = rootPush.element("AuthorizationCode").getText().trim();
            result.put("AuthorizationCode", authorizationCode);
            int authorizationCodeExpiredTime = Integer.valueOf(rootPush.element("AuthorizationCodeExpiredTime").getText().trim());
            result.put("AuthorizationCodeExpiredTime", authorizationCodeExpiredTime);
        }

        return result;
    }

    /**
     * 获取第三方平台component_access_token
     *
     * 第三方平台compoment_access_token是第三方平台的下文中接口的调用凭据，也叫做令牌（component_access_token）。
     * 每个令牌是存在有效期（2小时）的，且令牌的调用不是无限制的，请第三方平台做好令牌的管理，在令牌快过期时（比如1小时50分）再进行刷新。
     * @param componentAppid
     * @param componentAppSecret
     * @param componentVerifyTicket
     * @return 请参看微信文档,map结构对应返回的json
     */
    public static Map<String, Object> apiComponentToken(String componentAppid, String componentAppSecret,
                                                           String componentVerifyTicket) {

        JSONObject paramJson = new JSONObject().put("component_appid", componentAppid)
                .put("component_appsecret", componentAppSecret)
                .put("component_verify_ticket", componentVerifyTicket);

        Http http = new Http();
        String re = http.post(COMPONENT_ACCESS_TOKEN_URL, paramJson.toString());
        http.close();
        log.debug("==== apiComponentToken response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }
        String newToken = reJson.getString("component_access_token");
        int expiresIn = reJson.getInt("expires_in");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("component_access_token", newToken);
        result.put("expires_in", expiresIn);
        return result;
    }

    /**
     * 用于获取预授权码。预授权码用于公众号授权时的第三方平台方安全验证。
     * @param componentAppid
     * @param componentAccessToken
     * @return 请参看微信文档,map结构对应返回的json
     */
    public static Map<String, Object> apiCreatePreAuthcode(String componentAppid, String componentAccessToken) {

        JSONObject paramJson = new JSONObject().put("component_appid", componentAppid);

        String url = MessageFormat.format(PRE_AUTH_CODE_URL, componentAccessToken);
        Http http = new Http();
        String re = http.post(url, paramJson.toString());
        http.close();
        log.debug("==== apiCreatePreAuthcode response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }

        String newToken = reJson.getString("pre_auth_code");
        int expiresIn = reJson.getInt("expires_in");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("pre_auth_code", newToken);
        result.put("expires_in", expiresIn);

        return result;
    }

    /**
     * 使用授权码换取授权公众号的授权信息，并换取authorizer_access_token和authorizer_refresh_token。
     * 授权码的获取，需要在用户在第三方平台授权页中完成授权流程后，在回调URI中通过URL参数提供给第三方平台方。
     * 请注意，由于现在公众号可以自定义选择部分权限授权给第三方平台，因此第三方平台开发者需要通过该接口来获取公众号具体授权了哪些权限，
     * 而不是简单地认为自己声明的权限就是公众号授权的权限。
     * @param componentAppid
     * @param authorizationCode
     * @param componentAccessToken
     * @return 请参看微信文档,map结构对应返回的json
     */
    public static Map<String, Object> apiQueryAuth(String componentAppid, String authorizationCode,
                                                   String componentAccessToken) {

        JSONObject paramJson = new JSONObject().put("component_appid", componentAppid).put("authorization_code", authorizationCode);

        String url = MessageFormat.format(API_QUERY_AUTH_URL, componentAccessToken);
        Http http = new Http();
        String re = http.post(url, paramJson.toString());
        http.close();
        log.debug("==== apiQueryAuth response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }

        JSONObject authInfoJson = reJson.getJSONObject("authorization_info");
        String authorizerAppid = authInfoJson.getString("authorizer_appid");
        String authorizerAccessToken = authInfoJson.getString("authorizer_access_token");
        int expiresIn = authInfoJson.getInt("expires_in");
        String authorizerRefreshToken = authInfoJson.getString("authorizer_refresh_token");
        JSONArray funcInfoArrJson = authInfoJson.getJSONArray("func_info");
        List<Map<String, Object>> funcInfoList = new ArrayList<Map<String, Object>>();
        for(int i = 0; i < funcInfoArrJson.length(); i++) {
            JSONObject o = funcInfoArrJson.getJSONObject(i);
            JSONObject funcscopeCategoryJson = o.getJSONObject("funcscope_category");
            int id = funcscopeCategoryJson.getInt("id");
            Map<String, Object> funcscopeCategory = new HashMap<String, Object>();
            funcscopeCategory.put("id", id);

            Map<String, Object> obj = new HashMap<String, Object>();
            obj.put("funcscope_category", funcscopeCategory);
            funcInfoList.add(obj);
        }

        Map<String, Object> authInfo = new HashMap<String, Object>();
        authInfo.put("authorizer_appid", authorizerAppid);
        authInfo.put("authorizer_access_token", authorizerAccessToken);
        authInfo.put("expires_in", expiresIn);
        authInfo.put("authorizer_refresh_token", authorizerRefreshToken);
        authInfo.put("func_info", funcInfoList);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("authorization_info", authInfo);

        return result;
    }


    /**
     * 该API用于在授权方令牌（authorizer_access_token）失效时，可用刷新令牌（authorizer_refresh_token）获取新的令牌。
     * 请注意，此处token是2小时刷新一次，开发者需要自行进行token的缓存，避免token的获取次数达到每日的限定额度
     * @param componentAppid 第三方平台appid
     * @param componentAccessToken
     * @param authorizerAppid 授权方appid
     * @param authorizerRefreshToken 授权方的刷新令牌，刷新令牌主要用于公众号第三方平台获取和刷新已授权用户的access_token，只会在授权时刻提供，请妥善保存。一旦丢失，只能让用户重新授权，才能再次拿到新的刷新令牌
     * @return 请参看微信文档,map结构对应返回的json
     */
    public static Map<String, Object> apiAuthorizerToken(String componentAppid, String componentAccessToken,
                                                         String authorizerAppid, String authorizerRefreshToken) {

        JSONObject paramJson = new JSONObject().put("component_appid", componentAppid)
                .put("authorizer_appid", authorizerAppid).put("authorizer_refresh_token", authorizerRefreshToken);

        String url = MessageFormat.format(API_AUTHORIZER_TOKEN_URL, componentAccessToken);
        Http http = new Http();
        String re = http.post(url, paramJson.toString());
        http.close();
        log.debug("==== apiAuthorizerToken response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }

        String authorizerAccessToken = reJson.getString("authorizer_access_token");
        int expiresIn = reJson.getInt("expires_in");
        String newAuthorizerRefreshToken = reJson.getString("authorizer_refresh_token");

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("authorizer_access_token", authorizerAccessToken);
        result.put("expires_in", expiresIn);
        result.put("authorizer_refresh_token", newAuthorizerRefreshToken);

        return result;
    }

    /**
     * 代公众号发起网页授权步骤中通过code换取access_token以及openid
     * @param componentAppid
     * @param authorizerAppid
     * @param code
     * @param componentAccessToken
     * @return Map {"access_token":"expires_in",0:"refresh_token","openid":"","":"scope"}
     */
    public static Map<String, Object> getAccessTokenByCode(String componentAppid, String authorizerAppid, String code,
                                                           String componentAccessToken) {

        String url = MessageFormat.format(GET_ACCESSTOKEN_BY_CODE_URL, authorizerAppid, code, componentAppid, componentAccessToken);
        Http http = new Http();
        String re = http.get(url);
        http.close();
        log.debug("==== getAccessTokenByCode response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }

        String accessToken = reJson.getString("access_token");
        int expiresIn = reJson.getInt("expires_in");
        String refreshToken = reJson.getString("refresh_token");
        String openid = reJson.getString("openid");
        String scope = reJson.getString("scope");

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("access_token", accessToken);
        result.put("expires_in", expiresIn);
        result.put("refresh_token", refreshToken);
        result.put("openid", openid);
        result.put("scope", scope);

        return result;
    }

    /**
     * 第三方平台代替小程序实现登录,使用登录凭证 code 以及第三方平台的component_access_token 获取 session_key 和 openid
     * @param componentAppid
     * @param authorizerAppid
     * @param code
     * @param componentAccessToken
     * @return
     */
    public static Map<String, Object> jscode2session(String componentAppid, String authorizerAppid, String code,
                                                     String componentAccessToken) {

        String url = MessageFormat.format(JSCODE2SESSION_URL, authorizerAppid, code, componentAppid, componentAccessToken);
        Http http = new Http();
        String re = http.get(url);
        http.close();
        log.debug("==== jscode2session response : " + re);
        JSONObject reJson = new JSONObject(re);
        if(!reJson.isNull("errcode") && 0 != reJson.getInt("errcode")) {
            throw new MpException("component_access_error", reJson.getInt("errcode") + "." + reJson.getString("errmsg"));
        }

        String openid = reJson.getString("openid");
        String sessionKey = reJson.getString("session_key");

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("openid", openid);
        result.put("session_key", sessionKey);

        return result;
    }

    /**
     * 获取授权方的帐号基本信息，公众号获取
     * @param componentAppid
     * @param authorizerAppid
     * @param componentAccessToken
     * @return Map
     *
     */
    public static Map<String, Object> apiGetAuthorizerInfo(String componentAppid, String authorizerAppid,
                                                           String componentAccessToken) {
        String url = MessageFormat.format(API_GET_AUTHORIZER_INFOl_URL, componentAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("component_appid", componentAppid);
        param.put("authorizer_appid", authorizerAppid);
        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

}
