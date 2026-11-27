package com.kyobo.product;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ReviewDtos {

	private ReviewDtos() {
	}

	// 2단계: 요청 검증 (10_06)
	public record ReviewRequest(
			@NotBlank(message = "작성자는 비어 있을 수 없습니다")
			@Size(max = 20, message = "작성자는 20자 이하입니다")
			String author,
			@NotNull(message = "별점은 필수입니다")
			@Min(value = 1, message = "별점은 1~5입니다")
			@Max(value = 5, message = "별점은 1~5입니다")
			Integer rating,
			@NotBlank(message = "내용은 비어 있을 수 없습니다")
			@Size(max = 200, message = "내용은 200자 이하입니다")
			String content) {
	}

	// 엔티티 대신 내보내는 응답 (10_05: 엔티티 JSON 순환 방지)
	public record ReviewResponse(Long id, String author, int rating, String content, LocalDateTime createdAt) {

		static ReviewResponse from(Review r) {
			return new ReviewResponse(r.getId(), r.getAuthor(), r.getRating(), r.getContent(), r.getCreatedAt());
		}
	}

	// 4단계: 평균은 소수 첫째 자리까지
	public record RatingSummary(long count, double average) {

		public RatingSummary {
			average = Math.round(average * 10) / 10.0;
		}
	}
}
