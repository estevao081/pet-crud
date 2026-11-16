package dev.estv.petcrud.pet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Substitui o antigo SecurityFilter (que validava o JWT e consultava o banco a cada request).
 * Agora o JWT já foi validado pelo gateway; aqui só transformamos os headers de identidade em
 * um Authentication, para que @PreAuthorize continue funcionando como antes.
 * Isto só é seguro porque o serviço não é exposto fora da rede interna e o gateway remove
 * esses headers de qualquer request vinda de fora.
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && role != null) {
            try {
                AuthenticatedUser principal = new AuthenticatedUser(UUID.fromString(userId), role);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority(role))));
            } catch (IllegalArgumentException ignored) {
                // id malformado: segue anônimo e o Spring Security responde 401
            }
        }
        filterChain.doFilter(request, response);
    }
}
