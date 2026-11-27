package com.kyobo.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// 요청·응답 모양. 교재의 ProductDto · ProductResponseDto · ChangeProductNameDto와 같은 칸
public final class ProductDtos {

	private ProductDtos() {
	}

	// 1단계: 요청 검증. 숫자는 Integer — int면 빠뜨린 칸이 조용히 0이 된다
	public record ProductRequest(
			@NotBlank(message = "이름은 비어 있을 수 없습니다")
			@Size(max = 30, message = "이름은 30자 이하입니다")
			String name,
			@NotNull(message = "가격은 필수입니다")
			@PositiveOrZero(message = "가격은 0 이상이어야 합니다")
			Integer price,
			@NotNull(message = "재고는 필수입니다")
			@Min(value = 0, message = "재고는 0 이상이어야 합니다")
			Integer stock) {
	}

	public record ProductResponse(Long number, String name, int price, int stock) {

		static ProductResponse from(Product p) {
			return new ProductResponse(p.getNumber(), p.getName(), p.getPrice(), p.getStock());
		}
	}

	public record ChangeNameRequest(@NotNull Long number, @NotBlank String name) {
	}

	// 4단계: 시각까지 보여 주는 응답
	public record ProductDetail(Long number, String name, int price, int stock,
			java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {

		static ProductDetail from(Product p) {
			return new ProductDetail(p.getNumber(), p.getName(), p.getPrice(), p.getStock(),
					p.getCreatedAt(), p.getUpdatedAt());
		}
	}
}
