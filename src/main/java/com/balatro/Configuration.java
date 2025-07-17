package com.balatro;

public sealed class Configuration permits BalatroImpl {

    protected boolean analyzeTags;
    protected boolean analyzeBoss;
    protected boolean analyzeVoucher;
    protected boolean analyzeShopQueue;

    protected boolean analyzeStandardPacks;
    protected boolean analyzeCelestialPacks;
    protected boolean analyzeJokers;
    protected boolean analyzeArcana;
    protected boolean analyzeSpectral;

    protected boolean freshProfile;
    protected boolean freshRun = true;
    protected boolean showman;

    public boolean isAnalyzePacks() {
        return analyzeCelestialPacks || analyzeStandardPacks || analyzeSpectral || analyzeArcana || analyzeJokers;
    }
}