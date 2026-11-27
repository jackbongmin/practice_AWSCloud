package com.kyobo.product;

// 3단계: 업무 규칙 위반. 어떤 오류인지는 ErrorCode로 들고 다닌다
public class BusinessException extends RuntimeException {

	private final ErrorCode code;

	public BusinessException(ErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	public ErrorCode code() {
		return code;
	}
}
