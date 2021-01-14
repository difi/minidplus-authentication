package no.idporten.minidplus.web;

import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import no.idporten.sdk.oidcserver.protocol.TokenRequest;
import no.idporten.sdk.oidcserver.protocol.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MinIdPlusAuthorizeV2ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private OpenIDConnectIntegration openIDConnectSDK;

    @Nested
    @DisplayName("When receiving a pushed authorization request")
    class PushedAuthorizationRequestTests {

        @org.junit.jupiter.api.Test
        @DisplayName("then unauthorized requests gives an error response")
        public void testUnauthorizedPushedAuthorizationRequest() throws Exception {
            mockMvc.perform(
                    post("/v2/par")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("client_id", "unknown")
                            .param("client_secret", "secret"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", is("invalid_client")))
                    .andExpect(jsonPath("$.error_description", containsString("Invalid client authentication")));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("then invalid requests gives an error response")
        public void testInvalidPushedAuthorizationRequest() throws Exception {
            mockMvc.perform(
                    post("/v2/par")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("client_id", "junit")
                            .param("client_secret", "secret"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", is("invalid_request")))
                    .andExpect(jsonPath("$.error_description", containsString("redirect_uri")));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("then valid requests gives successful response with a request_uri")
        public void testValidPushedAuthorizationRequest() throws Exception {
            mockMvc.perform(
                    post("/v2/par")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("client_id", "junit")
                            .param("client_secret", "secret")
                            .param("response_type", "code")
                            .param("redirect_uri", "https://idporten.no/junit")
                            .param("scope", "openid")
                            .param("state", "florida")
                            .param("nonce", "nonstop"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.request_uri").exists())
                    .andExpect(jsonPath("$.expires_in").isNumber())
                    .andReturn();
        }
    }


    @Nested
    @DisplayName("When receiving a token request")
    class TokenRequestTests {

        @org.junit.jupiter.api.Test
        @DisplayName("then invalid request gives an error response")
        public void testInvalidTokenRequest() throws Exception {
            mockMvc.perform(
                    post("/v2/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("client_id", "junit")
                            .param("client_secret", "secret")
                            .param("grant_type", "authorization_code")
                            .param("code", "invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", is("invalid_grant")))
                    .andExpect(jsonPath("$.error_description", containsString("does not exist")));
        }

        @org.junit.jupiter.api.Test
        @DisplayName("then valid request gives an token response")
        public void testValidTokenRequest() throws Exception {
            doReturn(TokenResponse.builder()
                    .accessToken("at")
                    .idToken("it")
                    .tokenType("Bearer")
                    .expiresInSeconds(10)
                    .build())
                    .when(openIDConnectSDK)
                    .process(any(TokenRequest.class));
            mockMvc.perform(
                    post("/v2/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("client_id", "junit")
                            .param("client_secret", "secret")
                            .param("grant_type", "authorization_code")
                            .param("code", "valid"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id_token", is("it")))
                    .andExpect(jsonPath("$.access_token", is("at")))
                    .andExpect(jsonPath("$.token_type", is("Bearer")))
                    .andExpect(jsonPath("$.expires_in", is(10)));
        }
    }

}
