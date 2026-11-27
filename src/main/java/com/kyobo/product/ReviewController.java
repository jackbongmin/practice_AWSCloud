package com.kyobo.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kyobo.product.ReviewDtos.RatingSummary;
import com.kyobo.product.ReviewDtos.ReviewRequest;
import com.kyobo.product.ReviewDtos.ReviewResponse;

import jakarta.validation.Valid;

// POST /product/1/reviews · GET /product/1/reviews?page=0&size=5 · GET /product/1/rating
@RestController
@RequestMapping("/product/{number}")
public class ReviewController {

	private final ReviewService service;

	public ReviewController(ReviewService service) {
		this.service = service;
	}

	@PostMapping("/reviews")
	@ResponseStatus(HttpStatus.CREATED)
	public ReviewResponse write(@PathVariable Long number, @Valid @RequestBody ReviewRequest req) {
		return service.write(number, req);
	}

	@GetMapping("/reviews")
	public Page<ReviewResponse> list(@PathVariable Long number,
			@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return service.list(number, pageable);
	}

	@GetMapping("/rating")
	public RatingSummary rating(@PathVariable Long number) {
		return service.summary(number);
	}
}
