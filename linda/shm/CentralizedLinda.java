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

        int i = 0;
        while (i < listeSemaphores.size()) {
            if (listeSemaphores.get(i).tuple.matches(t)) {
                listeSemaphores.get(i).semaphore.release();
            }
            i++;
        }
    }

    @Override
    public Tuple take(Tuple template) {
        int index_tuple = rechercher(template);
        if (index_tuple == -1) {   //Cad n'est pas dans la liste de tuples
            Semaphore semaphore = new Semaphore(0);
            SemaphoreTemplate semtemplate = new SemaphoreTemplate(semaphore, template);
            listeSemaphores.add(semtemplate);
            semaphore.acquire();
            listeSemaphores.remove(semtemplate);
        } //Sinon l'element est deja present
        return listeTuples.get(index_tuple);
    }

    @Override
    public Tuple read(Tuple template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void debug(String prefix) {
        // TODO Auto-generated method stub
        
    }

    // TO BE COMPLETED


}
