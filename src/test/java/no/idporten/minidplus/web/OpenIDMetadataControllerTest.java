package no.idporten.minidplus.web;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OpenIDMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Nested
    @DisplayName("When receiving a request for OpenID Connect discovery")
    class DiscoveryRequestTests {

        @Test
        @DisplayName("then metadata is returned")
        public void testOpenIDConnectDiscovery() throws Exception {
            mockMvc.perform(get("/v2/.well-known/openid-configuration"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.issuer", is("http://localhost:8080/v2")))
                    .andExpect(jsonPath("$.pushed_authorization_request_endpoint", is("http://localhost:8080/v2/par")))
                    .andExpect(jsonPath("$.authorization_endpoint", is("http://localhost:8080/v2/authorize")))
                    .andExpect(jsonPath("$.token_endpoint", is("http://localhost:8080/v2/token")))
                    .andExpect(jsonPath("$.jwks_uri", is("http://localhost:8080/v2/jwks")));
        }
    }

    @Nested
    @DisplayName("When receiving a request for JSON web key set")
    class JsonWebKeySetRequestTests {

        @Test
        @DisplayName("then JSON web keys are returned without private keys")
        public void testJsonWebKeys() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/v2/jwks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.keys").isArray())
                    .andExpect(jsonPath("$.keys.length()", is(1)))
                    .andReturn();
            JWKSet jwkSet = JWKSet.parse(mvcResult.getResponse().getContentAsString());
            JWK jwk = jwkSet.getKeys().get(0);
            assertFalse(jwk.isPrivate());
        }
    }

}
