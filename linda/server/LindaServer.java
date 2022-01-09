package linda.server;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Semaphore;
import javax.security.auth.callback.Callback;
import linda.Tuple;
import linda.Linda.*;
import linda.shm.*;

public class LindaServer extends UnicastRemoteObject implements LindaServerInterface{

    private Map<Integer,List<Tuple>> tuplespace;  //liste des tuples en mémoire partagée
    private List<SemaphoreTemplate> semaphorespace;  //Liste des semaphores pour les read/take en attente
    private List<CallbackTemplate> callbackspace;    //Liste des Callbacks en attente
    private Semaphore mutex;   //Utilisé pour que l'accès à la mémoire partagée se fasse par
    //un seul thread en même temps
	
    public LindaServer() throws RemoteException {
        this.tuplespace = new HashMap<>();
        this.semaphorespace = new ArrayList<>();
        this.callbackspace = new ArrayList<>();
        this.mutex = new Semaphore(1);
    }

    public void write(Tuple t) throws java.rmi.RemoteException {
         try {
            mutex.acquire();
        } catch (InterruptedException e) {} 
        //On va accéder à la mémoire partagée donc on bloque toute autre intéraction
        //pouvant être effectuée dessus par un autre thread

        Integer size = t.size();
        if (this.tuplespace.containsKey(size)) {
            this.tuplespace.get(size).add(t);
        }
        else {
            this.tuplespace.put(size,new ArrayList<Tuple>());
            this.tuplespace.get(size).add(t);
        }

        //Ce bloc va débloquer un thread en attente de read ou take en releasant la sémaphore
        //associée si elle vient d'être ajoutée 
        Iterator<SemaphoreTemplate> it = this.semaphorespace.iterator();
        boolean takeTriggered = false;
        while (it.hasNext()) {
            SemaphoreTemplate st = it.next();
            if (st.getTuple().matches(t)) {
                st.getSemaphore().release();
                if (st.getMode() == eventMode.TAKE) {
                    takeTriggered = true;
                }
                it.remove();
                break;
            }
        }

        //Débloquage potentiel des callbacks
        Iterator<CallbackTemplate> it2 = this.callbackspace.iterator();
        while (it2.hasNext()) {
            CallbackTemplate elmt = it2.next();
            if (t.matches(elmt.getTuple())) {
                if (elmt.getMode() == eventMode.TAKE && !takeTriggered) {
                    this.tuplespace.get(t.size()).remove(t);
                    elmt.getCallback().call(t);
                    it2.remove();
                    break;
                }
                else if (elmt.getMode() == eventMode.READ) {
                    elmt.getCallback().call(t);
                    it2.remove();
                    break;
                }
            }
                
        }
        mutex.release(); //On laisse les autres thread intéragir avec la mémoire partagée
    }

    public Tuple take(Tuple template) throws java.rmi.RemoteException {
        try {
            mutex.acquire();
        } catch (InterruptedException e1) {}

        Integer size = template.size();
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    it.remove();
                    mutex.release();
                    return elmt;
                }
            }
        }
        //Si on arrive ici, alors le motif n'est pas encore présent dans la mémoire
        mutex.release();

        //On fait une attente bloquante
        Semaphore s = new Semaphore(0);
        this.semaphorespace.add(new SemaphoreTemplate(s, template, eventMode.TAKE));
        
        //Attente bloquante qu'un élément au motif recherche apparaisse dans la mémoire
        try {
        s.acquire();
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        Iterator<Tuple> it = this.tuplespace.get(size).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
             if (elmt.matches(template)) {
                it.remove();
                mutex.release();
                return elmt;
            }
        }
        return null; //ERREUR SI ON ARRIVE ICI
    }

    public Tuple read(Tuple template) throws java.rmi.RemoteException {
        return template;
    }

    public Tuple tryTake(Tuple template) throws java.rmi.RemoteException {
        return template;
    }

    public Tuple tryRead(Tuple template) throws java.rmi.RemoteException {
        return template;
    }

    public Collection<Tuple> takeAll(Tuple template) throws java.rmi.RemoteException {
        return new ArrayList<>();
    }

    public Collection<Tuple> readAll(Tuple template) throws java.rmi.RemoteException {
        return new ArrayList<>();
    }

    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) throws java.rmi.RemoteException {

    }

    public void debug(String prefix) throws java.rmi.RemoteException {
        if (prefix.equals("callback")){
            for(CallbackTemplate t: callbackspace)
            {
                System.out.println(t.getTuple());
            }
        }
        else{
            System.out.println(tuplespace);
        }
        
    }

    public static void main(String[] args) {
        try {
            LindaServer server = new LindaServer(); 

            Registry registry = LocateRegistry.createRegistry(1099);

            Naming.rebind("//localhost:1099/LindaServer", server);
            System.out.println("Serveur Linda lancé");
            /*
            LindaClient client = new LindaClient("//localhost:1099/LindaServer");
            System.out.println("Client lancé");
            
            Tuple t1 = new Tuple(1,2);
            client.write(t1);
            client.debug("");
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

}
