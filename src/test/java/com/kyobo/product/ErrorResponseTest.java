package com.kyobo.product;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// 1~4단계: 잘못된 요청마다 상태 코드와 code·errors를 확인한다 (서비스는 가짜)
@WebMvcTest(ProductController.class)
class ErrorResponseTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	ProductService service;

	@Test
	void 칸_검증_실패는_400과_칸별_메시지() throws Exception {
		mvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"price\":-100,\"stock\":-5}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.errors", hasSize(3)))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[1].field").value("price"))
				.andExpect(jsonPath("$.errors[2].field").value("stock"));
		verify(service, never()).saveProduct(any()); // 검증에서 막히면 서비스까지 가지 않는다
	}

	@Test
	void 가격을_빠뜨리면_0이_아니라_400() throws Exception {
		mvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"pen\",\"stock\":1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("가격은 필수입니다"));
	}

	@Test
	void 숫자_자리에_문자는_400() throws Exception {
		mvc.perform(get("/product").param("number", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
				.andExpect(jsonPath("$.detail").value("number 값 'abc'의 형식이 올바르지 않습니다"));
	}

	@Test
	void JSON을_읽을_수_없으면_400() throws Exception {
		mvc.perform(post("/product").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"pen\",\"price\":\"abc\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
	}

	@Test
	void 검색_조건의_두_칸_관계() throws Exception {
		mvc.perform(get("/product/filter").param("minPrice", "3000").param("maxPrice", "1000"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].message").value("최소 가격이 최대 가격보다 클 수 없습니다"));
	}

	@Test
	void 검색_조건_형식_오류는_자바_이름을_드러내지_않는다() throws Exception {
		mvc.perform(get("/product/filter").param("minPrice", "abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("minPrice"))
				.andExpect(jsonPath("$.errors[0].message").value("형식이 올바르지 않습니다: abc"));
	}

	@Test
	void 없는_상품은_404() throws Exception {
		given(service.getProduct(999L)).willThrow(new ProductNotFoundException(999L));

		mvc.perform(get("/product").param("number", "999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
	}

	@Test
	void 업무_규칙_위반은_409() throws Exception {
		willThrow(new BusinessException(ErrorCode.PRODUCT_IN_STOCK, "재고가 10개 남은 상품은 삭제할 수 없습니다"))
				.given(service).deleteProduct(1L);

		mvc.perform(delete("/product").param("number", "1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("PRODUCT_IN_STOCK"));
	}

	@Test
	void 없는_주소는_500이_아니라_404() throws Exception {
		mvc.perform(get("/nothing-here"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NO_SUCH_API"));
	}

	@Test
	void 없는_메서드는_405() throws Exception {
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/product").param("number", "1"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void 지원하지_않는_본문_형식은_415() throws Exception {
		mvc.perform(post("/product").contentType(MediaType.TEXT_PLAIN).content("pen"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
	}

	@Test
	void 예상_못한_예외는_500이고_내부_메시지를_숨긴다() throws Exception {
		given(service.getProduct(1L)).willThrow(new IllegalStateException("DB 비밀번호가 틀림"));

		mvc.perform(get("/product").param("number", "1"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.detail").value("잠시 후 다시 시도해 주세요"));
	}
}
