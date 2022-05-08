package linda.shm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import linda.Tuple;

public class parallelListTest {
    
    class recherche  implements Callable<Tuple> {
        // pool fixe
        private Tuple tuple;
        private List<Tuple> liste;
    
        recherche(Tuple t, List<Tuple> l) {
            tuple = t;
            liste = l;
        }
    
        public Tuple call() {
            // cher her le tuple dans la liste
            Iterator<Tuple> it = liste.iterator();
            while (it.hasNext()) {
                Tuple elmt = it.next();
                if (elmt.matches(tuple)) {
                    it.remove();
                    //endEdit();
                    return elmt;
                }
            }
            return null;
        }
}

public static void main(String[] args) {

    parallelListTest linda = new parallelListTest();

    ArrayList<Tuple> list = new ArrayList<>();
    for (int i = 0; i <= 100; i++) {
        Tuple t = new Tuple(i);
        list.add(t);
    }

    Tuple template = new Tuple(Integer.class,Integer.class);

    if (list.size()>=20){// Inutile de paralléliser si la taille de la liste reste petite
        // Création d'un pool avec un nombre fixe d'ouvriers égale à la taille donnée/2
        ExecutorService xs = Executors.newFixedThreadPool(2);
        List<Future<Tuple>> resultats=new LinkedList<Future<Tuple>>();
        
        // Soumissions des tâches
        for (int i = 1; i<=list.size(); i = i + 10){
            List<Tuple> subListe = list.subList(i, i+10);
            resultats.add(xs.submit(linda.new recherche(template, subListe)));
        }
    }



}

}