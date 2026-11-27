package com.kyobo.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

// 4단계: 만든 시각·고친 시각
@DataJpaTest
@Import({ QuerydslConfig.class, JpaAuditingConfig.class })
class AuditingTest {

	@Autowired
	ProductRepository products;

	@Autowired
	EntityManager em;

	@Test
	void 저장하면_두_시각이_채워진다() {
		Product saved = products.saveAndFlush(new Product("펜", 1000, 10));

		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
	}

	@Test
	void 엔티티를_고치면_고친_시각만_바뀐다() throws Exception {
		Product pen = products.saveAndFlush(new Product("펜", 1000, 10));
		var created = pen.getCreatedAt();
		var firstUpdated = pen.getUpdatedAt();
		Thread.sleep(20);

		pen.changeName("볼펜");
		products.flush(); // 더티 체킹 → update

		assertThat(pen.getCreatedAt()).isEqualTo(created);
		assertThat(pen.getUpdatedAt()).isAfter(firstUpdated);
	}

	@Test
	void 벌크_수정은_고친_시각을_바꾸지_않는다() throws Exception {
		Product pen = products.saveAndFlush(new Product("펜", 1000, 10));
		em.clear();
		// DB에서 다시 읽은 값으로 비교한다 (메모리는 나노초, DB는 마이크로초까지 저장)
		var firstUpdated = products.findById(pen.getNumber()).orElseThrow().getUpdatedAt();
		Thread.sleep(20);

		products.raiseAll("펜", 100); // update 쿼리 직접 실행 (10_03)

		Product reloaded = products.findById(pen.getNumber()).orElseThrow();
		assertThat(reloaded.getPrice()).isEqualTo(1100);
		assertThat(reloaded.getUpdatedAt()).isEqualTo(firstUpdated);
	}
}
