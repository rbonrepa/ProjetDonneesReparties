package linda.server;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Semaphore;
import linda.Callback;
import linda.Tuple;
import linda.Linda.*;
import linda.shm.*;
import linda.shm.CallbackTemplate;

public class LindaServer extends UnicastRemoteObject implements LindaServerInterface{

    // STRUCTURE de données pour les occurences des tuples
    // MAP de Integer (size) x MAP ()
    private Map<Integer,List<Tuple>> cache;  //liste des tuples en mémoire partagée
    private List<SemaphoreTemplate> semaphorespace;  //Liste des semaphores pour les read/take en attente
    private List<CallbackTemplateServer> callbackspace;    //Liste des Callbacks en attente
    private Semaphore mutex;   //Utilisé pour que l'accès à la mémoire partagée se fasse par
    //un seul thread en même temps

    //Utilisé pour afficher (ou non) ce que le serveur fait dans la console
    public boolean debugActivated = false;
	
    public LindaServer() throws RemoteException {
        this.cache = new HashMap<>();
        this.semaphorespace = new ArrayList<>();
        this.callbackspace = new ArrayList<>();
        this.mutex = new Semaphore(1);
    }

    public void write(Tuple t) throws java.rmi.RemoteException {
         try {
            if (debugActivated) {System.out.println("Ecriture de " + t.toString() + " en attente.");}
            mutex.acquire();
        } catch (InterruptedException e) {} 
        //On va accéder à la mémoire partagée donc on bloque toute autre intéraction
        //pouvant être effectuée dessus par un autre thread

        //Selon si le client dont est issu le tuple on créée une nouvelle clé dans le Hashmap ou non
        if (this.cache.containsKey(t.size())) {
            this.cache.get(t.size()).add(t);
        }
        else {
            this.cache.put(t.size(),new ArrayList<Tuple>());
            this.cache.get(t.size()).add(t);
        }

        //Ce bloc va débloquer un thread en attente de read ou take en releasant la sémaphore
        //associée si elle vient d'être ajoutée 
        Iterator<SemaphoreTemplate> it = this.semaphorespace.iterator();
        boolean takeTriggered = false;
        while (it.hasNext()) {
            SemaphoreTemplate st = it.next();
            
            if (t.matches(st.getTuple())) {
                if (debugActivated) {System.out.println("La sémaphore associée au tuple " + st.getTuple().toString() + " est débloquée.");}
                st.getSemaphore().release();
                if (st.getMode() == eventMode.TAKE) {
                    takeTriggered = true;
                }
                it.remove();
                break;
            }
        }

        //Débloquage potentiel des callbacks
        Iterator<CallbackTemplateServer> it2 = this.callbackspace.iterator();
        while (it2.hasNext()) {
            CallbackTemplateServer elmt = it2.next();
            if (t.matches(elmt.getTuple())) {
                if (debugActivated) {System.out.println("Le callback associé au tuple " + elmt.getTuple() + " est activé.");}
                if (elmt.getMode() == eventMode.TAKE && !takeTriggered) {
                    this.cache.get(t.size()).remove(t);
                    removeIfKeyEmpty(t.size());
                    elmt.getClient().callbackCheck(elmt, t); // Il y a un match, on déclenche le callbackCheck côté client
                    it2.remove();
                    break;
                }
                else if (elmt.getMode() == eventMode.READ) {
                    elmt.getClient().callbackCheck(elmt, t); // Il y a un match, on déclenche le callbackCheck côté client
                    it2.remove();
                    break;
                }
            }
                
        }
        mutex.release(); //On laisse les autres thread intéragir avec la mémoire partagée

        if (debugActivated) {System.out.println("Ecriture de " + t.toString() + " finie.");
            System.out.print("Etat cache : ");
            debug("");
        }
    }

