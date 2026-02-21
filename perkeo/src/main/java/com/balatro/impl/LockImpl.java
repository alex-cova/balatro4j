package com.balatro.impl;

import com.balatro.api.Item;
import com.balatro.api.Lock;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class LockImpl implements Lock {

    private final Set<String> locked;

    public LockImpl() {
        locked = new HashSet<>();
    }

    public void lock(String item) {
        locked.add(item);
    }

    private void lock(Collection<String> collection) {
        locked.addAll(collection);
    }

    public void unlock(String item) {
        locked.remove(item);
    }

    public void unlock(@NotNull Item item) {
        unlock(item.getName());
    }

    public void unlock(Collection<String> collection) {
        locked.removeAll(collection);
    }

    public void lock(@NotNull Item item) {
        lock(item.getName());
    }

    public boolean isLocked(@NotNull Item item) {
        return isLocked(item.getName());
    }

    public boolean isLocked(String item) {
        return locked.contains(item);
    }

}
