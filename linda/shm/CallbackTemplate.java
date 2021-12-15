package linda.shm;

import linda.*;
import linda.Linda.eventMode;

public class CallbackTemplate {
    private Callback callback;
    private eventMode mode;
    private Tuple tuple;

    public CallbackTemplate(Callback c, eventMode m, Tuple t) {
        this.callback = c;
        this.tuple = t;
        this.mode = m;
    }

    public Callback getCallback() {
        return this.callback;
    }

    public Tuple getTuple() {
        return this.tuple;
    }

    public eventMode getMode() {
        return this.mode;
    }
}
