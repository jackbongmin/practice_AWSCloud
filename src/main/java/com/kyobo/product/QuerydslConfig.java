package com.kyobo.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

// 1단계: Querydsl 쿼리를 만드는 공장. 교재 예제 8.29와 같다
@Configuration
public class QuerydslConfig {

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return new JPAQueryFactory(em);
	}
}
