package no.idporten.minidplus.linkmobility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class SmsAllowedFilter {

    public static final String PREFIX = "/";
    private final FilterConfig config;
    private Set<String> authorizedNumbers = new HashSet<>();

    /**
     * Init authorized mobile numbers from file.
     */
    void loadNumbers() {
        if (!config.isEnabled()) {
            return;
        }
        if (config.getFilename() == null) {
            log.error("Authorized numbers list filename is null");
            return;
        }
        String filename = config.getFilename();
        if (!filename.startsWith(PREFIX)) {
            filename = PREFIX + filename;
        }
        log.info("Reading authorized numbers from " + filename);
        Set<String> mobileNumbers = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(SmsAllowedFilter.class.getResourceAsStream(filename)))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotEmpty(line)) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        mobileNumbers.add(line.trim());
                    }
                }
            }
            if(log.isDebugEnabled()) {
                log.debug("Accepting the following numbers: " + mobileNumbers);
            }
            authorizedNumbers = mobileNumbers;
        } catch (Exception e) {
            log.error("Failed to read mobile numbers from input file [" + filename + "]", e);
        }
    }

    boolean isAllowed(String number) {
        return !config.isEnabled() || authorizedNumbers.contains(number);
    }

}
