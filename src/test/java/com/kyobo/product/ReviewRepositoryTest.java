package com.kyobo.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

// 3·4단계: 쿼리 메서드·집계 JPQL·DB 유일 제약 (10_03·10_05)
@DataJpaTest
@Import({ QuerydslConfig.class, JpaAuditingConfig.class })
class ReviewRepositoryTest {

	@Autowired
	ReviewRepository reviews;

	@Autowired
	ProductRepository products;

	Product pen;
	Product notebook;

	@BeforeEach
	void setUp() {
		pen = products.save(new Product("펜", 1000, 10));
		notebook = products.save(new Product("공책", 3000, 7));
		reviews.save(new Review(pen, "kim", 5, "좋아요"));
		reviews.save(new Review(pen, "lee", 4, "괜찮아요"));
		reviews.save(new Review(pen, "park", 4, "무난"));
		reviews.saveAndFlush(new Review(notebook, "kim", 2, "얇아요"));
	}

	@Test
	void 상품별_목록과_페이지() {
		var page = reviews.findByProductNumber(pen.getNumber(), PageRequest.of(0, 2));

		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getTotalElements()).isEqualTo(3);
	}

	@Test
	void 평균과_개수() {
		var summary = reviews.summarize(pen.getNumber());

		assertThat(summary.count()).isEqualTo(3);
		assertThat(summary.average()).isEqualTo(4.3); // (5+4+4)/3 = 4.333… → 4.3
	}

	@Test
	void 리뷰가_없으면_0건_0점() {
		Product pencil = products.save(new Product("연필", 500, 50));

		var summary = reviews.summarize(pencil.getNumber());

		assertThat(summary.count()).isZero();
		assertThat(summary.average()).isZero();
	}

	@Test
	void 같은_상품_같은_작성자는_DB도_막는다() {
		// 서비스의 중복 검사를 건너뛰어도(동시에 두 요청이 와도) 유일 제약이 마지막 방어선
		assertThatThrownBy(() -> reviews.saveAndFlush(new Review(pen, "kim", 1, "또 씀")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void 다른_상품이면_같은_작성자도_된다() {
		assertThat(reviews.existsByProductNumberAndAuthor(notebook.getNumber(), "kim")).isTrue();
		assertThat(reviews.existsByProductNumberAndAuthor(notebook.getNumber(), "lee")).isFalse();
	}
}
