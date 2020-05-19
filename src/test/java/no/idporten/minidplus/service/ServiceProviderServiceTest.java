package no.idporten.minidplus.service;

import no.idporten.domain.sp.ServiceProvider;
import no.idporten.minidplus.config.CacheConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static no.idporten.minidplus.service.ServiceproviderService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"spring.cache.type=jcache"})
@ContextConfiguration(classes = {ServiceproviderService.class, CacheConfiguration.class})
public class ServiceProviderServiceTest {

    @Autowired
    ServiceproviderService serviceproviderService;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    LdapTemplate ldapTemplate;

    @Test
    public void test_no_service_provider_found_gives_default_values() {
        ServiceProvider sp = serviceproviderService.getServiceProvider("test", "localhost");
        assertEquals(DEFAULT_SP_NAME, sp.getName());
        assertEquals(DEFAULT_SP_LOGO_PATH, sp.getLogoPath());
        assertEquals(DEFAULT_SP_URL, sp.getUrl());
    }

    @Test
    public void test_service_provider_found_gives_correct_values() {
        ServiceProvider expected = new ServiceProvider("nav");
        expected.setName("NAV");
        expected.setUrl("https://nav.no");
        expected.setLogoPath("nav.gif");
        when(serviceproviderService.findByEntityIdFilter("nav")).thenReturn(Collections.singletonList(expected));
        ServiceProvider sp = serviceproviderService.getServiceProvider("nav", "hei.no");
        assertEquals(expected.getName(), sp.getName());
        assertEquals(OPENSSO_IMAGES_FOLDER + "nav.gif", sp.getLogoPath());
        assertEquals(expected.getUrl(), sp.getUrl());
    }

    @Test
    public void test_service_provider_fails_to_get_logo_sets_default_logo() {

        ServiceProvider expected = new ServiceProvider("nav");
        expected.setName("NAV");
        expected.setUrl("https://nav.no");
        expected.setLogoPath("nav.gif");

        when(restTemplate.getForObject(eq("https://localhost" + OPENSSO_IMAGES_FOLDER + expected.getLogoPath()), eq(Object.class))).thenThrow(new RuntimeException("not found"));
        when(serviceproviderService.findByEntityIdFilter("nav")).thenReturn(Collections.singletonList(expected));
        ServiceProvider sp = serviceproviderService.getServiceProvider("nav", "localhost");
        assertEquals(expected.getName(), sp.getName());
        assertEquals(DEFAULT_SP_LOGO_PATH, sp.getLogoPath());
        assertEquals(expected.getUrl(), sp.getUrl());
    }

}
