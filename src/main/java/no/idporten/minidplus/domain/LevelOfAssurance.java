package no.idporten.minidplus.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum LevelOfAssurance {

    UNKNOWN("unknown", 0, "unknown"),
    LEVEL3("Level3", 3, "substantial"),
    LEVEL4("Level4", 4, "high"),
    LEVEL5("Level5", 5, "highest");


    private String externalName;
    private String eidasExternalName;
    private int level;

    LevelOfAssurance(String externalName, int level, String eidasExternalName) {
        this.externalName = externalName;
        this.level = level;
        this.eidasExternalName = eidasExternalName;
    }

    public static LevelOfAssurance resolve(String externalName) {
        return Arrays.stream(values()).filter(levelOfAssurance -> levelOfAssurance.externalName.equalsIgnoreCase(externalName)).findFirst().orElse(UNKNOWN);
    }

}