package com.kyobo.product;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kyobo.product.ProductDtos.ChangeNameRequest;
import com.kyobo.product.ProductDtos.ProductRequest;
import com.kyobo.product.ProductDtos.ProductResponse;

// 교재 7장과 같은 주소: GET /product?number=1, POST /product, PUT /product, DELETE /product?number=1
@RestController
@RequestMapping("/product")
public class ProductController {

	private final ProductService service;

	public ProductController(ProductService service) {
		this.service = service;
	}

	@GetMapping
	public ProductResponse get(@RequestParam Long number) {
		return service.getProduct(number);
	}

	// 5단계: GET /product/search?keyword=펜&page=0&size=2&sort=price,desc
	@GetMapping("/search")
	public Page<ProductResponse> search(@RequestParam String keyword,
			@PageableDefault(size = 5, sort = "number") Pageable pageable) {
		return service.search(keyword, pageable);
	}

	// 2단계: GET /product/filter?name=펜&minPrice=1000&maxPrice=2000&page=0&size=5
	@GetMapping("/filter")
	public Page<ProductResponse> filter(@Valid ProductSearchCond cond, @PageableDefault(size = 5) Pageable pageable) {
		return service.filter(cond, pageable);
	}

	// 4단계: GET /product/detail?number=1 — createdAt·updatedAt 포함
	@GetMapping("/detail")
	public ProductDtos.ProductDetail detail(@RequestParam Long number) {
		return service.detail(number);
	}

	@PostMapping
	public ProductResponse create(@Valid @RequestBody ProductRequest req) {
		return service.saveProduct(req);
	}

	@PutMapping
	public ProductResponse changeName(@Valid @RequestBody ChangeNameRequest req) {
		return service.changeName(req.number(), req.name());
	}

	@DeleteMapping
	public String delete(@RequestParam Long number) {
		service.deleteProduct(number);
		return "정상적으로 삭제되었습니다.";
	}
}
