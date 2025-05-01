package indiv.neitdev.nollie_furniture.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
//@EnableWebSecurity //auto enable when config  securityFilterChain Bean
@EnableMethodSecurity // not auto enable when use @PreAuthorize / @PostAuthorize on method
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/", "/user", "/auth/**"
    };
    private static final String[] ADMIN_ENDPOINTS = {
            "/a"
    };
    private static final String[] STAFF_ENDPOINTS = {
            "/s"
    };

    private final CustomJwtDecoder customJwtDecoder;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests( auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS)
                        .hasAuthority("ROLE_ADMIN")
                        .requestMatchers(STAFF_ENDPOINTS)
                        .hasAuthority("ROLE_STAFF")
                        .anyRequest()
                        .authenticated());

        // this will auto add BearerTokenAuthenticationFilter in filterChanin
        // introspect token and save Authentication to SecurityContextHolder
//        Bên trong BearerTokenAuthenticationFilter, thực tế nó sẽ:
//        Gọi một AuthenticationManager.
//                AuthenticationManager sẽ dùng JwtAuthenticationProvider.
//                JwtAuthenticationProvider sẽ lấy token, decode, rồi sinh Authentication.
//        Sau đó nó set vào SecurityContextHolder qua cơ chế mặc định của Spring Security.
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));

        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable);

        httpSecurity.addFilterAfter(new AuthenticationLoggingFilter(), BearerTokenAuthenticationFilter.class);


        // This config is default so i can comment out it
        // This config for auto save securityContext to session or threat-local after authen within a request
//        httpSecurity.securityContext(
//                securityContext -> securityContext.requireExplicitSave(false));

        return httpSecurity.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                List.of("http://127.0.0.1:5500", "http://127.0.0.1:3000", "http://127.0.0.1:5501"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*", "Content-Type"));
        configuration.setAllowCredentials(true); // frontend can send cookies, authorization header, TLS cert,..; when this set to true, AllowedOrigins cant be "*"
        configuration.setExposedHeaders(List.of("Set-Cookie")); // frontend can read what

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Config converter to convert JwtAuthentication to Authentication(include: principal, authorities, Credential, isAuthenticated) which later save to SecurityContext
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // new small Authorities Converter (JwtGrantedAuthoritiesConverter) to convert jwt to Collection<GrantedAuthority> authorities (all in claim role)
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // default prefix is SCOPE_, change to ROLE_
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role"); // default ClaimName to get authorities from is scope or scp, change to role

        // new Authentication converter (JwtAuthenticationConverter) to convert jwtAuthentication to Authentication, AbstractAuthenticationToken(JwtAuthenticationToken or UserPasswordAuthenticationToken)
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        // likely override the  JwtGrantedAuthoritiesConverter but no need
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            Collection<GrantedAuthority> authorities = new ArrayList<>(jwtGrantedAuthoritiesConverter.convert(jwt));
//            String scope = jwt.getClaimAsString("role");
//            if ("ADMIN".equals(scope)) {
//                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
//            } else if ("STAFF".equals(scope)) {
//                authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
//            } else {
//                authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
//            }
//            return authorities;
//        });

        return jwtAuthenticationConverter;
    }


//    @Bean
//    JwtDecoder jwtDecoder() {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
//        return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
//    }
}
