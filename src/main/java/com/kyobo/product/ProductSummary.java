package com.kyobo.product;

// 4단계: 이름·가격만 필요할 때. Spring Data가 select name, price만 하도록 만든다
public interface ProductSummary {

	String getName();

	Integer getPrice();
}
