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

class FindTuple extends RecursiveTask<IndexedTuple> {
    private Tuple tuple;
    private List<Tuple> liste;

    // RecursiveTask retournant un Tuple matchant avec le template ou null

    FindTuple(Tuple t, List<Tuple> l) {
        tuple = t;
        liste = l;
    }

    @Override
    protected IndexedTuple compute() {

        if (liste.size() > 50000) {
            List<Tuple> subList1 = liste.subList(0, liste.size()/2);
            List<Tuple> subList2 = liste.subList(liste.size()/2, liste.size());

            FindTuple subTask1 = new FindTuple(tuple, subList1);
            FindTuple subTask2 = new FindTuple(tuple, subList2);

            IndexedTuple t1 = subTask2.compute();
            IndexedTuple t2 = subTask1.compute();


            if (t1.getTuple() != null) {
                return t1;
            } else {
                return t2;
            }

        } else {
        
            // Chercher le tuple dans la liste
            ListIterator<Tuple> it = liste.listIterator();
            while (it.hasNext()) {
                Integer index = it.nextIndex();
                Tuple elmt = it.next();
                if (elmt.matches(tuple)) {
                    return new IndexedTuple(index, elmt);
                }
            }
            return new IndexedTuple(0, null);               
        }
    }


public static void main(String[] args) {

    ArrayList<Tuple> list = new ArrayList<>();
    for (int i = 0; i <= 100000; i++) {
        Tuple t = new Tuple(i);
        list.add(t);
    }
    list.add(new Tuple("Salut"));

    Tuple template = new Tuple(String.class);

    ForkJoinPool pool = new ForkJoinPool(2);
    FindTuple finder = new FindTuple(template, list);

    long start2 = System.nanoTime();
    IndexedTuple t = pool.invoke(finder);   
    System.out.println(t);
    long end2 = System.nanoTime();      
    System.out.println("Elapsed Time in nano seconds: "+ (end2-start2));  

    long start1 = System.nanoTime();
    Iterator<Tuple> it = list.iterator();
    while (it.hasNext()) {
        Tuple elmt = it.next();
        if (elmt.matches(template)) {
            //endEdit();
            System.out.println(elmt);
        }
    }
    long end1 = System.nanoTime();      
    System.out.println("Elapsed Time in nano seconds: "+ (end1-start1));     
   }


   



}

