package linda.shm;

import java.util.ArrayList;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import java.util.concurrent.Semaphore;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    ArrayList<Tuple> listeTuples = new ArrayList<Tuple>();
    ArrayList<SemaphoreTemplate> listeSemaphores = new ArrayList<SemaphoreTemplate>();
	
    public CentralizedLinda() {
    }

    @Override
    public void write(Tuple t) {
        listeTuples.add(t);

        //Ce code va débloquer un thread en attente de read ou take en releasant la sémaphore
        //associée
        int i = 0; 
        while (i < listeSemaphores.size()) {
            if (listeSemaphores.get(i).tuple.matches(t)) {
                listeSemaphores.get(i).semaphore.release();
                i = listeSemaphores.size();
            }
            i++;
        }
    }

    @Override
    public Tuple take(Tuple template) {
        Tuple element = attente_bloquante(template);
        listeTuples.remove(element);
        return element;
    }

    @Override
    public Tuple read(Tuple template) {
        Tuple element = attente_bloquante(template);
        return element;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        int index_tuple = rechercher(template);
        if (index_tuple == -1) {
            return null;
        } else {
            listeTuples.remove(index_tuple);
            return listeTuples.get(index_tuple);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        int index_tuple = rechercher(template);
        if (index_tuple == -1) {
            return null;
        } else {
            return listeTuples.get(index_tuple);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        ArrayList<Tuple> collection = new ArrayList<Tuple>();
        int index_tuple = rechercher(template);
        while (index_tuple != -1) { //Tant qu'on trouve encore des elements matchant avec le motif
            collection.add(listeTuples.get(index_tuple));
            listeTuples.remove(index_tuple);
            index_tuple = rechercher(template);
        }
        return collection;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        ArrayList<Tuple> collection = new ArrayList<Tuple>();
        int index_tuple = rechercher(template);
        while (index_tuple != -1) { //Tant qu'on trouve encore des elements matchant avec le motif
            collection.add(listeTuples.get(index_tuple));
            index_tuple = rechercher(template);
        }
        return collection;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void debug(String prefix) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param template
     * @return l'index de l'element à read ou take
     */
    private Tuple attente_bloquante(Tuple template) {
        int index_tuple = rechercher(template);
        if (index_tuple == -1) {   //Aucun tuple n'est associé à ce template dans la liste de tuples
            Semaphore semaphore = new Semaphore(0);
            SemaphoreTemplate semtemplate = new SemaphoreTemplate(semaphore, template);
            listeSemaphores.add(semtemplate);
            try {
                semaphore.acquire();  //Bloquant
            } catch (InterruptedException e) {e.printStackTrace();}
            index_tuple = rechercher(template);
            listeSemaphores.remove(semtemplate);
        } //Sinon l'element est deja present
        return listeTuples.get(index_tuple);
    }

    private int rechercher(Tuple template) {
        int i = 0;
        while (i < listeTuples.size() && listeTuples.get(i).matches(template)) {
            i++;
        }
        return 0;
    }

}
