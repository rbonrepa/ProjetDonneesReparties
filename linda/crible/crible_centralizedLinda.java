package linda.crible;
import linda.Linda;
import linda.Tuple;
import linda.server.LindaClient;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Future;

public class crible_centralizedLinda {


    private static void initialisation(Integer taille, Linda linda){
        for (int indice = 0; indice <= taille; indice++) {
            linda.write(new Tuple(indice));
        }
    }
    public static void main(final String[] args) throws InterruptedException, ExecutionException {
        long départ, fin;
        // Start chrono
        départ = System.nanoTime();
        int taille = Integer.parseInt(args[0]);

        // On écrit dans la mémoire tous les nombres jusqu'à la taille donnée
        final Linda linda = new linda.shm.CentralizedLindaParallel();
        initialisation(taille, linda);

        // Création d'un pool avec un nombre fixe d'ouvriers égale à la taille donnée/2
        ExecutorService poule = Executors.newFixedThreadPool(taille/2);
        // Répartition et exécution des taches
        Collection<Tuple> result = crible(poule, taille, linda);

        // End chrono + Affichage résultat
        fin = System.nanoTime();
        long output = (fin - départ);

        System.out.println("Temps d'execution: "+ output /1_000000000 + "s");
        System.out.println("Résultat des nombres premiers inférieurs à " + taille +" : " );
        System.out.println(result);
        //System.out.println(result.size());
    }

    static Collection<Tuple> crible(ExecutorService xs, int taille, Linda linda)
            throws InterruptedException, ExecutionException {
        List<Future<Collection<Tuple>>> résultats=new LinkedList<Future<Collection<Tuple>>>();

        // Soumissions des tâches
        for (int i = 2; i<=taille/2; i = i + 1){
            résultats.add(xs.submit(new supprimeMult2(i, taille, linda)));
        }

        // On récupère la plus petite collection cad celle qui a éliminé tous les nombres premiers
        int min = taille;
        Collection<Tuple> result = null;
        for (Future<Collection<Tuple>> résultat : résultats) {
            if (résultat.get().size() < min){
                min = résultat.get().size();
                result = résultat.get();
            }
        }
        return result;
    }

}

class supprimeMult2  implements Callable<Collection<Tuple>> {
    // pool fixe
    private int nombre;
    private int taille;
    private Linda linda;

    supprimeMult2(int d, int f, Linda l) {
        nombre = d;
        taille = f;
        linda = l;
    }

    public Collection<Tuple> call() {
        // Un ouvrier va supprimer tous les multiples du nombre de départ.
        for (int i = nombre * 2; i <= taille; i += nombre) {
            Tuple t1 = new Tuple(i);
            if (linda != null) {
                linda.tryTake(t1);
            }
        }
        return linda.readAll(new Tuple(Integer.class));
    }
}