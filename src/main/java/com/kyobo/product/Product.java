package com.kyobo.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// 교재 7장 Product와 같은 칸: 번호·이름·가격·재고 (+ 4단계: BaseEntity의 만든·고친 시각)
@Entity
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long number;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer stock;

	protected Product() {
	}

	public Product(String name, int price, int stock) {
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	public Long getNumber() { return number; }
	public String getName() { return name; }
	public Integer getPrice() { return price; }
	public Integer getStock() { return stock; }

	public void changeName(String name) {
		this.name = name;
	}
}
