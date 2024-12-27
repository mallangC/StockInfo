package zerobase.stockinfo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration(enforceUniqueMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter authenticationFilter;

  @Bean
  protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
  http    .csrf((csrf) -> csrf.disable())
          .sessionManagement(sessionConfig ->
                  sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests((auth) ->
                  auth.requestMatchers("/**/signup", "/**/signin").permitAll())
          .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class);
      return http.build();
  }

  @Bean
  protected WebSecurityCustomizer configure() throws Exception {
    return (web)-> web.ignoring()
            .requestMatchers(PathRequest.toH2Console())
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    .requestMatchers("/h2-console/**");
  }
}
