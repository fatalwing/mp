# mp
微信公众号开发接口交互封装  

微信公众号开发分两种方式，一种是开发者直接接入方式，一种是第三方平台授权方式。关于这两种方式的介绍这里不做说明。

## 开发者接入方式
使用开发者接入方式需要登录公众号的MP后台获得几个参数。相关参数在“开发->基本配置”页面中。
* 开发者ID(AppID)
* 开发者密码(AppSecret)
* 令牌(Token)
* 消息加解密密钥(EncodingAESKey)
* 原始ID `原始id请在“设置->公众号设置”中获得`

> 开发者模式在接入前需要配置事件推送的服务器url，可以参照[微信文档](https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421135319) 

下面介绍几个主要使用到的类和接口
### com.townmc.mp.model.Token
在与微信接口进行交互的过程当中，access_token是开发者访问接口的全局唯一票据，定义这个Token对象主要用于下面要实现的一个重要接口TokenManager

### com.townmc.mp.TokenManager
你需要做一个这个接口的实现类，该接口就两个方法
```
    public void toStorage(Token token);
    public Token get(String appid);
```
保存好Token对象，并且能通过appid取到保存的Token对象。  
至于你是使用数据存储还是存在缓存中，请随意。只要能保证能存能取即可。

### com.townmc.mp.DevWechat
做好了上面那些准备工作之后，最重要的一个类DevWechat就可以使用了，这个类是com.townmc.mp.Wechat的一个实现。  
```
    String appId = "wxabcdefghijkl123456"; // 开发者id
    String appSecret = "e922cc385e1b90fbbb3c35cb3b8499c4"; // 开发者密码
    TokenManager tokenManager = xxx; // 前面提到的TokenManager接口的实现
    
    Wechat wechat = new DevWechat(appId, appSecret, tokenManager);

```
实例化了Wechat对象之后，就可以操作需要用到的大部分微信公众号接口了：
```
    /**
     * 给某个用户发送文本消息
     * @param openid 消息接收者的openid
     * @param text 文本消息内容
     */
    public void sendTextMsg(String openid, String text);

    /**
     * 给某个用户发送多媒体消息
     * @param openid 消息接收者的openid
     * @param mediaType 多媒体消息的类型 image|voice|video
     * @param mediaId 多媒体消息id
     */
    public void sendMediaMsg(String openid, MsgType mediaType, String mediaId);

    /**
     * 给某个用户发送多条图文消息
     * @param openid 消息接收者的openid
     * @param news
     */
    public void sendNewsMsg(String openid, List<Article> news);

    /**
     * 高级群发接口
     * @param openids
     * @param mediaId
     */
    public void sendMpNewsMsg(List<String> openids, String mediaId);
    
    /**
     * 高级群发接口
     * @param openids
     * @param mediaId
     * @param type
     */
    public void sendMpNewsMsg(List<String> openids, String mediaId, String type);
    
    /**
     * 获取用户基本信息
     * @param openid 用户的openid
     * @return
     */
    public MpUser getUser(String openid);

    /**
     * 通过code换取openid
     * @param code 微信网页回调地址是携带的code参数
     * @return
     */
    public String getOpenidByCode(String code);

    /**
     * 获取关注者列表
     * @param nextOpenid 第一个拉取的OPENID，为空默认从头开始拉取
     * @return
     */
    public MpUserList getUserList(String nextOpenid);

    /**
     * 微信第三方平台跳转 可获得code的跳转url地址
     * 
     * @param redirectUrl
     * @param state
     * @param componentAppid
     * @return
     */
    public String redirectUrl(String redirectUrl, String state, String componentAppid);
    
    /**
     * 创建自定义菜单
     * @param menus
     */
    public void createMenu(List<Menu> menus);
    
    /**
     * 查询自定义菜单的结构
     * @return
     */
    public List<Menu> getMenu();
    
    /**
     * 删除自定义菜单
     */
    public void deleteMenu();

    /**
     * 响应微信推送过来的通知
     * @param msgXml String
     * @param handler MsgHandler
     */
    public Map<String, Object> receiveMsg(String msgXml, MsgHandler handler);

    /**
     * 上传多媒体消息
     * @param type 图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param file
     * @return media_id
     */
    public String uploadMedia(String type, File file);
    
    /**
     * 上传图文消息素材用于高级群发接口
     * @param articles
     * @return
     */
    public String uploadNews(List<AdvancedArticle> articles);
    
    /**
     * 利用高级群发接口，发送多条图文消息
     * @param openids
     * @param mediaId
     */
    public void sendNewsAdvanced(List<String> openids, String mediaId);
    
    /**
     * 利用高级群发接口，发送各类型消息
     * @param openids
     * @param mediaId
     * @param type(text:文本、mpnews:图文、voice:语音、image:图片)
     */
    public void sendNewsAdvancedByType(List<String> openids, String mediaId, String type);
    
    /**
     * 创建参数的永久二维码tiket
     * @param sceneId 扫描关注事件返回的eventKey
     * @return ticket
     */
    public String createQrCode(int sceneId);

    /**
     * 创建参数的永久二维码tiket
     * @param sceneStr 扫描关注事件返回的eventKey
     * @return ticket
     */
    public String createQrCode(String sceneStr);
    
    /**
     * 创建带参数的临时性二维码ticket
     * @param sceneId 扫描关注事件返回的eventKey
     * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过1800
     * @return ticket
     */
    public String createExpireQrCode(int sceneId, int expireSeconds);

    /**
     * 创建带参数的临时性二维码ticket
     * @param sceneStr 扫描关注事件返回的eventKey
     * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过1800
     * @return ticket
     */
    public String createExpireQrCode(String sceneStr, int expireSeconds);
    
    /**
     * 通过ticket获得二维码展示地址
     * @param ticket
     * @return 
     */
    public String showQrCodeUrl(String ticket);

    /**
     * 从行业模板库选择模板到帐号后台，获得模板ID的过程可在MP中完成。为方便第三方开发者，提供通过接口调用的方式来获取模板ID
     * @param templateIdShort 模板库中模板的编号，有“TM**”和“OPENTMTM**”等形式
     * @return template_id
     */
    public String apiAddTemplate(String templateIdShort);
    
    /**
     * 
     * 给某个用户发送模版消息
     * @param openid 消息接收者的openid
     * @param templateId
     * @param jumpUrl
     * @param paramList
     */
    public boolean sendTemplateMsg(String openid, String templateId, String jumpUrl, List<TemplateParam> paramList);
    
    /**
     * 统一下单获取prepay_id
     * 返回appId、timeStamp、nonceStr、package、signType、paySign
     * @param unifiedorder 统一下单参数
     * @param key  商户的key
     */
    public Map<String, Object> unifiedOrder(Unifiedorder unifiedorder, String key);
    
    /**
     * 获得jsSDK签名
     * @return
     */
    public String getJsApiTicket();

    /**
     * 微信支付的订单查询
     * @param orderQuery
     * @param key 商户的key
     * @return
     */
    public Map<String, Object> queryOrder(OrderQuery orderQuery, String key);
    
    public Map<String, Object> sendRedPack(RedPack redPack,String keyFile, String keyPassWord);

    /**
     * 获得调用jssdk的配置数据
     * @param appId
     * @param url
     * @return
     */
    public Map<String, Object> getJsConfig(String appId,String url);

    /**
     * 微信支付退款
     * @param refund
     * @param keyFile
     * @param keyPassWord
     * @param apiKey 商户的key
     * @return
     */
    public Map<String, Object> refund(Refund refund, String keyFile, String keyPassWord, String apiKey);
```

