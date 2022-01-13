package linda.crible;

import linda.Tuple;
import linda.server.LindaClient;

import java.util.ArrayList;
import java.util.List;


public class crible_client_serveur {

    // Fonction permettant d'initialiser tous les entiers sur le serveur
    private static void initialisation(LindaClient clientEcrivain,Integer taille){
        for (int indice = 0; indice <= taille; indice++) {
            Tuple t1 = new Tuple(indice);
            clientEcrivain.write(t1);
        }
    }

     public static synchronized void main(String[] args) {
        final class Lock { }
        final Object lock = new Lock();
        // Start chrono
        long lStartTime = System.currentTimeMillis();
        int taille = Integer.parseInt(args[0]);

        // Pour Parralléliser, on crée un client qui écrit et d'autres qui suppriment les multiples
        LindaClient clientEcrivain = new LindaClient("//localhost:1099/LindaServer");

        // On intitialise avec l'écrivain et on débloque les lecteurs
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               initialisation(clientEcrivain,taille);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }.start();

        // On crée un thread pour chaque nombre qui va retirer ses multiples
        for (int indice = 2; indice <= taille; indice++ ){
            int finalIndice = indice;
            new Thread() {
                    public void run() {
                        try {
                            synchronized (lock) {
                                lock.wait();
                            }
                            if (finalIndice == 2){
                                Thread.sleep(10000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LindaClient clientLecteur = new LindaClient("//localhost:1099/LindaServer");
                        for (int nombre = finalIndice * 2; nombre <= taille; nombre += finalIndice) {
                            Tuple t1 = new Tuple(nombre);
                            clientLecteur.tryTake(t1);
                        }
// On suppose ici que le thread qui devra suprimer les multiples de 2 sera le plus long, ce qui est vérifier en pratique
// Puisqu'on parcourt la mémoire de 2 en 2 (plus lent que 3 en 3, 4 en 4 ....)
// Enft ca marche pas si la liste de départ est longue
                        if (finalIndice == 2){

                            System.out.println("Fin");
                            clientLecteur.debug("");
                            // End chrono
                            long lEndTime = System.currentTimeMillis();
                            long output = lEndTime - lStartTime;

                            System.out.println("Temps  d'éxecution: " + output / 1000.0 + " secondes. ");
                        }
                    }
                }.start();
        }
        // Pour 1000000, tps d'execution = 59 secondes
    }
}
