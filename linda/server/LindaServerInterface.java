package linda.server;
import java.util.Collection;
import javax.security.auth.callback.Callback;
import linda.Tuple;
import linda.Linda.*;

public interface LindaServerInterface extends java.rmi.Remote {

    public void write(Tuple t) throws java.rmi.RemoteException;

    public Tuple take(Tuple template) throws java.rmi.RemoteException;

    public Tuple read(Tuple template) throws java.rmi.RemoteException;

    public Tuple tryTake(Tuple template) throws java.rmi.RemoteException;

    public Tuple tryRead(Tuple template) throws java.rmi.RemoteException;

    public Collection<Tuple> takeAll(Tuple template) throws java.rmi.RemoteException;

    public Collection<Tuple> readAll(Tuple template) throws java.rmi.RemoteException;

    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) throws java.rmi.RemoteException;

    public void debug(String prefix) throws java.rmi.RemoteException;


}