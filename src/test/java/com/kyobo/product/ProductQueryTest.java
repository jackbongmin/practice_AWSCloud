package com.kyobo.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;

// 1~4단계: 같은 상품 8개로 쿼리 메서드의 결과를 확인한다
@DataJpaTest
@Import(QuerydslConfig.class) // 저장소에 Querydsl 구현이 붙었으므로 공장 빈이 필요하다
class ProductQueryTest {

	@Autowired
	ProductRepository products;

	@Autowired
	EntityManager em;

	@BeforeEach
	void setUp() {
		products.saveAll(List.of(
				new Product("펜", 1000, 10),
				new Product("펜", 1500, 5),
				new Product("펜", 2000, 0),
				new Product("볼펜", 800, 30),
				new Product("형광펜", 1200, 12),
				new Product("공책", 3000, 7),
				new Product("연필", 500, 50),
				new Product("필통", 5000, 2)));
	}

	static List<String> names(List<Product> list) {
		return list.stream().map(Product::getName).toList();
	}

	static List<Integer> prices(List<Product> list) {
		return list.stream().map(Product::getPrice).toList();
	}

	// 1단계
	@Test
	void 주제_키워드() {
		assertThat(products.findByName("펜")).hasSize(3);
		assertThat(products.existsByName("지우개")).isFalse();
		assertThat(products.countByName("펜")).isEqualTo(3);
		assertThat(products.findFirstByNameOrderByPriceAsc("펜")).get()
				.extracting(Product::getPrice).isEqualTo(1000);
		assertThat(prices(products.findTop3ByOrderByPriceDesc())).containsExactly(5000, 3000, 2000);
	}

	// 2단계
	@Test
	void 조건_키워드() {
		assertThat(products.findByNameNot("펜")).hasSize(5);
		assertThat(prices(products.findByNameAndPriceLessThan("펜", 1500))).containsExactly(1000);
		assertThat(prices(products.findByPriceBetween(1000, 2000))).containsExactlyInAnyOrder(1000, 1500, 2000, 1200);
		assertThat(names(products.findByNameContaining("펜"))).containsExactlyInAnyOrder("펜", "펜", "펜", "볼펜", "형광펜");
	}

	// 3단계
	@Test
	void 정렬() {
		List<Product> sorted = products.findByNameContaining("펜",
				Sort.by(Sort.Order.desc("price"), Sort.Order.asc("name")));
		assertThat(prices(sorted)).containsExactly(2000, 1500, 1200, 1000, 800);
	}

	@Test
	void 페이지는_0부터() {
		Page<Product> page = products.findByNameContaining("펜", PageRequest.of(1, 2, Sort.by("price")));

		// 가격순 800, 1000 | 1200, 1500 | 2000 → 두 번째 페이지(번호 1)
		assertThat(prices(page.getContent())).containsExactly(1200, 1500);
		assertThat(page.getTotalElements()).isEqualTo(5);
		assertThat(page.getTotalPages()).isEqualTo(3);
		assertThat(page.getNumber()).isEqualTo(1);
		assertThat(page.hasNext()).isTrue();
	}

	// 4단계
	@Test
	void JPQL() {
		assertThat(prices(products.findExpensive(2000))).containsExactly(5000, 3000, 2000);
	}

	@Test
	void 프로젝션은_필요한_칸만() {
		List<ProductSummary> rows = products.findSummaryByNameContaining("형광");

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getName()).isEqualTo("형광펜");
		assertThat(rows.get(0).getPrice()).isEqualTo(1200);
	}

	@Test
	void 벌크_수정_후에는_다시_읽어야_새_값() {
		Product pen = products.findFirstByNameOrderByPriceAsc("펜").orElseThrow();

		int updated = products.raiseAll("펜", 100);

		assertThat(updated).isEqualTo(3);
		// clearAutomatically = true 라서 다시 조회하면 DB의 새 값
		assertThat(products.findById(pen.getNumber())).get().extracting(Product::getPrice).isEqualTo(1100);
	}
}
