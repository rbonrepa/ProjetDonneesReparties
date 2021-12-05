package linda.shm;

import linda.*;

public class CallbackTemplate {
    private Callback callback;
    private Integer mode;
    private Tuple tuple;

    public CallbackTemplate(Callback c, Integer m, Tuple t) {
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

    public Integer getMode() {
        return this.mode;
    }
}
