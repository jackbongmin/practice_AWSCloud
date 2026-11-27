package com.kyobo.product;

import org.springframework.http.HttpStatus;

// 3단계: 오류 종류 목록. 프런트는 메시지 문장이 아니라 이 code로 분기한다
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST),       // 칸 검증 실패
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST),   // 타입 변환·누락·JSON 파싱·정렬 칸
	NO_SUCH_API(HttpStatus.NOT_FOUND),           // 없는 주소
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED), // 주소는 있는데 그 메서드는 없음
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND),
	PRODUCT_IN_STOCK(HttpStatus.CONFLICT),       // 업무 규칙 위반
	REVIEW_DUPLICATE(HttpStatus.CONFLICT),       // 10_09: 한 사람이 한 상품에 리뷰 하나
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

	private final HttpStatus status;

	ErrorCode(HttpStatus status) {
		this.status = status;
	}

	public HttpStatus status() {
		return status;
	}
}
