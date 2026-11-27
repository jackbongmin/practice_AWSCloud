package com.kyobo.product;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 서버가 뜰 때 상품 8개를 넣는다 (테스트 ProductQueryTest와 같은 데이터). H2 메모리 DB라 재시작하면 처음부터
@Component
public class DataInitializer implements CommandLineRunner {

	private final ProductRepository products;

	public DataInitializer(ProductRepository products) {
		this.products = products;
	}

	@Override
	public void run(String... args) {
		// 11_03: RDS처럼 데이터가 남는 DB에서는 이미 있으면 넣지 않는다 (안 그러면 재시작마다 8개씩 늘어난다)
		if (products.count() > 0) {
			return;
		}
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
}
