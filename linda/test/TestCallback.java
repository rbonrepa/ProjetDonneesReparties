
package linda.test;

import linda.Callback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;

public class TestCallback {

    private static Tuple cbmotif;
    private static Linda linda;
    private static Callback callback;
    
    private static class MyCallback implements Callback {
        public void call(Tuple t) {
            System.out.println("On récupère: "+t);
            //linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, this);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] a) {
        linda = new linda.shm.CentralizedLinda();
        // linda = new linda.server.LindaClient("//localhost:4000/MonServeur");
        cbmotif = new Tuple(String.class, Integer.class);

        Tuple t3 = new Tuple("t3", 3);
        Tuple t1 = new Tuple(1, 1);
        Tuple t2 = new Tuple("t2", 2);

        linda.write(t1);
        linda.write(t2);
        linda.write(t3);

// Test du callback mode read
        System.out.println("--- Test du callback en mode READ et IMMEDIATE ---");
        System.out.print("Mémoire: ");linda.debug("");
        System.out.println("- Exécution du callback (motif: "+cbmotif+") -");
        linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, cbmotif, new MyCallback());
        System.out.println("- Fin du callback -");
        System.out.print("Mémoire: ");linda.debug("");

// Test du callback mode read et FUTURE
        System.out.print("");
        System.out.println("--- Test du callback en mode READ et FUTURE ---");
        System.out.print("Mémoire: ");linda.debug("");
        System.out.println("- Exécution du callback (motif: "+cbmotif+") -");
        linda.eventRegister(eventMode.READ, eventTiming.FUTURE, cbmotif, new MyCallback());
        System.out.println("- Fin du callback -");
        System.out.print("Mémoire: ");linda.debug("");


// Test du callback mode take et IMMEDIAT
        System.out.print("");
        System.out.println("--- Test du callback en mode TAKE et IMMEDIATE---");
        System.out.print("Mémoire: ");linda.debug("");
        System.out.println("-- Exécution du callback (motif: "+cbmotif+") --");
        linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, new MyCallback());
        System.out.println("-- Fin du callback --");
        System.out.print("Mémoire: ");linda.debug("");

// Test du callback mode take et FUTURE
        linda.write(t2);
        linda.write(t3);
        System.out.print("");
        System.out.println("--- Test du callback en mode TAKE et FUTURE---");
        System.out.print("Mémoire: ");linda.debug("");
        System.out.println("-- Exécution du callback (motif: "+cbmotif+") --");
        linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, cbmotif, new MyCallback());
        System.out.println("-- Fin du callback --");
        System.out.print("Mémoire: ");linda.debug("");

    }

}
