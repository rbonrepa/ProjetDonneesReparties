package linda.search.basic;

import linda.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    public static void main(String args[]) throws InterruptedException, ExecutionException {
    	if (args.length != 2) {
            System.err.println("linda.search.basic.Main search file.");
            return;
    	}
        Linda linda = new linda.shm.CentralizedLinda();
        Manager manager = new Manager(linda, args[1], args[0]);
        (new Thread(manager)).start();

        // Création d'un pool avec un nombre fixe d'ouvriers égale à la taille donnée/2
        ExecutorService poule = Executors.newFixedThreadPool(11);
        // Répartition et exécution des taches
        division(args[0], poule,linda);

}
    static void division(String mot,ExecutorService xs, Linda linda)
            throws InterruptedException, ExecutionException {
        List<Future<String>> résultats=new LinkedList<Future<String>>();

        // Soumissions des tâches
        for (int i = 2; i<=10; i = i + 1){
            xs.submit(new linda.search.basic.searchLocal(mot, linda));
        }
    }
}

class searchLocal  implements Callable<String> {
    // pool fixe
    private String mot;
    private Linda linda;

    searchLocal(String s, Linda l) {
        mot = s;
        linda = l;
    }

    public String call() {
        // Un ouvrier va chercher dans une partie de la liste.
        Searcher searcher = new Searcher(linda);
        (new Thread(searcher)).start();
        return "fin";
    }
}
