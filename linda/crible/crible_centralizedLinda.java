package linda.crible;

import linda.Linda;
import linda.Tuple;

import java.util.*;

public class crible_centralizedLinda {

    public static void main(String[] args) {
        // Start chrono
        long lStartTime = System.currentTimeMillis();

        final Linda linda = new linda.shm.CentralizedLinda();
        int taille = Integer.parseInt(args[0]);
        final int[] nb_element = {taille};

        // Initialisation de l'espace avec les entiers: inutile de lancer des threads pour une seule commande
        for (int indice = 0; indice <= taille; indice++) {
            linda.write(new Tuple(indice));
        }

        // Lancement des threads pour supprimer en parrallèle les multiples
        for (int nombre = 2; nombre <= nb_element[0]; nombre++ ){
            int finalNombre = nombre;
            // Lancer un thread que si le read renvoie quelque chose car sinon, on a deja supprimé les multiples
            if (null != linda.tryRead(new Tuple(finalNombre))) {
                System.out.println("supr multiple "+finalNombre);
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(1/1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int indice = finalNombre * 2; indice <= taille; indice += finalNombre) {
                            linda.tryTake(new Tuple(indice));
                        }
                        System.out.println("FIN supr multiple "+finalNombre);
                    }
                }.start();
            }
            // Faire une pause pour laisser le premier thread et calculer la taille de la mémoire
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Mise à jour régulière de la taille de la mémoire
                    nb_element[0] = linda.readAll(new Tuple(Integer.class)).size();
                    System.out.println("nb_element[0][0]: " + nb_element[0]);

                }

                }.start();
        }

        // End chrono
        long lEndTime = System.currentTimeMillis();
        long output = lEndTime - lStartTime;

        System.out.println("Temps  d'éxecution: " + output / 1000.0 + " secondes. ");
        //linda.debug("");
        // Pour 1000000, tps d'execution =
    }
}
