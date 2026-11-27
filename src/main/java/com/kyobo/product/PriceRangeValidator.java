package com.kyobo.product;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PriceRangeValidator implements ConstraintValidator<PriceRange, ProductSearchCond> {

	@Override
	public boolean isValid(ProductSearchCond cond, ConstraintValidatorContext context) {
		// 한쪽이라도 비어 있으면 비교할 것이 없다 → 통과 (비어 있는지는 다른 규칙의 일)
		if (cond == null || cond.minPrice() == null || cond.maxPrice() == null) {
			return true;
		}
		return cond.minPrice() <= cond.maxPrice();
	}
}
