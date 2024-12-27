package zerobase.stockinfo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.stockinfo.service.MemberService;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

  private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; //1시간
  private static final String KEY_ROLES = "roles";

  private final MemberService memberService;

  @Value("${spring.jwt.secret}")
  private String secretKey;
  private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

  public String generateToken(String username, List<String> roles) {
    Claims claims = (Claims) Jwts.claims().subject(username);
    claims.put(KEY_ROLES, roles);

    var now = new Date();
    var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

    return Jwts.builder()
            .claims(claims)
            .issuedAt(now)
            .expiration(expiredDate)
            .signWith(key)
            .compact();
  }

  public Authentication getAuthentication(String jwt) {
    UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));

    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    return this.parseClaims(token).getSubject();
  }

  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) return false;

    var claims = this.parseClaims(token);
    return claims.getExpiration().before(new Date());
  }

  private Claims parseClaims(String token) {
    try{

      return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload();

    }catch (ExpiredJwtException e){
      return e.getClaims();
    }
  }

}
