package com.townmc.mp;

import com.townmc.utils.Http;
import com.townmc.utils.JsonUtil;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方代小程序实现业务相关接口。
 * 接口的返回参数均为Map，对应的格式请参照文档：
 * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1489144594_DhNoV&token=&lang=zh_CN
 */
public class Wxapp {
    private static final String MODIFY_DOMAIN_URL = "https://api.weixin.qq.com/wxa/modify_domain?access_token={0}";
    private static final String BIND_TESTER_URL = "https://api.weixin.qq.com/wxa/bind_tester?access_token={0}";
    private static final String UNBIND_TESTER_URL = "https://api.weixin.qq.com/wxa/unbind_tester?access_token={0}";
    private static final String COMMIT_URL = "https://api.weixin.qq.com/wxa/commit?access_token={0}";
    private static final String GET_QRCODE_URL = "https://api.weixin.qq.com/wxa/get_qrcode?access_token={0}";
    private static final String GET_CATEGORY_URL = "https://api.weixin.qq.com/wxa/get_category?access_token={0}";
    private static final String GET_PAGE_URL = "https://api.weixin.qq.com/wxa/get_page?access_token={0}";
    private static final String SUBMIT_AUDIT_URL = "https://api.weixin.qq.com/wxa/submit_audit?access_token={0}";
    private static final String GET_AUDITS_TATUS_URL = "https://api.weixin.qq.com/wxa/get_auditstatus?access_token={0}";
    private static final String GET_LATEST_AUDITS_TATUS_URL = "https://api.weixin.qq.com/wxa/get_latest_auditstatus?access_token={0}";
    private static final String RELEASE_URL = "https://api.weixin.qq.com/wxa/release?access_token={0}";
    private static final String CHANGE_VISITSTATUS_URL = "https://api.weixin.qq.com/wxa/change_visitstatus?access_token={0}";
    private static final String LIST_TEMPLATE_MSG_URL = "https://api.weixin.qq.com/cgi-bin/wxopen/template/library/list?access_token={0}";
    private static final String GET_TEMPLATE_MSG_INFO_URL = "https://api.weixin.qq.com/cgi-bin/wxopen/template/library/get?access_token={0}";
    private static final String ADD_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/wxopen/template/add?access_token={0}";
    private static final String LIST_EXISTS_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/wxopen/template/list?access_token={0}"; //获取帐号下已存在的模板列表
    private static final String DEL_EXISTS_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/wxopen/template/del?access_token={0}"; //删除帐号下的某个模板

