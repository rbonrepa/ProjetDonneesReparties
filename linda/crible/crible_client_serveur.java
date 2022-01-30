package linda.crible;
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

public class crible_client_serveur {

    // Fonction permettant d'écrire tous les entiers sur le serveur
    private static void initialisation(LindaClient clientEcrivain,Integer taille){
        for (int indice = 0; indice <= taille; indice++) {
            Tuple t1 = new Tuple(indice);
            clientEcrivain.write(t1);
        }
    }

    public static void main(final String[] args) throws InterruptedException, ExecutionException {
        long départ, fin;
        // Start chrono
        départ = System.nanoTime();
        int taille = Integer.parseInt(args[0]);

        // On écrit tous les nombres jusqu'à la taille donnée:
        LindaClient clientEcrivain = new LindaClient("//localhost:1099/LindaServer");
        initialisation(clientEcrivain,taille);
        long lEndTime = System.currentTimeMillis();

        // Création d'un pool avec un nombre fixe d'ouvriers égale à la taille donnée/2
        ExecutorService poule = Executors.newFixedThreadPool(taille/2);
        Collection<Tuple> result = crible(poule, taille);

        // End chrono + Affichage résultat
        fin = System.nanoTime();
        long output = (fin - départ);

        System.out.println("Temps d'execution: "+ output /1_000000 + "s");
        System.out.println("Résultat des nombres premiers inférieurs à " + taille +" : " );
        System.out.println(result);
        //System.out.println(result.size());
    }

    static Collection<Tuple> crible(ExecutorService xs, int taille)
            throws InterruptedException, ExecutionException {
        List<Future<Collection<Tuple>>> résultats=new LinkedList<Future<Collection<Tuple>>>();
        // Soumissions des tâches
        for (int i = 2; i<=taille/2; i = i + 1){
            résultats.add(xs.submit(new supprimeMult(i, taille)));
        }

        int min = taille;
        Collection<Tuple> result = null;
        for (Future<Collection<Tuple>> résultat : résultats) {
            if (résultat.get().size() < min && résultat.get().size() != 0) {
                    min = résultat.get().size();
                    result = résultat.get();
            }
        }
        return result;
    }
}

    class supprimeMult  implements Callable<Collection<Tuple>> {
        // pool fixe
        private int nombre;
        private int taille;

        supprimeMult(int d, int f) {
            nombre = d;
            taille = f;
        }

        public Collection<Tuple> call() {
            // Un ouvrier va supprimer tous les multiples du nombre de départ.
            LindaClient clientLecteur = new LindaClient("//localhost:1099/LindaServer");
            Collection<Tuple> result = null;

            for (int i = nombre *2; i <= taille; i += nombre) {
                if (clientLecteur != null) {
                    clientLecteur.tryTake(new Tuple(i));
                    result = clientLecteur.readAll(new Tuple(Integer.class));
                }
            }
            return result;
        }
    }
