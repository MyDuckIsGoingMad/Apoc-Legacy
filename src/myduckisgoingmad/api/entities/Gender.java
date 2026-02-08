package myduckisgoingmad.api.entities;

public enum Gender {
    MALE("Male"), FEMALE("Female");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromString(String str) {
        for (Gender gender : values()) {
            if (gender.displayName.equalsIgnoreCase(str)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + str);
    }
}
