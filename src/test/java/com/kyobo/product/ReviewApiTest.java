package com.kyobo.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

// 5단계: 요청부터 DB까지 한 번에 — 계약(상태 코드·JSON 모양) 확인
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext // 리뷰를 저장하므로 다른 @SpringBootTest와 DB를 나누지 않는다
class ReviewApiTest {

	@Autowired
	MockMvc mvc;

	static String body(String author, int rating, String content) {
		return "{\"author\":\"" + author + "\",\"rating\":" + rating + ",\"content\":\"" + content + "\"}";
	}

	@Test
	void 쓰기_목록_평점_중복까지() throws Exception {
		mvc.perform(post("/product/2/reviews").contentType(MediaType.APPLICATION_JSON).content(body("kim", 5, "좋아요")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.author").value("kim"))
				.andExpect(jsonPath("$.createdAt").exists());
		mvc.perform(post("/product/2/reviews").contentType(MediaType.APPLICATION_JSON).content(body("lee", 4, "괜찮아요")))
				.andExpect(status().isCreated());

		mvc.perform(get("/product/2/reviews"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].author").value("lee")) // 최신순
				.andExpect(jsonPath("$.page.totalElements").value(2));

		mvc.perform(get("/product/2/rating"))
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.average").value(4.5));

		mvc.perform(post("/product/2/reviews").contentType(MediaType.APPLICATION_JSON).content(body("kim", 1, "또")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("REVIEW_DUPLICATE"));
	}

	@Test
	void 별점_범위와_빈_내용은_400() throws Exception {
		mvc.perform(post("/product/3/reviews").contentType(MediaType.APPLICATION_JSON).content(body("kim", 6, "")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
				.andExpect(jsonPath("$.errors[0].field").value("content"))
				.andExpect(jsonPath("$.errors[1].field").value("rating"));
	}

	@Test
	void 없는_상품은_404() throws Exception {
		mvc.perform(post("/product/999/reviews").contentType(MediaType.APPLICATION_JSON).content(body("kim", 5, "x")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
		mvc.perform(get("/product/999/rating")).andExpect(status().isNotFound());
	}
}
