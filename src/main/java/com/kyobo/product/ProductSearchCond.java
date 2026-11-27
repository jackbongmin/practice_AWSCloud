package com.kyobo.product;

import jakarta.validation.constraints.PositiveOrZero;

// 2단계: 검색 조건. 셋 다 비어 있을 수 있다 (null이면 그 조건은 빼고 검색)
// 10_06 2단계: 칸 하나씩이 아니라 두 칸의 관계(최소 <= 최대)를 검사하는 직접 만든 규칙
@PriceRange
public record ProductSearchCond(String name,
		@PositiveOrZero(message = "최소 가격은 0 이상이어야 합니다") Integer minPrice,
		@PositiveOrZero(message = "최대 가격은 0 이상이어야 합니다") Integer maxPrice) {
}
