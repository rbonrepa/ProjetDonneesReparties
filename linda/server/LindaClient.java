package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.rmi.Naming;
import java.util.ArrayList;
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
        try {
            this.server = (LindaServer) Naming.lookup(serverURI);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            this.server.write(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            this.server.take(template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return template;
    }

    

    @Override
    public Tuple read(Tuple template) {
        return template;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        return template;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        return template;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        return new ArrayList<Tuple>();
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        return new ArrayList<Tuple>();
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

    }

    @Override
    public void debug(String prefix) {
        try {
            server.debug(prefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client lancé");
        
        Tuple t1 = new Tuple(1,2);
        client.write(t1);
        client.debug("");
    }

}
