package no.idporten.minidplus.linkmobility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(properties = {"minid-plus.sms-filter.filename=difi-mobile-numbers-test.txt"})
@RunWith(SpringRunner.class)
@Profile("test")
public class SmsAllowedFilterTest {

    @Autowired
    SmsAllowedFilter smsAllowedFilter;

    @Autowired
    FilterConfig filterConfig;

    @Test
    public void read_file_test() {
        assertTrue(smsAllowedFilter.getConfig().isEnabled());
        smsAllowedFilter.loadNumbers();
        assertEquals(5, smsAllowedFilter.getAuthorizedNumbers().size());
    }

    @Test
    public void test_allowed_ok() {
        assertTrue(smsAllowedFilter.isAllowed("40436656"));
    }

    @Test
    public void test_not_allowed_nok() {
        assertFalse(smsAllowedFilter.isAllowed("99286853"));
    }

    @Test
    public void test_not_allowed_ok_if_filter_not_enabled() {
        filterConfig.setEnabled(false);
        assertTrue(smsAllowedFilter.isAllowed("99286853"));
        filterConfig.setEnabled(true);
    }

}
