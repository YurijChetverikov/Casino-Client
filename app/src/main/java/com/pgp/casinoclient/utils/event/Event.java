package com.pgp.casinoclient.utils.event;


import com.pgp.casinoclient.utils.event.eventArgs.EventArgs;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Event {
    private Set<Consumer<EventArgs>> listeners = new HashSet();

    public void AddListener(Consumer<EventArgs> listener) {
        listeners.add(listener);
    }

    public void Fire(EventArgs args) {
        listeners.forEach(x -> x.accept(args));
    }
}