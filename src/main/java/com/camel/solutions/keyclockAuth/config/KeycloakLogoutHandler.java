package com.camel.solutions.keyclockAuth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Component
public class KeycloakLogoutHandler implements LogoutHandler {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakLogoutHandler.class);
    private final RestTemplate restTemplate;

    public KeycloakLogoutHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) authentication.getPrincipal();
            logoutFromKeycloak(user);
        }
    }

    private void logoutFromKeycloak(OidcUser user) {
        String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
        OidcIdToken idToken = user.getIdToken();
        if (idToken != null) {
            String idTokenValue = idToken.getTokenValue();
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(endSessionEndpoint)
                    .queryParam("id_token_hint", idTokenValue);

            try {
                ResponseEntity<String> logoutResponse = restTemplate.getForEntity(
                        builder.toUriString(), String.class);

                if (logoutResponse.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                    logger.info("Successfully logged out from Keycloak");
                    SecurityContextHolder.clearContext();
                } else {
                    logger.error("Failed to log out from Keycloak");
                }
            } catch (RestClientException e) {
                logger.error("Error occurred while logging out from Keycloak", e);
            }
        } else {
            logger.error("Id token not found in the authentication object.");
        }
    }
}
