package com.townmc.mp.model;

import java.io.Serializable;
import java.util.List;

/**
 * 关注者列表
 * @author meng
 *
 */
public class MpUserList implements Serializable {

	private static final long serialVersionUID = -130118038909685580L;
	private int total; // 关注该公众账号的总用户数
	private int count; // 拉取到的OPENID个数，最大值为10000
	private List<String> openids; // 列表数据，OPENID的列表
	private String nextOpenid; // 拉取列表的后一个用户的OPENID，数量超过10000时，可用于下一次拉取的参数

	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<String> getOpenids() {
		return openids;
	}
	public void setOpenids(List<String> openids) {
		this.openids = openids;
	}
	public String getNextOpenid() {
		return nextOpenid;
	}
	public void setNextOpenid(String nextOpenid) {
		this.nextOpenid = nextOpenid;
	}

}
