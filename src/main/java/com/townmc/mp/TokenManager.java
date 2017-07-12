package com.townmc.mp;

import com.townmc.mp.model.Token;

/**
 * token管理接口
 * access_token是公众号的全局唯一票据，公众号调用各接口时都需使用access_token。
 * 正常情况下access_token有效期为7200秒，重复获取将导致上次获取的access_token失效。
 * 由于获取access_token的api调用次数非常有限，所以开发者必须全局存储与更新access_token。
 * 
 * token的数据库存储是必须实现的。appid为主键
 * 同时为提高效率，建议对缓存的接口也进行实现
 * 
 * @author meng
 *
 */
public interface TokenManager {

	/**
	 * 存储token
	 * @param token
	 */
	public void toStorage(Token token);
	
	public Token get(String appid);
	
}
