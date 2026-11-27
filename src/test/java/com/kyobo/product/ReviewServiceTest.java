package com.kyobo.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.kyobo.product.ReviewDtos.ReviewRequest;

// 5단계: 서비스 단위 테스트 (10_01 방식). 업무 규칙 3가지
class ReviewServiceTest {

	ReviewRepository reviews = mock(ReviewRepository.class);
	ProductRepository products = mock(ProductRepository.class);
	ReviewService service = new ReviewService(reviews, products);

	ReviewRequest req = new ReviewRequest("kim", 5, "잘 써져요");

	@Test
	void 리뷰를_저장하고_응답으로_바꾼다() {
		Product pen = new Product("펜", 1000, 10);
		given(products.findById(1L)).willReturn(Optional.of(pen));
		given(reviews.save(any(Review.class))).willAnswer(inv -> inv.getArgument(0));

		ReviewDtos.ReviewResponse res = service.write(1L, req);

		assertThat(res.author()).isEqualTo("kim");
		assertThat(res.rating()).isEqualTo(5);
	}

	@Test
	void 없는_상품에는_쓸_수_없다() {
		given(products.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.write(999L, req)).isInstanceOf(ProductNotFoundException.class);
		verify(reviews, never()).save(any());
	}

	@Test
	void 같은_사람의_두_번째_리뷰는_409() {
		given(products.findById(1L)).willReturn(Optional.of(new Product("펜", 1000, 10)));
		given(reviews.existsByProductNumberAndAuthor(1L, "kim")).willReturn(true);

		assertThatThrownBy(() -> service.write(1L, req))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).code()).isEqualTo(ErrorCode.REVIEW_DUPLICATE);
		verify(reviews, never()).save(any());
	}

	@Test
	void 없는_상품의_목록과_평점은_404() {
		given(products.existsById(999L)).willReturn(false);

		assertThatThrownBy(() -> service.list(999L, Pageable.unpaged())).isInstanceOf(ProductNotFoundException.class);
		assertThatThrownBy(() -> service.summary(999L)).isInstanceOf(ProductNotFoundException.class);
	}
}
