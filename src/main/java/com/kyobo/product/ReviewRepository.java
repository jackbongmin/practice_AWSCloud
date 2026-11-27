package com.kyobo.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 3단계: 쿼리 메서드 — 연관 객체의 필드는 Product + Number로 (10_03·10_05)
	boolean existsByProductNumberAndAuthor(Long productNumber, String author);

	Page<Review> findByProductNumber(Long productNumber, Pageable pageable);

	// 4단계: 평균·개수 — 이름으로 못 만드는 집계는 JPQL (10_03)
	@Query("select new com.kyobo.product.ReviewDtos$RatingSummary(count(r), coalesce(avg(r.rating), 0))"
			+ " from Review r where r.product.number = :number")
	ReviewDtos.RatingSummary summarize(@Param("number") Long number);
}
