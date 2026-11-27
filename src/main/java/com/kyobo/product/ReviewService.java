package com.kyobo.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyobo.product.ReviewDtos.RatingSummary;
import com.kyobo.product.ReviewDtos.ReviewRequest;
import com.kyobo.product.ReviewDtos.ReviewResponse;

@Service
public class ReviewService {

	private final ReviewRepository reviews;
	private final ProductRepository products;

	public ReviewService(ReviewRepository reviews, ProductRepository products) {
		this.reviews = reviews;
		this.products = products;
	}

	// 1·3단계: 없는 상품 404, 같은 사람의 두 번째 리뷰 409 (10_06 업무 예외)
	@Transactional
	public ReviewResponse write(Long productNumber, ReviewRequest req) {
		Product product = products.findById(productNumber)
				.orElseThrow(() -> new ProductNotFoundException(productNumber));
		if (reviews.existsByProductNumberAndAuthor(productNumber, req.author())) {
			throw new BusinessException(ErrorCode.REVIEW_DUPLICATE, req.author() + "님은 이미 이 상품에 리뷰를 남겼습니다");
		}
		return ReviewResponse.from(reviews.save(new Review(product, req.author(), req.rating(), req.content())));
	}

	@Transactional(readOnly = true)
	public Page<ReviewResponse> list(Long productNumber, Pageable pageable) {
		if (!products.existsById(productNumber)) {
			throw new ProductNotFoundException(productNumber);
		}
		return reviews.findByProductNumber(productNumber, pageable).map(ReviewResponse::from);
	}

	@Transactional(readOnly = true)
	public RatingSummary summary(Long productNumber) {
		if (!products.existsById(productNumber)) {
			throw new ProductNotFoundException(productNumber);
		}
		return reviews.summarize(productNumber);
	}
}
