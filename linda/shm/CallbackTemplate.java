package linda.shm;

import java.util.concurrent.Semaphore;
import linda.*;

public class CallbackTemplate {
    private Callback callback;
    private eventMode mode;
    private Tuple tuple;

    public CallbackTemplate(Callaback c, eventMode mode, Tuple t) {
        this.callback = c;
        this.tuple = t;
        this.mode = mode;
    }

    public Semaphore getCallback() {
        return this.callback;
    }

    public Tuple getTuple() {
        return this.tuple;
    }

    public eventMode getMode() {
        return this.mode;
    }
}
