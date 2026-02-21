package com.balatro.structs;

public class JokerStickers {
    private boolean eternal;
    private boolean perishable;
    private boolean rental;

    public JokerStickers() {
        this.eternal = false;
        this.perishable = false;
        this.rental = false;
    }

    public boolean isEternal() {
        return eternal;
    }

    public void setEternal(boolean eternal) {
        this.eternal = eternal;
    }

    public boolean isPerishable() {
        return perishable;
    }

    public void setPerishable(boolean perishable) {
        this.perishable = perishable;
    }

    public boolean isRental() {
        return rental;
    }

    public void setRental(boolean rental) {
        this.rental = rental;
    }
}