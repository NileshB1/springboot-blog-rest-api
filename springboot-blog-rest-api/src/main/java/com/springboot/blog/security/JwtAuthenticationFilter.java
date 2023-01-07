package com.springboot.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //Get JWT token from http request
        String token = getTokenFromRequest(request);
        System.out.println("#### [doFilterInternal] token received is: "+ token);

        //Validate JWT token
        if(StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            //Get username from token
            String userName = jwtTokenProvider.getUsername(token);

            //Load user associated with token
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
            System.out.println("#### [doFilterInternal] userdetails are: "+ userDetails);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        System.out.println("#### [doFilterInternal] setting in filter chain request in filterChain.doFilter()");
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String token = "";
        String bearerToken = request.getHeader("Authorization");
        System.out.println("#### [getTokenFromRequest] bearerToken is: "+ bearerToken);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7, bearerToken.length());
            //return  bearerToken.split(" ")[1].trim();
        }
        System.out.println("#### [getTokenFromRequest] returning token: "+ token);
        return token;
    }
}
