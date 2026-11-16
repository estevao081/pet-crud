package dev.estv.petcrud.gateway.security;

import org.springframework.http.HttpMethod;

/**
 * Rotas que não exigem token. É a mesma lista do SecurityConfig do monólito.
 * Todo o resto exige JWT válido.
 */
public final class PublicRoutes {

    private PublicRoutes() {
    }

    public static boolean isPublic(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && (path.equals("/auth") || path.startsWith("/auth/"))) {
            return true;
        }
        if (HttpMethod.POST.equals(method) && path.equals("/pets/search")) {
            return true;
        }
        if (HttpMethod.GET.equals(method) && path.equals("/pets")) {
            return true;
        }
        return HttpMethod.GET.equals(method) && path.startsWith("/images/");
    }
}
