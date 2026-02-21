package com.balatro.enums;

public enum PackKind {
    Celestial("Celestial"),
    Arcana("Arcana"),
    Standard("Standard"),
    Buffoon("Buffoon"),
    Legendary("Legendary"),
    Spectral("Spectral");

    private final String name;

    PackKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isLegendaryEnabled() {
        return this == Spectral || this == Arcana;
    }
}
