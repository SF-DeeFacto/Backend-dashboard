package com.backend_dashboard.backend_dashboard.zonePage.filter;

import com.backend_dashboard.backend_dashboard.common.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ZoneAuthFilter implements WebFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // /home/zone/** 경로만 체크
        String path = exchange.getRequest().getPath().toString();
        if(!path.startsWith("/home/zone")) {
            return chain.filter(exchange);
        }

        // 경로에서 zoneId 추출
        String zoneId = exchange.getRequest().getQueryParams().getFirst("zoneId");
        if(zoneId == null) {
            return sendError(exchange, ErrorCode.FORBIDDEN);
        }

        // header에서 X-Role 읽기
        List<String> roleHeader = exchange.getRequest().getHeaders().get("X-Role");
        if(roleHeader == null || roleHeader.isEmpty()) {
            return sendError(exchange, ErrorCode.UNAUTHORIZED);
        }

        List<String> roles = Arrays.stream(roleHeader.get(0).split(","))
                .map(String::trim)
                .toList();


        // 요청 zoneId가 roles에 없으면 접근 거부
        if(!roles.contains(String.valueOf(zoneId.charAt(0)))) {
            return sendError(exchange, ErrorCode.FORBIDDEN);
        }

        return chain.filter(exchange);

    }

//    private String extractZoneId(String path) {
//        String[] parts = path.split("/");
//        return (parts.length >=4) ? parts[3] : null;
//    }

    private Mono<Void> sendError(ServerWebExchange exchange, ErrorCode errorCode) {
        try {
            byte[] bytes = objectMapper
                    .writeValueAsString(ApiResponseDto.createError(
                            errorCode.getCode(),
                            errorCode.getMessage()
                    ))
                    .getBytes(StandardCharsets.UTF_8);

            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(errorCode.getStatus().value()));
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().setContentLength(bytes.length);

            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
