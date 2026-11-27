package com.kyobo.product;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// 4단계: 모든 컨트롤러의 오류를 한 모양(ProblemDetail, RFC 9457)으로 바꾼다
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	// 칸 검증 실패 — @Valid @RequestBody는 MethodArgumentNotValidException(BindException의 자식),
	// @Valid 검색 조건(쿼리 파라미터)은 BindException
	@ExceptionHandler(BindException.class)
	ProblemDetail invalid(BindException e) {
		List<Map<String, String>> errors = e.getBindingResult().getAllErrors().stream()
				.map(err -> {
					if (err instanceof FieldError fe) {
						// 타입 변환 실패(minPrice=abc)의 기본 메시지는 자바 클래스 이름이 드러난다 → 바꿔 쓴다
						String message = fe.isBindingFailure() ? "형식이 올바르지 않습니다: " + fe.getRejectedValue()
								: fe.getDefaultMessage();
						return Map.of("field", fe.getField(), "message", String.valueOf(message));
					}
					return Map.of("field", err.getObjectName(), "message", String.valueOf(err.getDefaultMessage()));
				})
				.sorted(java.util.Comparator.comparing(m -> m.get("field"))) // 순서를 고정한다 (검증 순서는 정해져 있지 않다)
				.toList();
		ProblemDetail pd = problem(ErrorCode.INVALID_INPUT, "입력 값이 올바르지 않습니다");
		pd.setProperty("errors", errors);
		return pd;
	}

	// 사용자가 보낸 값의 모양이 틀림 — 타입 변환, 필수 파라미터 누락, JSON 파싱, 없는 정렬 칸
	@ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class, PropertyReferenceException.class })
	ProblemDetail badParameter(Exception e) {
		String detail;
		if (e instanceof MethodArgumentTypeMismatchException m) {
			detail = m.getName() + " 값 '" + m.getValue() + "'의 형식이 올바르지 않습니다";
		} else if (e instanceof MissingServletRequestParameterException m) {
			detail = m.getParameterName() + " 파라미터가 필요합니다";
		} else if (e instanceof PropertyReferenceException p) {
			detail = "정렬할 수 없는 칸입니다: " + p.getPropertyName();
		} else {
			detail = "요청 본문을 읽을 수 없습니다";
		}
		return problem(ErrorCode.INVALID_PARAMETER, detail);
	}

	// 없는 주소·없는 메서드 — 아래 Exception 그물에 걸리면 500이 된다 (실측)
	@ExceptionHandler(NoResourceFoundException.class)
	ProblemDetail noSuchApi(NoResourceFoundException e) {
		return problem(ErrorCode.NO_SUCH_API, "없는 주소입니다: /" + e.getResourcePath());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ProblemDetail methodNotAllowed(HttpRequestMethodNotSupportedException e) {
		return problem(ErrorCode.METHOD_NOT_ALLOWED, e.getMethod() + " 메서드는 지원하지 않습니다");
	}

	@ExceptionHandler(BusinessException.class)
	ProblemDetail business(BusinessException e) {
		return problem(e.code(), e.getMessage());
	}

	// 나머지 — 원인은 로그에만, 응답에는 내부 사정을 드러내지 않는다
	@ExceptionHandler(Exception.class)
	ProblemDetail unknown(Exception e) {
		// 스프링이 상태를 아는 요청 오류(415 본문 형식, 406 응답 형식 등)는 그 상태 그대로 — 500으로 바꾸지 않는다 (실측)
		if (e instanceof org.springframework.web.ErrorResponse er && er.getStatusCode().is4xxClientError()) {
			ProblemDetail pd = er.getBody();
			pd.setProperty("code", ErrorCode.INVALID_PARAMETER.name());
			return pd;
		}
		log.error("처리하지 못한 예외", e);
		return problem(ErrorCode.INTERNAL_ERROR, "잠시 후 다시 시도해 주세요");
	}

	private static ProblemDetail problem(ErrorCode code, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.status(), detail);
		pd.setType(URI.create("about:blank"));
		pd.setProperty("code", code.name());
		return pd;
	}
}
