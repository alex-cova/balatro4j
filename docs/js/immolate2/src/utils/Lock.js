// import { ItemImpl } from "./interface/Item.js";
// import { JokerImpl } from "./interface/Joker.js";
import LOCKED_ITEMS from "../constants/lockedItems";

export class Lock {
    constructor() {
        this.locked = new Set();
    }

    static firstLock = new Set(LOCKED_ITEMS.FIRST_LOCK);

    static ante2Lock = new Set(LOCKED_ITEMS.ANTE_2_LOCK);

    // Helper method to extract common functionality
    processItem(item, action) {
        if (typeof item === 'string') {
            action(item);
        } else if (Array.isArray(item)) {
            item.forEach(i => action(i));
            //TODO implement this for ItemImpl and JokerImpl
            // } else if (item instanceof ItemImpl || item instanceof JokerImpl) {
            //     action(item.getName());
        } else {
            throw new Error("Invalid argument type");
        }
    }

    lock(item) {
        this.processItem(item, name => this.locked.add(name));
    }

    firstLock() {
        Lock.firstLock.forEach(item => this.locked.add(item));;
    }

    unlock(collection) {
       this.processItem(collection, name => this.locked.delete(name));
    }

    isLocked(item) {
        if (typeof item === 'string') {
            return this.locked.has(item);
            // } else if (item instanceof ItemImpl || item instanceof JokerImpl) {
            //     return this.locked.has(item.getName());
        } else {
            throw new Error(`Invalid argument type: ${typeof item}. Expected string, array, or Item/Joker instance.`);
        }
    }

    initLocks(ante, freshProfile, freshRun) {
        if (ante < 2) this.lock(Array.from(Lock.ante2Lock));
        if (ante < 3) this.lock(["The Toot", "The Eye"]);
        if (ante < 4) this.lock("The Plant");
        if (ante < 5) this.lock("The Serpent");
        if (ante < 6) this.lock("The Ox");

        if (freshProfile) {
            this.lock([
                ...LOCKED_ITEMS.FRESH_PROFILE.FIRST_SET,
                ...LOCKED_ITEMS.FRESH_PROFILE.SECOND_SET,
                ...LOCKED_ITEMS.FRESH_PROFILE.THIRD_SET
            ]);
        }

        if (freshRun) {
            this.lock(LOCKED_ITEMS.FRESH_RUN);
        }
    }

    initUnlocks(ante, freshProfile) {
        if (ante === 2) {
            this.unlock([
                "The Mouth", "The Fish", "The Wall", "The House", "The Mark",
                "The Wheel", "The Arm", "The Water", "The Needle", "The Flint"
            ]);
            this.unlock([
                "Standard Tag", "Meteor Tag", "Buffoon Tag", "Handy Tag",
                "Garbage Tag", "Ethereal Tag", "Top-up Tag", "Orbital Tag"
            ]);
            if (!freshProfile) this.unlock("Negative Tag");
        }

        if (ante == 3) {
            this.unlock("The Tooth");
            this.unlock("The Eye");
        }
        if (ante == 4) this.unlock("The Plant");
        if (ante == 5) this.unlock("The Serpent");
        if (ante == 6) this.unlock("The Ox");
    }
}