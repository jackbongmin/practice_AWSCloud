package com.kyobo.product;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 4단계: Auditing 켜기. 애플리케이션 클래스가 아니라 따로 둔다 (@WebMvcTest가 깨지지 않게)
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
