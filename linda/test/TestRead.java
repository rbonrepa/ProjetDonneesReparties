package linda.test;

import linda.*;
import linda.server.LindaClient;

import java.util.Collection;

public class TestRead {

    public static void main(String[] a) {

         final Linda linda = new linda.shm.CentralizedLinda();
        //final Linda linda  = new LindaClient("//localhost:1099/LindaServer");
        //LindaClient client = new LindaClient("//localhost:1099/LindaServer");
                
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
                Tuple cbmotif = new Tuple(String.class, Integer.class);
                Tuple cbmotif2 = new Tuple(String.class, Integer.class,Integer.class);
                Tuple cbmotif3 = new Tuple(Integer.class,Integer.class);
                Tuple t3 = new Tuple("t3", 3);
                Tuple t1 = new Tuple("t1", 1);
                Tuple t4 = new Tuple("t4", 4);
                linda.write(t1);
                Tuple t2 = new Tuple("t2", 2, 2);
                linda.write(t2);
                System.out.print("Mémoire: ");linda.debug("");
                System.out.println("");

//Test de read avec tuple non présent => bloquant
                System.out.println("----- Test de read pour un tuple absent en mémoire (bloquant) ----");
                System.out.println("Il faut attendre un autre thread qui écrit ce motif. (5s...)");
                System.out.println("");
                Tuple r3 = linda.read(t3);
                System.out.println("Read(" +t3 + ")= " + r3);

// Test de read et tryRead pour un tuple présent
                System.out.println("");
                System.out.println("---- Test de read et tryRead pour un tuple présent en mémoire ----");
                Tuple r2 = linda.read(t1);
                System.out.println("read (" + t1 +")= " + r2);
                Tuple tr1 = linda.tryRead(t2);
                System.out.println("TryRead("+ t2+")= " + tr1);
                System.out.print("Mémoire: ");linda.debug("");
                System.out.println("");
// Test avec des motifs
                System.out.println("---- Test de read et tryRead pour un motif présent ----");
                Tuple m2 = linda.read(cbmotif2);
                System.out.println("read (" + cbmotif2 +")= " + m2);
                Tuple mr1 = linda.tryRead(cbmotif);
                System.out.println("TryRead("+ cbmotif+")= " + mr1);
                System.out.print("Mémoire: ");linda.debug("");
                System.out.println("");
// Test motif absent
                System.out.println("---- Test de tryRead pour un motif ou un tuple absent ----");
                Tuple mr3 = linda.tryRead(cbmotif3);
                System.out.println("tryRead("+ cbmotif3+")= " + mr3);
                Tuple tr3 = linda.tryRead(t4);
                System.out.println("tryRead(" +t4 + ")= " + tr3);

// Test de readAll
                System.out.println("");
                System.out.println("---- Test de readall -----");
                System.out.print("Mémoire: ");linda.debug("");
                Collection<Tuple> tal = linda.readAll(cbmotif);
                System.out.println("readdALl("+cbmotif+")="+tal);
                Collection<Tuple> tal2 = linda.readAll(t4);
                System.out.print("readAll("+t4+")= "+tal2);
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
            }
        }.start();
                
    }
}
