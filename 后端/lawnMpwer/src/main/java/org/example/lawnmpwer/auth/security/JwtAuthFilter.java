package org.example.lawnmpwer.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
/**
 * 继承 OncePerRequestFilter 确保每次外部 HTTP 请求只经过这个拦截器一次。
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    // 1. 放行前端的跨域预检请求（OPTIONS 请求）
    // 前端在发 POST/PUT 之前，经常会发一个 OPTIONS 问问服务器能不能连，这里直接放行。
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String header = request.getHeader("Authorization");

        // 只处理 Bearer token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String username = JwtUtil.parseUsername(token);

                var auth = new UsernamePasswordAuthenticationToken(
                        username, null, List.of()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // token 错/过期：不设置认证信息，后面会被 Security 拒绝
            }
        }

        filterChain.doFilter(request, response);
    }
}
