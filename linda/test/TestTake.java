package linda.test;

import linda.Linda;
import linda.Tuple;

import java.util.Collection;

public class TestTake {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Tuple motif = new Tuple(Integer.class, String.class);
                Tuple res = linda.take(motif);
                System.out.println("(1) Resultat:" + res);
                linda.debug("(1)");
            }
        }.start();
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

// Ecriture de t1 et t2
                Tuple t1 = new Tuple("t1", 1);
                linda.write(t1);
                Tuple t2 = new Tuple("t2", 2, 2);
                Tuple t3 = new Tuple("t3", 3);
                Tuple cbmotif = new Tuple(String.class, Integer.class);
                Tuple cbmotif2 = new Tuple(String.class, Integer.class,Integer.class);
                Tuple cbmotif3 = new Tuple(Integer.class,Integer.class);
                linda.write(t2);
                System.out.print("Mémoire: ");linda.debug("(1)");
                System.out.println("");

//Tst take bloquant
                System.out.println("--- Test de take pour un tuple absent en mémoire (bloquant) ---");
                System.out.print("Mémoire: ");linda.debug("(1)");
                System.out.println("Il faut attendre un autre thread qui écrit le motif choisi (5s...)");
                System.out.println("");
                Tuple r3 = linda.take(t3);
                System.out.println("Take de" +t3+": " + r3);
                System.out.print("Mémoire: ");linda.debug("(1)");
                System.out.println("");


// Test de take et trytake pour un tuple présent
                System.out.println("--- Test de take et tryTake pour un tuple présent en mémoire ---");
                System.out.print("On écrit "+ t3);
                linda.write(t3);
                System.out.print("Mémoire: ");linda.debug("(1)");
                Tuple r2 = linda.take(t1);
                System.out.println("take("+t1+")= " + r2);
                System.out.print("On verifie que le tuple n'est plus en mémoire:"); linda.debug("(1)");
                linda.write(t1);
                System.out.println("On écrit "+t1+": "); linda.debug("(1)");
                System.out.println("On test TryTake");
                Tuple tr1 = linda.tryTake(t1);
                System.out.println("TryRead de "+t1+": " + tr1);
                System.out.print("On verifie que le tuple n'est plus en mémoire:");
                linda.debug("(1)");
                System.out.println("");

// Test avec des motifs présents
                System.out.println("---- Test de take et tryTake pour un motif présent ----");
                Tuple m2 = linda.take(cbmotif2);
                System.out.println("take (" + cbmotif2 +")= " + m2);
                Tuple mr1 = linda.tryRead(cbmotif);
                System.out.println("tryTake("+ cbmotif+")= " + mr1);
                System.out.print("Mémoire: ");linda.debug("(1)");
                System.out.println("");

// Test motif absent
                System.out.println("---- Test de tryTake pour un motif ou un tuple absent ----");
                Tuple mr3 = linda.tryRead(cbmotif3);
                System.out.println("tryRead("+ cbmotif3+")= " + mr3);
                Tuple tr3 = linda.tryRead(t2);
                System.out.println("tryRead(" +t2 + ")= " + tr3);
                System.out.println("");

// Test de takeAll
                System.out.println("--- Test de takeAll ---");
                linda.write(t1);
                linda.write(t2);
                linda.write(t3);
                System.out.print("Mémoire: ");linda.debug("(1)");
                Collection<Tuple> tal = linda.takeAll(cbmotif);
                System.out.println("takeAll("+cbmotif+")="+tal);
                System.out.print("Mémoire: ");linda.debug("(1)");
                Collection<Tuple> tal2 = linda.takeAll(cbmotif3);
                System.out.println("Résultat de takeAll"+cbmotif3+":"+tal2);
                System.out.print("Fin des tests");
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
// Ecriture de t3
                Tuple t3 = new Tuple("t3", 3);
                linda.write(t3);
                System.out.println("Write t3 par un autre thread qui débloque le read " + t3);
                System.out.print("On verifie que le tuple est en mémoire:");
                linda.debug("");

            }
        }.start();
                
    }
}
