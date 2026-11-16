package dev.estv.petcrud.gateway;

import dev.estv.petcrud.gateway.security.PublicRoutes;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PublicRoutesTest {

    @Test
    void publicRoutesMatchTheMonolithRules() {
        assertThat(PublicRoutes.isPublic(HttpMethod.POST, "/auth/login")).isTrue();
        assertThat(PublicRoutes.isPublic(HttpMethod.POST, "/auth/register")).isTrue();
        assertThat(PublicRoutes.isPublic(HttpMethod.POST, "/pets/search")).isTrue();
        assertThat(PublicRoutes.isPublic(HttpMethod.GET, "/pets")).isTrue();
        assertThat(PublicRoutes.isPublic(HttpMethod.GET, "/images/abc")).isTrue();
        assertThat(PublicRoutes.isPublic(HttpMethod.OPTIONS, "/users/admin")).isTrue();
    }

    @Test
    void everythingElseRequiresAToken() {
        assertThat(PublicRoutes.isPublic(HttpMethod.POST, "/pets")).isFalse();
        assertThat(PublicRoutes.isPublic(HttpMethod.DELETE, "/pets/1")).isFalse();
        assertThat(PublicRoutes.isPublic(HttpMethod.PUT, "/pets/1")).isFalse();
        assertThat(PublicRoutes.isPublic(HttpMethod.GET, "/users/admin")).isFalse();
        assertThat(PublicRoutes.isPublic(HttpMethod.DELETE, "/users/1")).isFalse();
        assertThat(PublicRoutes.isPublic(HttpMethod.GET, "/auth/login")).isFalse();
    }
}