    public Tuple take(Tuple template) throws java.rmi.RemoteException {
        try {
            if (debugActivated) {System.out.println("Demande de take de " + t.toString() + " en attente.");}
            mutex.acquire();
        } catch (InterruptedException e1) {}

        if (this.cache.containsKey(t.size())) {
            Iterator<Tuple> it = this.cache.get(t.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(t)) {
                    it.remove();
                    removeIfKeyEmpty(t.size());
                    mutex.release();
                    if (debugActivated) {System.out.println("Le motif " + t.toString() + " est déjà présent dans la mémoire, take de l'élément : " + elmt.toString());}
                    return elmt;
                }
            }
        }
        //Si on arrive ici, alors le motif n'est pas encore présent dans la mémoire
        mutex.release();

        //On fait une attente bloquante
        Semaphore s = new Semaphore(0);
        this.semaphorespace.add(new SemaphoreTemplate(s, t, eventMode.TAKE));
        
        //Attente bloquante qu'un élément au motif recherche apparaisse dans la mémoire
        if (debugActivated) {System.out.println("Le motif n'est pas présent dans la mémoire --> take mis en attente.");}
        try {
            s.acquire();
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        {System.out.println("Le motif " + t.toString() + " est apparu dans la mémoire, un take va s'effectuer");}

        Iterator<Tuple> it = this.cache.get(t.size()).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
             if (elmt.matches(t)) {
                it.remove();
                removeIfKeyEmpty(t.size());
                mutex.release();
                if (debugActivated) {
                    System.out.println("Le motif " + t.toString() + " a take l'élément : " + elmt.toString());
                    System.out.print("Etat cache : ");
                    debug("");
                }
                return elmt;
            }
        }
        return null; //ERREUR SI ON ARRIVE ICI
    }

    public Tuple read(Tuple template) throws java.rmi.RemoteException {
        try {
            if (debugActivated) {System.out.println("Demande de read de " + template.toString() + " en attente.");}
            mutex.acquire();
        } catch (InterruptedException e1) {}
        if (this.cache.containsKey(template.size())) {
            Iterator<Tuple> it = this.cache.get(template.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    mutex.release();
                    if (debugActivated) {System.out.println("Le motif " + template.toString() + " est déjà présent dans la mémoire, read de l'élément : " + elmt.toString());}
                    return elmt;
                }
            }
        }
       
        mutex.release();

        Semaphore s = new Semaphore(0);
        this.semaphorespace.add(new SemaphoreTemplate(s, template, eventMode.READ));

        //Attente bloquante
        if (debugActivated) {System.out.println("Le motif n'est pas présent dans la mémoire --> read mis en attente.");}
        try {
            s.acquire();
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Attente bloquante qu'un élément au motif recherche apparaisse dans la mémoire
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        {System.out.println("Le motif " + template.toString() + " est apparu dans la mémoire, un read va s'effectuer");}

        Iterator<Tuple> it = this.cache.get(template.size()).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
            if (elmt.matches(template)) {
                mutex.release();
                if (debugActivated) {System.out.println("Le motif " + template.toString() + " a été read et a renvoyé : " + elmt.toString());}
                return elmt;
            }
        }
        return null; //ERREUR SI ON ARRIVE ICI
    }

    public Tuple tryTake(Tuple template) throws java.rmi.RemoteException {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}
        Tuple res = null;
        if (this.cache.containsKey(template.size())) {
            Iterator<Tuple> it = this.cache.get(template.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    it.remove();
                    removeIfKeyEmpty(template.size());
                    res = elmt;
                    mutex.release();
                    if (debugActivated) {
                        System.out.println("Le tryTake du motif " + template.toString() + " a trouvé l'élément : " + res.toString());
                        System.out.print("Etat cache : ");
                        debug("");
                    }
                    return res;
                }
            }
            if (debugActivated) {System.out.println("Le tryTake du motif " + template.toString() + " n'a pas trouvé d'élément correspondant.");}
            //TODO faudrait tej ça
            mutex.release();
            return res;
        }
        if (debugActivated) {System.out.println("Le tryTake du motif " + template.toString() + " n'a pas trouvé d'élément correspondant.");}
        mutex.release();
        return res;
    }

    public Tuple tryRead(Tuple template) throws java.rmi.RemoteException {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        Tuple res = null;
        if (this.cache.containsKey(template.size())) {
            Iterator<Tuple> it = this.cache.get(template.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    res = elmt;
                    mutex.release();
                    if (debugActivated) {System.out.println("Le tryRead du motif " + template.toString() + " a trouvé l'élément : " + res.toString());}
                    return res;
                }
            }
            if (debugActivated) {System.out.println("Le tryRead du motif " + template.toString() + " n'a pas trouvé d'élément correspondant.");}
            mutex.release();
            return res;
        }
        if (debugActivated) {System.out.println("Le tryRead du motif " + template.toString() + " n'a pas trouvé d'élément correspondant.");}
        mutex.release();
        return res;
    }

    public Collection<Tuple> takeAll(Tuple template) throws java.rmi.RemoteException {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        ArrayList<Tuple> res = new ArrayList<>();
        if (this.cache.containsKey(template.size())) {
            Iterator<Tuple> it = this.cache.get(template.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    res.add(elmt);
                    it.remove();
                    removeIfKeyEmpty(template.size());
                }
            }
        }
        mutex.release();
        if (debugActivated) {
            System.out.println("Le takeAll du motif " + template.toString() + " a trouvé ces élements : " + res.toString());
            System.out.print("Etat cache : ");
            debug("");
        }
        return res;
    }

    public Collection<Tuple> readAll(Tuple template) throws java.rmi.RemoteException {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        ArrayList<Tuple> res = new ArrayList<>();
        if (this.cache.containsKey(template.size())) {
            Iterator<Tuple> it = this.cache.get(template.size()).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    res.add(elmt);
                }
            }
        }
        mutex.release();
        if (debugActivated) {System.out.println("Le readAll du motif " + template.toString() + " a trouvé ces élements : " + res.toString());}
        return res;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback, CallbackTemplateServer ct) throws java.rmi.RemoteException {        
        this.callbackspace.add(ct); // Enregistrement du CallbackTemplateServer dans le callbackspace
        if (timing == eventTiming.IMMEDIATE) { // Timing = IMMEDIATE, on déclenche callbackCheck
            this.callbackCheck(ct);
        }
    }

    private void callbackCheck(CallbackTemplateServer ct) {
        Integer size = ct.getTuple().size();
        if (this.cache.containsKey(size)) {
            Iterator<Tuple> it = this.cache.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(ct.getTuple())) {
                    if (ct.getMode() == eventMode.TAKE){
                        it.remove();
                        removeIfKeyEmpty(size);
                    }
                    try {
                        ct.getClient().callbackCheck(ct, elmt); // Il y a un match, on déclenche le callbackCheck côté client
                        System.out.println(cache);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.callbackspace.remove(ct);
                    break;
                    
                }
            }
        }
    }

    public void debug(String prefix) throws java.rmi.RemoteException {
        if (prefix.equals("callback")){
            for(CallbackTemplateServer t: callbackspace)
            {
                System.out.println(t.getTuple());
            }
        }
        else{
            System.out.println(cache);
        }
    }

    //Pour print le cache dans la console du client
    //Pourra-t-elle être utilisée finalement ? L'interface cache l'accès à la fonction :/
    public String debugClient() throws java.rmi.RemoteException {
        return cache.toString();
    }

    /**
     * Enlève la clé de taille cle si elle est vide (ne contient plus de tuple) dans cache
     * entier : cle est la taille du tuple, et aussi sa clé pour le retrouver dans le hashmap
     */
    public void removeIfKeyEmpty(int cle) {
        if (cache.containsKey(cle)) {
            if (cache.get(cle).isEmpty()) {
                cache.remove(cle);
            } 
        } else {
            System.out.println("Erreur dans removeIfKeyEmpty la clé est introuvable");
        }
    }
    
    public static void main(String[] args) {
        try {
            LindaServer server = new LindaServer();
            server.debugActivated = true; 

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
