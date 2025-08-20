package com.backend_dashboard.backend_dashboard.zonePage.filter;

import com.backend_dashboard.backend_dashboard.common.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public ZoneAuthFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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

        // Header에서 employeeId 추출
        String employeeId = exchange.getRequest().getHeaders().getFirst("X-Employee-Id");
        if(employeeId == null || employeeId.isEmpty()) {
            return sendError(exchange, ErrorCode.UNAUTHORIZED);
        }

        // Redis에서 scope 조회 및 권한 검증
        return redisTemplate.opsForValue()
                .get(employeeId)
                .flatMap(value -> {
                    if(value == null || value.isEmpty()) {
                        return sendError(exchange, ErrorCode.UNAUTHORIZED);
                    }

                    // scope 추출
                    List<String> scopes;
                    try{
                        JsonNode node = objectMapper.readTree(value);
                        String scopeStr = node.path("scope").asText();
                        scopes = Arrays.stream(scopeStr.split(","))
                                .map(String::trim)
                                .toList();
                    } catch (Exception e) {
                        return sendError(exchange, ErrorCode.INTERNAL_ERROR);
                    }

                    // zoneId와 scope 매칭
                    String zoneScope = String.valueOf(zoneId.charAt(0));
                    if(!scopes.contains(zoneScope)) {
                        return sendError(exchange, ErrorCode.FORBIDDEN);
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(sendError(exchange, ErrorCode.UNAUTHORIZED));

    }


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
