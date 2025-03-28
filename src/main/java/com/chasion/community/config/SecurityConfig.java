package com.chasion.community.config;

import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    // 放开静态资源访问
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    // 授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 需要登录的功能
        http.authorizeRequests()
                .antMatchers("/user/setting",
                        "/user/upload",
                        "/user/updatePassword",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR
                )
                .antMatchers("/discuss/top",
                            "/discuss/wonderful").hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers("/discuss/delete",
                        "/data/**",
                        "/actuator/**"


                ).hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll().and().csrf().disable();

        // 权限不够的处理
        http.exceptionHandling()
                .authenticationEntryPoint(
                        new AuthenticationEntryPoint() {
                            // 没有登录
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                                // 判断当前的请求是否是异步？
                                String xRequestedWith = request.getHeader("x-requested-with");
                                if ("XMLHttpRequest".equals(xRequestedWith)) {
                                    // 异步请求
                                    response.setContentType("application/plain;charset=utf-8");
                                    PrintWriter writer = response.getWriter();
                                    writer.write(CommunityUtil.getJSONString(403, "你还没有登录！"));
                                }else {
                                    response.sendRedirect(request.getContextPath() + "/login");
                                }
                            }
                        }
                )
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不够
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        // 判断当前的请求是否是异步？
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            // 异步请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限！"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        // security底层会默认拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/security/logout");
    }
}
