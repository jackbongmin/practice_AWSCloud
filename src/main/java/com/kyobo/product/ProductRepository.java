package com.kyobo.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

	// 1단계: 주제 키워드 — find · exists · count · First/Top
	List<Product> findByName(String name);

	Optional<Product> findFirstByNameOrderByPriceAsc(String name);

	boolean existsByName(String name);

	long countByName(String name);

	List<Product> findTop3ByOrderByPriceDesc();

	// 2단계: 조건 키워드 — 결과가 여러 개일 수 있으면 List로 받는다
	List<Product> findByNameNot(String name);

	List<Product> findByNameAndPriceLessThan(String name, Integer price);

	List<Product> findByPriceBetween(Integer low, Integer high);

	List<Product> findByNameContaining(String keyword);

	// 3단계: 정렬 · 페이지
	List<Product> findByNameContaining(String keyword, Sort sort);

	Page<Product> findByNameContaining(String keyword, Pageable pageable);

	// 4단계: JPQL — 엔티티 이름(Product)과 필드 이름(p.price)으로 쓴다
	@Query("select p from Product p where p.price >= :min order by p.price desc")
	List<Product> findExpensive(@Param("min") int min);

	// 4단계: 필요한 칸만 — 인터페이스 프로젝션
	List<ProductSummary> findSummaryByNameContaining(String keyword);

	// 4단계: 한 번에 여러 행 수정. clearAutomatically: 끝나면 영속성 컨텍스트를 비운다
	@Modifying(clearAutomatically = true)
	@Query("update Product p set p.price = p.price + :amount where p.name = :name")
	int raiseAll(@Param("name") String name, @Param("amount") int amount);
}
