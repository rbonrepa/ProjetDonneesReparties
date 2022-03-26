package linda.server;

import java.io.Serializable;

import linda.*;
import linda.Linda.eventMode;

// Classe permettant de lier un Callback avec Un CallbackClient

public class CallbackTemplateServer implements Serializable {
    private Callback callback;
    private eventMode mode;
    private Tuple tuple;
    private LindaClientCallbackInterface client;
    private int id;

    public CallbackTemplateServer(Callback callback2, eventMode m, Tuple t, LindaClientCallbackInterface client2, int id) {
        this.callback = callback2;
        this.tuple = t;
        this.mode = m;
        this.client = client2;
        this.id = id;
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

    public int getID() {
        return this.id;
    }

    public LindaClientCallbackInterface getClient() {
        return this.client;
    }
}
