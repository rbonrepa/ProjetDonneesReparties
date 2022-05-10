package linda.crible;
import java.util.*;


public class crible_sequentiel {

    // Initialiser une liste avec tous les entiers
    private static List<Integer> initialisation(Integer taille){
        List<Integer> liste;
        liste =new ArrayList<Integer>();
        for (int indice = 0; indice <= taille; indice++) {
            liste.add(indice);
        }
        return liste;
    }

    // Retirer les multiples du nombre donné en paramètre dans la liste
    private static List<Integer> retrait_multiple(List<Integer> liste, Integer nombre){
        int taille = liste.size();
       for (int indice =nombre*2; indice < liste.size(); indice+=nombre) {
            liste.remove(indice);
            indice--;
        }
        return liste;
    }

    public static void main(String[] args) {
        long départ, fin;
        // Start chrono
        départ = System.nanoTime();

        // Initialisation de la liste
        int taille = Integer.parseInt(args[0]);
        List<Integer> liste = initialisation(taille);

        // Retrait des multiples de l'élement de la liste
        for (int indice = 2; indice < liste.size(); indice++ ){
            liste = retrait_multiple(liste, liste.get(indice));
        }

        // End chrono
        fin = System.nanoTime();
        long output = (fin - départ);

        System.out.println("Temps d'execution: "+ output /1_000000 + "s");
        System.out.println("Résultat des nombres premiers inférieurs à " + taille +" : " );
        //System.out.println(liste);
        //System.out.println(liste.size());
    }
}
