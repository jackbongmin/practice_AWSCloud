package com.kyobo.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import com.kyobo.product.ProductDtos.ProductResponse;

import static org.assertj.core.api.Assertions.assertThat;

// 5단계: 주소의 page·size·sort가 Pageable로 바뀌고, 응답은 {content, page} 모양
@WebMvcTest(ProductController.class)
class ProductSearchApiTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	ProductService service;

	@Test
	void 검색_파라미터가_Pageable로_바뀐다() throws Exception {
		given(service.search(eq("펜"), any(Pageable.class))).willReturn(new PageImpl<>(
				List.of(new ProductResponse(5L, "형광펜", 1200, 12)), PageRequest.of(1, 2), 5));

		mvc.perform(get("/product/search").param("keyword", "펜")
				.param("page", "1").param("size", "2").param("sort", "price,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("형광펜"))
				.andExpect(jsonPath("$.page.totalElements").value(5))
				.andExpect(jsonPath("$.page.totalPages").value(3))
				.andExpect(jsonPath("$.page.number").value(1));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(service).search(eq("펜"), captor.capture());
		assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
		assertThat(captor.getValue().getPageSize()).isEqualTo(2);
		assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Order.desc("price")));
	}

	@Test
	void 아무것도_안_주면_기본값_size5_번호순() throws Exception {
		given(service.search(eq("펜"), any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

		mvc.perform(get("/product/search").param("keyword", "펜")).andExpect(status().isOk());

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(service).search(eq("펜"), captor.capture());
		assertThat(captor.getValue().getPageNumber()).isZero();
		assertThat(captor.getValue().getPageSize()).isEqualTo(5);
		assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("number"));
	}
}
