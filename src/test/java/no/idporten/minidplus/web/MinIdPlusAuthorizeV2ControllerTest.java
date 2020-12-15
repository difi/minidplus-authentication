package no.idporten.minidplus.web;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.domain.AuthorizationRequest;
import no.idporten.minidplus.domain.LevelOfAssurance;
import no.idporten.minidplus.service.AuthenticationService;
import no.idporten.minidplus.service.MinidPlusCache;
import no.idporten.minidplus.service.ServiceproviderService;
import no.idporten.sdk.oidcserver.OpenIDConnectIntegration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"minid-plus.serverid=testserver"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MinIdPlusAuthorizeV2ControllerTest {

    protected final ServiceProvider sp = new ServiceProvider("idporten");
    private final String pid = "23079421189";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    MinidPlusCache minidPlusCache;

    @MockBean
    ServiceproviderService serviceproviderService;

    @MockBean
    OpenIDConnectIntegration openIDConnectIntegration;

    @Test
    public void test_authorization_session_parameters_set() throws Exception {
        //TODO!!
    }

    private AuthorizationRequest getAuthorizationRequest() {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.setRedirectUri("http://localhost");
        ar.setLocale("nb");
        ar.setState("123");
        ar.setGotoParam("http://localhost");
        ar.setSpEntityId("NAV");
        ar.setResponseType("authorization_code");
        ar.setAcrValues(LevelOfAssurance.LEVEL4);
        return ar;
    }
}
