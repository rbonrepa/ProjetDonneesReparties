package linda.server;
import java.rmi.*;
import java.util.ArrayList;

import linda.Callback;
import linda.Tuple;

// Interface du "serveur" gérant les callbacks côté Client

public interface LindaClientCallbackInterface extends Remote {

    void add(CallbackTemplateServer callback) throws RemoteException;

    void callbackCheck(CallbackTemplateServer callback, Tuple t) throws RemoteException;
}