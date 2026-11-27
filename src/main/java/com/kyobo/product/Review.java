package com.kyobo.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// 1단계: 상품 리뷰. 다대일 — 외래 키 product_number가 이 테이블에 있다 (10_05)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "product_number", "author" }))
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_number")
	private Product product;

	@Column(nullable = false, length = 20)
	private String author;

	@Column(nullable = false)
	private int rating;

	@Column(nullable = false, length = 200)
	private String content;

	protected Review() {
	}

	public Review(Product product, String author, int rating, String content) {
		this.product = product;
		this.author = author;
		this.rating = rating;
		this.content = content;
	}

	public Long getId() { return id; }
	public Product getProduct() { return product; }
	public String getAuthor() { return author; }
	public int getRating() { return rating; }
	public String getContent() { return content; }
}
