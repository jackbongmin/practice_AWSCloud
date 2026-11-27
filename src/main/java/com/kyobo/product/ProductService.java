package com.kyobo.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kyobo.product.ProductDtos.ProductRequest;
import com.kyobo.product.ProductDtos.ProductResponse;

@Service
public class ProductService {

	private final ProductRepository products;

	// 생성자로 받는다 → 테스트에서 가짜(mock) 저장소를 넣을 수 있다
	public ProductService(ProductRepository products) {
		this.products = products;
	}

	public ProductResponse getProduct(Long number) {
		return ProductResponse.from(find(number));
	}

	// 5단계: 이름 검색 + 페이지. 엔티티 대신 응답 DTO로 바꿔 내보낸다
	public Page<ProductResponse> search(String keyword, Pageable pageable) {
		return products.findByNameContaining(keyword, pageable).map(ProductResponse::from);
	}

	// 2단계: 조건이 있을 때만 붙는 검색
	public Page<ProductResponse> filter(ProductSearchCond cond, Pageable pageable) {
		return products.search(cond, pageable).map(ProductResponse::from);
	}

	// 4단계: 만든·고친 시각 확인용
	public ProductDtos.ProductDetail detail(Long number) {
		return ProductDtos.ProductDetail.from(find(number));
	}

	@Transactional
	public ProductResponse saveProduct(ProductRequest req) {
		Product saved = products.save(new Product(req.name(), req.price(), req.stock()));
		return ProductResponse.from(saved);
	}

	@Transactional
	public ProductResponse changeName(Long number, String name) {
		Product product = find(number);
		product.changeName(name);
		return ProductResponse.from(product);
	}

	@Transactional
	public void deleteProduct(Long number) {
		Product product = find(number);
		// 10_06 3단계: 업무 규칙 — 입력 형식은 맞지만 지금 상태로는 할 수 없는 요청
		if (product.getStock() > 0) {
			throw new BusinessException(ErrorCode.PRODUCT_IN_STOCK,
					"재고가 " + product.getStock() + "개 남은 상품은 삭제할 수 없습니다");
		}
		products.delete(product);
	}

	// 3단계: 교재 원본은 findById(number).get() — 없는 번호면 NoSuchElementException(500)
	private Product find(Long number) {
		return products.findById(number).orElseThrow(() -> new ProductNotFoundException(number));
	}
}
