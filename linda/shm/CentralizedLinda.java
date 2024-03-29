package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    private Map<Integer,List<Tuple>> tuplespace;  //liste des tuples en mémoire partagée
    private List<SemaphoreTemplate> semaphorespace;  //Liste des semaphores pour les read/take en attente
    private List<CallbackTemplate> callbackspace;    //Liste des Callbacks en attente
    private Semaphore mutex;   //Utilisé pour que l'accès à la mémoire partagée se fasse par
    //un seul thread en même temps
    private ExecutorService poule;

	
    public CentralizedLinda() {
        this.tuplespace = new HashMap<>();
        this.semaphorespace = new ArrayList<>();
        this.callbackspace = new ArrayList<>();
        this.mutex = new Semaphore(1);
        this.poule = Executors.newFixedThreadPool(4);
    }

    @Override
    public void write(Tuple t) {
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
        SemaphoreTemplate takeThread = null;
        while (it.hasNext()) {
            SemaphoreTemplate st = it.next();
            if (t.matches(st.getTuple())) {
                if (st.getMode() == eventMode.TAKE && !takeTriggered) {
                    takeTriggered = true;
                    takeThread = st;
                    it.remove();
                    
                }
                else if (st.getMode() == eventMode.READ) {
                    
                    st.getSemaphore().release();
                    it.remove();
                }
                
            }
        }
        if (takeTriggered) {
            takeThread.getSemaphore().release();
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
                    takeTriggered = true;
                }
                else if (elmt.getMode() == eventMode.READ) {
                    elmt.getCallback().call(t);
                    it2.remove();
                }
            }
                
        }

        mutex.release(); //On laisse les autres thread intéragir avec la mémoire partagée
               
    }


    @Override
    public Tuple take(Tuple template) {
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

        //On fait une attente bloquante
        Semaphore s = new Semaphore(0);
        this.semaphorespace.add(new SemaphoreTemplate(s, template, eventMode.TAKE));
        mutex.release();

        
        

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

    @Override
    public Tuple read(Tuple template) {
        try {
            mutex.acquire();
        } catch (InterruptedException e1) {}
        Integer size = template.size();
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    mutex.release();
                    return elmt;
                }
            }
       }

       Semaphore s = new Semaphore(0);
       this.semaphorespace.add(new SemaphoreTemplate(s, template, eventMode.READ));

       mutex.release();

        

        //Attente bloquante
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

        Iterator<Tuple> it = this.tuplespace.get(size).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
             if (elmt.matches(template)) {
                 mutex.release();
                 return elmt;
            }
        }
        return null; //ERREUR SI ON ARRIVE ICI    
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}
        Integer size = template.size();
        Tuple res = null;
        if (this.tuplespace.containsKey(size)) {
             Iterator<Tuple> it = this.tuplespace.get(size).iterator();
             while (it.hasNext()) {
                 Tuple elmt = it.next();
                 if (elmt.matches(template)) {
                     it.remove();
                     res = elmt;
                     mutex.release();
                     return res;
                 }
             }
             mutex.release();
             return res;
        }
        mutex.release();
        return res;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        Integer size = template.size();
        Tuple res = null;
        if (this.tuplespace.containsKey(size)) {
             Iterator<Tuple> it = this.tuplespace.get(size).iterator();
             while (it.hasNext()) {
                 Tuple elmt = it.next();
                 if (elmt.matches(template)) {
                     res = elmt;
                     mutex.release();
                     return res;
                 }
             }
             mutex.release();
             return res;
        }
        mutex.release();
        return res;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        Integer size = template.size();
        ArrayList<Tuple> res = new ArrayList<>();
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    res.add(elmt);
                    it.remove();
                }
            }
        }
        mutex.release();
        return res;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {}

        Integer size = template.size();
        ArrayList<Tuple> res = new ArrayList<>();
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    res.add(elmt);
                }
            }
        }
        mutex.release();
        return res;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {        
        CallbackTemplate ct = new CallbackTemplate(callback, mode, template);
        this.callbackspace.add(ct);
        if (timing == eventTiming.IMMEDIATE) {
            this.callbackCheck(ct);
        }
    }

    private void callbackCheck(CallbackTemplate ct) {
        Integer size = ct.getTuple().size();
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(ct.getTuple())) {
                    if (ct.getMode() == eventMode.TAKE){
                        it.remove();
                    }
                    ct.getCallback().call(elmt);
                    this.callbackspace.remove(ct);
                    break;
                    
                }
            }
        }
    }

    @Override
    public void debug(String prefix) {
        if (prefix.equals("callback")){
            for(CallbackTemplate t: callbackspace)
            {
                System.out.println (t.getTuple());
            }
        }
        else{
            System.out.println(tuplespace);
        }
        
    }

}

class RechercheTuple implements Callable<Tuple> {
    // pool fixe
    private List<Tuple> tuplespace;
    private Tuple template;

    public RechercheTuple(Tuple template, List<Tuple> t) {
       this.tuplespace = t;
       this.template = template;
    }

    public Tuple call() {
        Iterator<Tuple> it = this.tuplespace.iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
            if (elmt.matches(template)) {
                return elmt;
            }
        }
    return null;
    }
}