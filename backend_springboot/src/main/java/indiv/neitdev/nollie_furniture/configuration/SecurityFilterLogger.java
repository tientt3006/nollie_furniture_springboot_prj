package indiv.neitdev.nollie_furniture.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.FilterChainProxy;

@Configuration
@Slf4j
public class SecurityFilterLogger {

    @Autowired
    private FilterChainProxy filterChainProxy;

    @PostConstruct //after all bean created but not be used yet
    public void logSecurityFilters() {
        log.info("------ Spring Security Filters ------");

        int chainIndex = 0;
        for (var securityFilterChain : filterChainProxy.getFilterChains()) {
            log.info("Filter Chain #{}:", ++chainIndex);
            for (var filter : securityFilterChain.getFilters()) {
                log.info("  ➤ {}", filter.getClass().getName());
            }
        }

        log.info("------ End of Security Filters ------");
    }
}
