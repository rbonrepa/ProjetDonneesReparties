package linda.server;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

import linda.Callback;
import linda.Tuple;

// Implémentation du "Serveur" des callbacks côté client


public class LindaClientCallback extends UnicastRemoteObject implements LindaClientCallbackInterface,Serializable {

    private ArrayList<CallbackTemplateServer> listeCallback;

    public LindaClientCallback() throws RemoteException {
        this.listeCallback = new ArrayList<>();
    }

    // Ajouter un CallBackTemplateServer à la liste
    public void add(CallbackTemplateServer callback) throws RemoteException {
        this.listeCallback.add(callback);
    }

    // Le serveur a matché sur un Template d'un callback, cette méthode vérifie les ids des callbacks et déclenche le call si les IDs correspondent
    public void callbackCheck(CallbackTemplateServer callback, Tuple t) throws RemoteException {
        Iterator<CallbackTemplateServer> it = this.listeCallback.iterator();
        while (it.hasNext()) {
            CallbackTemplateServer elmt = it.next();
            if (callback.getID() == elmt.getID() ) {
                elmt.getCallback().call(t);
                it.remove();
                break;
            }
        }
    }

}
