package com.townmc.mp.wxpay.service;

import com.townmc.mp.wxpay.common.Configure;
import com.townmc.mp.wxpay.common.XMLParser;
import com.townmc.mp.wxpay.protocol.pay_protocol.UnifiedOrderReqData;
import com.townmc.mp.wxpay.protocol.pay_protocol.UnifiedOrderResData;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

/**
 * Created by meng on 2015-7-6.
 */
public class UnifiedOrderService extends BaseService {
    public UnifiedOrderService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        super(Configure.UNIFIED_ORDER_API);
    }

    /**
     * ����ͳһ�µ�����������
     * @param unifiedOrderReqData
     * @return
     * @throws Exception
     */
    public UnifiedOrderResData request(UnifiedOrderReqData unifiedOrderReqData) throws Exception {

        String responseString = sendPost(unifiedOrderReqData);
        Map<String,Object> re = XMLParser.getMapFromXML(responseString);

        UnifiedOrderResData res = new UnifiedOrderResData();

        res.setReturn_code((String)re.get("return_code"));
        if(!"SUCCESS".equals(res.getReturn_code())) {
            res.setReturn_code((String) (re.get("return_code")));
            res.setReturn_msg((String) (re.get("return_msg")));
            return res;
        } else {
            res.setAppid((String)re.get("appid"));
            res.setMch_id((String)re.get("mch_id"));
            res.setDevice_info((String)re.get("device_info"));
            res.setNonce_str((String)re.get("nonce_str"));
            res.setSign((String)re.get("sign"));
            res.setResult_code((String)re.get("result_code"));
            if(!"SUCCESS".equals(res.getResult_code())) {
                res.setErr_code((String)re.get("err_code"));
                res.setErr_code_des((String)re.get("err_code_des"));
                return res;
            } else {
                res.setTrade_type((String)re.get("trade_type"));
                res.setPrepay_id((String) re.get("prepay_id"));
                res.setCode_url((String)re.get("code_url"));
                return res;
            }
        }

    }

}
