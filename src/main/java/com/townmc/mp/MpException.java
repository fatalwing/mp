package com.townmc.mp;

/**
 * 
 * @author liumengying
 *
 */
public class MpException extends RuntimeException {

	private static final long serialVersionUID = -7980892655400414209L;
	
	protected String errorCode;
	protected String errorMsg;
	
	public MpException(String errorCode) {
		super(errorCode);
		
		this.errorCode = errorCode;
		this.errorMsg = "";
	}
	
	public MpException(String errorCode, String errorMsg) {
		super(errorMsg);
		
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public MpException(String errorCode, String errorMsg, String jumpTo) {
		super(errorMsg);
		
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
	public MpException(String errorCode, String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
