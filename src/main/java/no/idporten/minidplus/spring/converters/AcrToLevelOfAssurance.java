package no.idporten.minidplus.spring.converters;


import no.idporten.minidplus.domain.LevelOfAssurance;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AcrToLevelOfAssurance implements Converter<String, LevelOfAssurance> {

    @Override
    public LevelOfAssurance convert(String s) {
        return Arrays.stream(s.split("\\s+"))
                .map(LevelOfAssurance::resolve)
                .filter(levelOfAssurance -> LevelOfAssurance.UNKNOWN != levelOfAssurance)
                .findFirst()
                .orElse(LevelOfAssurance.UNKNOWN);
    }

}
