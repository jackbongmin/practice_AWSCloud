package com.kyobo.product;

import static com.kyobo.product.QProduct.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

// 이름 규칙: 인터페이스 이름 + Impl → Spring Data가 ProductRepository에 붙여 준다
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

	private final JPAQueryFactory query;

	public ProductRepositoryCustomImpl(JPAQueryFactory query) {
		this.query = query;
	}

	@Override
	public Page<Product> search(ProductSearchCond cond, Pageable pageable) {
		List<Product> content = query.selectFrom(product)
				.where(nameContains(cond.name()), priceGoe(cond.minPrice()), priceLoe(cond.maxPrice()))
				.orderBy(product.number.asc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		// 3단계: count는 같은 조건으로 따로. 마지막 페이지 등에서는 실행을 건너뛴다
		JPAQuery<Long> count = query.select(product.count()).from(product)
				.where(nameContains(cond.name()), priceGoe(cond.minPrice()), priceLoe(cond.maxPrice()));
		return PageableExecutionUtils.getPage(content, pageable, count::fetchOne);
	}

	// 조건이 없으면 null → where()가 그 조건을 무시한다
	private BooleanExpression nameContains(String name) {
		return name == null || name.isBlank() ? null : product.name.contains(name);
	}

	private BooleanExpression priceGoe(Integer min) {
		return min == null ? null : product.price.goe(min);
	}

	private BooleanExpression priceLoe(Integer max) {
		return max == null ? null : product.price.loe(max);
	}

	@Override
	public List<Product> findByNameQ(String name) {
		return query.selectFrom(product)
				.where(product.name.eq(name))   // 교재 원본: product.price.eq(500)
				.fetch();
	}
}
