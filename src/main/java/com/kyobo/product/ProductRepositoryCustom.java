package com.kyobo.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 2단계: 메서드 이름으로 만들기 어려운 조회는 직접 구현한다 (구현: ProductRepositoryCustomImpl)
public interface ProductRepositoryCustom {

	Page<Product> search(ProductSearchCond cond, Pageable pageable);

	// 5단계: 교재 QProductRepositorySupport.findByName을 고친 판
	List<Product> findByNameQ(String name);
}
