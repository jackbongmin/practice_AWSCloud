package com.kyobo.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// 3·4단계: 진짜 서비스·DB까지 — 정렬 칸 오류는 저장소에서 나므로 가짜 서비스로는 확인할 수 없다
@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

	@Autowired
	MockMvc mvc;

	@Test
	void 없는_정렬_칸은_500이_아니라_400() throws Exception {
		mvc.perform(get("/product/search").param("keyword", "펜").param("sort", "prise"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("정렬할 수 없는 칸입니다: prise"));
	}

	@Test
	void 재고가_남은_상품은_삭제할_수_없다() throws Exception {
		// DataInitializer의 1번 상품: 펜, 재고 10
		mvc.perform(delete("/product").param("number", "1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("재고가 10개 남은 상품은 삭제할 수 없습니다"));
	}
}
