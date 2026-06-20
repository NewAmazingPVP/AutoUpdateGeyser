package common;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RestartGate {
    private final AtomicBoolean restartScheduled = new AtomicBoolean(false);

    public boolean markScheduled() {
        return restartScheduled.compareAndSet(false, true);
    }
}
