package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import java.util.*;
import java.util.concurrent.Semaphore;


/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    private Map<Integer,List<Tuple>> tuplespace;
    private List<SemaphoreTemplate> semaphorespace;
    private List<CallbackTemplate> callbackspace;
	
    public CentralizedLinda() {
        this.tuplespace = new HashMap<>();
        this.semaphorespace = new ArrayList<>();
        this.callbackspace = new ArrayList<>();
    }

    @Override
    public void write(Tuple t) {
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
                if (st.getMode() == -1) {
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
            if (elmt.getTuple().matches(t)) {
                if (elmt.getMode() == -1 && !takeTriggered) {
                    this.tuplespace.get(t.size()).remove(t);
                    elmt.getCallback().call(t);
                    break;
                }
                else if (elmt.getMode() == 0) {
                    elmt.getCallback().call(t);
                }
            }
                
        }
               
    }


    @Override
    public Tuple take(Tuple template) {
       Integer size = template.size();
       boolean matches = false;
       if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    matches = true;
                }
            }
       }

       Semaphore s;
       if (!matches) {
           s = new Semaphore(0);
           this.semaphorespace.add(new SemaphoreTemplate(s, template, -1));
        }
        else {
            s = new Semaphore(1);
        }

        try {
        s.acquire();
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        Iterator<Tuple> it = this.tuplespace.get(size).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
             if (elmt.matches(template)) {
                it.remove();
                return elmt;
            }
        }
        return null;
        
    }

    @Override
    public Tuple read(Tuple template) {
        Integer size = template.size();
        boolean matches = false;
        if (this.tuplespace.containsKey(size)) {
            Iterator<Tuple> it = this.tuplespace.get(size).iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(template)) {
                    matches = true;
                }
            }
       }

       Semaphore s;
       if (!matches) {
           s = new Semaphore(0);
           this.semaphorespace.add(new SemaphoreTemplate(s, template, 0));
        }
        else {
            s = new Semaphore(1);
        }

        try {
        s.acquire();
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        Iterator<Tuple> it = this.tuplespace.get(size).iterator();
        while (it.hasNext()) {
            Tuple elmt = it.next();
             if (elmt.matches(template)) {
                return elmt;
            }
        }
        return null;    
    }

    @Override
    public Tuple tryTake(Tuple template) {
        Integer size = template.size();
        Tuple res = null;
        if (this.tuplespace.containsKey(size)) {
             Iterator<Tuple> it = this.tuplespace.get(size).iterator();
             while (it.hasNext()) {
                 Tuple elmt = it.next();
                 if (elmt.matches(template)) {
                     it.remove();
                     res = elmt;
                     return res;
                 }
             }
             return res;
        }
        return res;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        Integer size = template.size();
        Tuple res = null;
        if (this.tuplespace.containsKey(size)) {
             Iterator<Tuple> it = this.tuplespace.get(size).iterator();
             while (it.hasNext()) {
                 Tuple elmt = it.next();
                 if (elmt.matches(template)) {
                     res = elmt;
                     return res;
                 }
             }
             return res;
        }
        return res;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
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
        return res;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
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
        return res;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        Integer m;
        if (mode == eventMode.READ) {
            m = 0;
        } else {
            m = -1;
        }
        CallbackTemplate ct = new CallbackTemplate(callback, m, template);
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
                    if (ct.getMode() == -1) {
                        it.remove();
                    }
                    ct.getCallback().call(elmt);
                    this.callbackspace.remove(ct);
                    
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