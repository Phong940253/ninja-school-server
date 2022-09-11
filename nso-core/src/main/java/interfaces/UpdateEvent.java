package interfaces;

import real.Ninja;

@FunctionalInterface
public interface UpdateEvent {
    void update(Ninja nj);
}
