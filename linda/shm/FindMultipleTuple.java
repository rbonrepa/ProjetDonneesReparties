package linda.shm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import linda.Tuple;

class FindMultipleTuple extends RecursiveTask<ArrayList<IndexedTuple>> {
    // pool fixe
    private Tuple tuple;
    private List<Tuple> liste;
    private ArrayList<IndexedTuple> res;

    FindMultipleTuple(Tuple t, List<Tuple> l) {
        tuple = t;
        liste = l;
        res = new ArrayList<>();
    }

    @Override
    protected ArrayList<IndexedTuple> compute() {

        if (liste.size() > 50000) {
            List<Tuple> subList1 = liste.subList(0, liste.size()/2);
            List<Tuple> subList2 = liste.subList(liste.size()/2, liste.size());

            FindMultipleTuple subTask1 = new FindMultipleTuple(tuple, subList1);
            FindMultipleTuple subTask2 = new FindMultipleTuple(tuple, subList2);

            ArrayList<IndexedTuple> t1 = subTask1.compute();
            ArrayList<IndexedTuple> t2 = subTask2.compute();
            t1.addAll(t2);
            return t1;

        } else {
        
            // Chercher le tuple dans la liste
            ListIterator<Tuple> it = liste.listIterator();
            while (it.hasNext()) {
                Integer index = it.nextIndex();
                Tuple elmt = it.next();
                if (elmt.matches(tuple)) {
                    this.res.add(new IndexedTuple(index, elmt));
                }
            }
            return res;            
        }
    }

   }


   





