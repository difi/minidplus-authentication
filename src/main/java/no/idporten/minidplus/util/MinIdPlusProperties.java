package no.idporten.minidplus.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contains constants for names in the MinidPlus module properties-file.
 */
@Component
@Getter
public class MinIdPlusProperties {

        @Value("${minid-plus.logging.categoryname}")
        private String minidPlusLoggingCategoryName;
}