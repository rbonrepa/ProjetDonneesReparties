package linda.test;

import java.util.Random;

import linda.*;

public class TestConcurrenceReadWrite {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLindaParallel();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
                
        new Thread() {
            // Ce thread va écrire de nombreux tuples dans l'espace partagé
            public void run() {
                int i ;
                for (i = 0 ; i < 1000 ; i++ ){
                    try {
                        Tuple motif = new Tuple(i, Integer.toString(i));
                        linda.write(motif);
                        System.out.println("Write numéro "+i+": " + motif);
                        // On introduit une pause de durée aléatoire pour entrelacer les opérations
                        Random obj = new Random();
                        int nbr = obj.nextInt(500);
                        Thread.sleep(nbr);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("fin des write");
            }
        }.start();
                
        new Thread() {
            // Ce thread va lire de nombreux tuples dans l'espace partagé
            public void run() {
                int i ;
                for (i = 0 ; i < 1000 ; i++ ){
                    try {
                        Tuple motif = new Tuple(i+10, Integer.toString(i+10));
                        Tuple t = linda.read(motif);
                        System.out.println("read : " + t);
                        // On introduit une pause de durée aléatoire pour entrelacer les opérations
                        Random obj = new Random();
                        int nbr = obj.nextInt(500);
                        Thread.sleep(nbr);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("fin des reads");
            }
        }.start();

        new Thread() {
            // Ce thread va lire de nombreux tuples dans l'espace partagé
            public void run() {
                int i ;
                for (i = 0 ; i < 1000 ; i++ ){
                    try {
                        Tuple motif = new Tuple(i+10, Integer.toString(i));
                        Tuple t = linda.read(motif);
                        System.out.println("read : " + t);
                        // On introduit une pause de durée aléatoire pour entrelacer les opérations
                        Random obj = new Random();
                        int nbr = obj.nextInt(500);
                        Thread.sleep(nbr);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("fin des reads");
            }
        }.start();
    }

    
}
