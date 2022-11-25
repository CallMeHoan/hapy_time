package com.happy_time.happy_time.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class TokenUtils {
    public String getFieldValueThroughToken(HttpServletRequest request, String field_name) {
        String authorization = request.getHeader("Authorization");
        String token = null;

        if(null != authorization && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        Claims claims = Jwts.parser().setSigningKey("hoan123").parseClaimsJws(token).getBody();
        return claims.get(field_name).toString();
    }
}