    /**
     * 修改服务器地址
     *
     * @param authorizerAccessToken 第三方平台获取到的该小程序授权的authorizer_access_token
     * @param action add添加, delete删除, set覆盖, get获取。当参数是get时不需要填四个域名字段
     * @param requestdomain request合法域名，当action参数是get时不需要此字段
     * @param wsrequestdomain socket合法域名，当action参数是get时不需要此字段
     * @param uploaddomain uploadFile合法域名，当action参数是get时不需要此字段
     * @param downloaddomain downloadFile合法域名，当action参数是get时不需要此字段
     * @return
     */
    public static Map<String, Object> modifyDomain(String authorizerAccessToken, String action, List<String> requestdomain,
                                                   List<String> wsrequestdomain, List<String> uploaddomain, List<String> downloaddomain) {

        String url = MessageFormat.format(MODIFY_DOMAIN_URL, authorizerAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("action", action);
        param.put("requestdomain", requestdomain);
        param.put("wsrequestdomain", wsrequestdomain);
        param.put("uploaddomain", uploaddomain);
        param.put("downloaddomain", downloaddomain);
        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 绑定微信用户为小程序体验者
     * @param wechatid
     * @return
     */
    public static Map<String, Object> bindTester(String authorizerAccessToken, String wechatid) {
        String url = MessageFormat.format(BIND_TESTER_URL, authorizerAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("wechatid", wechatid);
        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 解除绑定小程序的体验者
     * @param wechatid
     * @return
     */
    public static Map<String, Object> unbindTester(String authorizerAccessToken, String wechatid) {
        String url = MessageFormat.format(UNBIND_TESTER_URL, authorizerAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("wechatid", wechatid);
        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 为授权的小程序帐号上传小程序代码
     * @param authorizerAccessToken
     * @param templateId
     * @param extJson {"extAppid":"", "ext":{"k1":"v1", k2":"v2"}}
     * @param userVersion
     * @param userDesc
     * @return
     */
    public static Map<String, Object> commitTemplate(String authorizerAccessToken, String templateId, String extJson,
                                                     String userVersion, String userDesc) {
        String url = MessageFormat.format(COMMIT_URL, authorizerAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("template_id", templateId);
        param.put("ext_json", extJson);
        param.put("user_version", userVersion);
        param.put("user_desc", userDesc);
        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 获取体验小程序的体验二维码
     * @param authorizerAccessToken
     * @return
     */
    public static String getQrcodeImgUrl(String authorizerAccessToken) {
        String url = MessageFormat.format(GET_QRCODE_URL, authorizerAccessToken);
        return url;
    }

    /**
     * 获取授权小程序帐号的可选类目
     * @param authorizerAccessToken
     * @return
     */
    public static Map<String, Object> getCategory(String authorizerAccessToken) {
        String url = MessageFormat.format(GET_CATEGORY_URL, authorizerAccessToken);
        Http http = new Http();
        String resp = http.get(url);
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 获取小程序的第三方提交代码的页面配置（仅供第三方开发者代小程序调用）
     * @param authorizerAccessToken
     * @return
     */
    public static Map<String, Object> getPage(String authorizerAccessToken) {
        String url = MessageFormat.format(GET_PAGE_URL, authorizerAccessToken);
        Http http = new Http();
        String resp = http.get(url);
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 将第三方提交的代码包提交审核（仅供第三方开发者代小程序调用）
     * @param authorizerAccessToken
     * @param itemList
     * @return
     */
    public static Map<String, Object> submitAudit(String authorizerAccessToken, List<SubmitAuditItem> itemList) {
        String url = MessageFormat.format(SUBMIT_AUDIT_URL, authorizerAccessToken);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("item_list", itemList);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 查询某个指定版本的审核状态（仅供第三方代小程序调用）
     * @param authorizerAccessToken 第三方平台获取到的该小程序授权的authorizer_access_token
     * @param auditid 提交审核时获得的审核id
     * @return
     */
    public static Map<String, Object> getAuditStatus(String authorizerAccessToken, Integer auditid) {
        String url = MessageFormat.format(GET_AUDITS_TATUS_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("auditid", auditid);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 查询最新一次提交的审核状态（仅供第三方代小程序调用）
     * @param authorizerAccessToken 第三方平台获取到的该小程序授权的authorizer_access_token
     * @return
     */
    public static Map<String, Object> getLatestAuditStatus(String authorizerAccessToken) {
        String url = MessageFormat.format(GET_LATEST_AUDITS_TATUS_URL, authorizerAccessToken);

        Http http = new Http();
        String resp = http.get(url);
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 发布已通过审核的小程序（仅供第三方代小程序调用）
     * @param authorizerAccessToken 第三方平台获取到的该小程序授权的authorizer_access_token
     * @return
     */
    public static Map<String, Object> release(String authorizerAccessToken) {
        String url = MessageFormat.format(RELEASE_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 修改小程序线上代码的可见状态（仅供第三方代小程序调用）
     * @param authorizerAccessToken 第三方平台获取到的该小程序授权的authorizer_access_token
     * @param action 设置可访问状态，发布后默认可访问，close为不可见，open为可见
     * @return
     */
    public static Map<String, Object> changeVisitStatus(String authorizerAccessToken, String action) {
        String url = MessageFormat.format(CHANGE_VISITSTATUS_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("action", action);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 获取小程序模板库标题列表
     * @param authorizerAccessToken
     * @param offset
     * @param count
     * @return
     * {
     *   "errcode":0,
     *   "errmsg":"ok",
     *   "list":[
     *      {"id":"AT0002","title":"购买成功通知"}
     *   ]
     * }
     */
    public static Map<String, Object> listTemplateMsg(String authorizerAccessToken, int offset, int count) {
        String url = MessageFormat.format(LIST_TEMPLATE_MSG_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("offset", offset);
        param.put("count", count);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 获取模板库某个模板标题下关键词库
     * @param authorizerAccessToken
     * @param tplId
     * @return
     * {
     *   "errcode": 0,
     *   "errmsg": "ok",
     *   "id": "AT0002",
     *   "title": "购买成功通知",
     *   "keyword_list": [
     *      {
     *      "keyword_id": 3,
     *      "name": "购买地点",
     *      "example": "TIT造舰厂"
     *      }
     *   ]
     * }
     */
    public static Map<String, Object> getTemplateMsgInfo(String authorizerAccessToken, String tplId) {
        String url = MessageFormat.format(GET_TEMPLATE_MSG_INFO_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", tplId);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 组合模板并添加至帐号下的个人模板库
     * @param authorizerAccessToken
     * @param tplId
     * @param keyIdList
     * @return
     * {
     *   "errcode": 0,
     *   "errmsg": "ok",
     *   "template_id": "wDYzYZVxobJivW9oMpSCpuvACOfJXQIoKUm0PY397Tc"
     * }
     */
    public static Map<String, Object> addTemplate(String authorizerAccessToken, String tplId, String[] keyIdList) {
        String url = MessageFormat.format(ADD_TEMPLATE_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", tplId);
        param.put("keyword_id_list", keyIdList);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 获取帐号下已存在的模板列表
     * @param authorizerAccessToken
     * @param offset
     * @param count
     * @return
     *   {
     *      "errcode": 0,
     *      "errmsg": "ok",
     *      "list": [
     *          {
     *          "template_id": "wDYzYZVxobJivW9oMpSCpuvACOfJXQIoKUm0PY397Tc",
     *          "title": "购买成功通知",
     *          "content": "购买地点{{keyword1.DATA}}\n购买时间{{keyword2.DATA}}\n物品名称{{keyword3.DATA}}\n",
     *          "example": "购买地点：TIT造舰厂\n购买时间：2016年6月6日\n物品名称：咖啡\n"
     *          }
     *      ]
     *  }
     */
    public static Map<String, Object> listExistsTemplate(String authorizerAccessToken, int offset, int count) {
        String url = MessageFormat.format(LIST_EXISTS_TEMPLATE_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("offset", offset);
        param.put("count", count);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    /**
     * 删除帐号下的某个模板
     * @param authorizerAccessToken
     * @param tplMsgId
     * @return
     *   {
     *       "errcode":0,
     *       "errmsg":"ok"
     *   }
     */
    public static Map<String, Object> delExistsTemplate(String authorizerAccessToken, String tplMsgId) {
        String url = MessageFormat.format(DEL_EXISTS_TEMPLATE_URL, authorizerAccessToken);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("template_id", tplMsgId);

        Http http = new Http();
        String resp = http.post(url, JsonUtil.object2Json(param));
        return JsonUtil.json2Object(resp, Map.class);
    }

    public static class SubmitAuditItem implements Serializable {
        private String address; // 小程序的页面，可通过“获取小程序的第三方提交代码的页面配置”接口获得
        private String tag; // 小程序的标签，多个标签用空格分隔，标签不能多于10个，标签长度不超过20
        private String first_class; // 一级类目名称，可通过“获取授权小程序帐号的可选类目”接口获得
        private String second_class; // 二级类目(同上)
        private String third_class; // 三级类目(同上)
        private Integer first_id; // 一级类目的ID，可通过“获取授权小程序帐号的可选类目”接口获得
        private Integer second_id; // 二级类目的ID(同上)
        private Integer third_id;	// 三级类目的ID(同上)
        private String title; // 小程序页面的标题,标题长度不超过32

        public String getThird_class() {
            return third_class;
        }

        public void setThird_class(String third_class) {
            this.third_class = third_class;
        }

        public Integer getThird_id() {
            return third_id;
        }

        public void setThird_id(Integer third_id) {
            this.third_id = third_id;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getTag() {
            return tag;
        }
        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getFirst_class() {
            return first_class;
        }

        public void setFirst_class(String first_class) {
            this.first_class = first_class;
        }

        public String getSecond_class() {
            return second_class;
        }

        public void setSecond_class(String second_class) {
            this.second_class = second_class;
        }

        public Integer getFirst_id() {
            return first_id;
        }

        public void setFirst_id(Integer first_id) {
            this.first_id = first_id;
        }

        public Integer getSecond_id() {
            return second_id;
        }

        public void setSecond_id(Integer second_id) {
            this.second_id = second_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
