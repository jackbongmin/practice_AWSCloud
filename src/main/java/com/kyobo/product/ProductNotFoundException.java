package com.kyobo.product;

// 10_06 3단계: 업무 예외의 한 종류. 상태 코드는 ErrorCode가 정한다 (10_01의 @ResponseStatus 대신)
public class ProductNotFoundException extends BusinessException {

	public ProductNotFoundException(Long number) {
		super(ErrorCode.PRODUCT_NOT_FOUND, "상품 없음: " + number);
	}
}
