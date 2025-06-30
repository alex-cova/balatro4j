package com.balatro.structs;

import com.balatro.enums.Deck;
import com.balatro.enums.Stake;
import com.balatro.enums.Version;
import com.balatro.enums.Voucher;
import org.jetbrains.annotations.NotNull;

public class InstanceParams {

    private Deck deck;
    private Stake stake;
    private boolean showman;
    public int sixesFactor;
    public long version;
    public int vouchers;

    public InstanceParams(Deck deck, Stake stake, boolean showman, @NotNull Version version) {
        this.deck = deck;
        this.stake = stake;
        this.showman = showman;
        this.sixesFactor = 1;
        this.version = version.getVersion();
        this.vouchers = 0;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Stake getStake() {
        return stake;
    }

    public void setStake(Stake stake) {
        this.stake = stake;
    }

    public boolean isShowman() {
        return showman;
    }

    public void setShowman(boolean showman) {
        this.showman = showman;
    }

    public int getSixesFactor() {
        return sixesFactor;
    }

    public void setSixesFactor(int sixesFactor) {
        this.sixesFactor = sixesFactor;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void activateVoucher(@NotNull Voucher voucher) {
        vouchers = vouchers | (1 << voucher.ordinal());
    }

    public boolean isVoucherActive(@NotNull Voucher voucher) {
        return (vouchers & (1 << voucher.ordinal())) != 0;
    }
}