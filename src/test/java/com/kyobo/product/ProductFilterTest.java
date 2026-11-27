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

// 2·3·5단계: 조건이 있을 때만 붙는 검색 (같은 8개 데이터)
@DataJpaTest
@Import(QuerydslConfig.class)
class ProductFilterTest {

	@Autowired
	ProductRepository products;

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

	Page<Product> search(String name, Integer min, Integer max) {
		return products.search(new ProductSearchCond(name, min, max), PageRequest.of(0, 10));
	}

	static List<Integer> prices(Page<Product> page) {
		return page.getContent().stream().map(Product::getPrice).toList();
	}

	@Test
	void 조건이_없으면_전부() {
		assertThat(search(null, null, null).getTotalElements()).isEqualTo(8);
	}

	@Test
	void 이름만() {
		assertThat(search("펜", null, null).getTotalElements()).isEqualTo(5);
	}

	@Test
	void 가격_범위만() {
		assertThat(prices(search(null, 1000, 2000))).containsExactly(1000, 1500, 2000, 1200);
	}

	@Test
	void 이름과_최소가격() {
		assertThat(prices(search("펜", 1200, null))).containsExactly(1500, 2000, 1200);
	}

	@Test
	void 빈_문자열_이름은_조건_없음() {
		assertThat(search(" ", null, 1000).getTotalElements()).isEqualTo(3); // 1000·800·500
	}

	@Test
	void 페이지와_count() {
		Page<Product> page = products.search(new ProductSearchCond("펜", null, null), PageRequest.of(1, 2));

		assertThat(prices(page)).containsExactly(2000, 800); // 번호순: 1000,1500 | 2000,800 | 1200
		assertThat(page.getTotalElements()).isEqualTo(5);
		assertThat(page.getTotalPages()).isEqualTo(3);
	}

	@Test
	void 교재_findByName을_고친_판은_이름으로_찾는다() {
		assertThat(products.findByNameQ("펜")).hasSize(3)
				.allMatch(p -> p.getName().equals("펜"));
	}
}
