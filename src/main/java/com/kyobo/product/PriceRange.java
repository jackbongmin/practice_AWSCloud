package com.kyobo.product;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// 2단계: 직접 만든 검증 어노테이션 (교재 예제 10.7 @Telephone과 같은 구조)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
public @interface PriceRange {

	String message() default "최소 가격이 최대 가격보다 클 수 없습니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
