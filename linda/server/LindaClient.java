package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.rmi.Naming;
import java.util.Collection;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    private LindaServer server;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        this.server = Naming.lookup(serverURI);
    }

    @Override
    public void write(Tuple t) {
        this.server.write(t);
    }

    @Override
    public Tuple take(Tuple template) {
        return null;
    }

    @Override
    public Tuple read(Tuple template) {
        return null;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return null;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

    }

    @Override
    public void debug(String prefix) {

    }

    // TO BE COMPLETED

}
