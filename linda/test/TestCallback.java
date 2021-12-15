
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

        private String name;

        public MyCallback(String s) {
            this.name = s;
        }

        public void call(Tuple t) {
            System.out.println("Le callback "+ this.name + " récupère "+t);
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

// Test du callback mode READ et IMMEDIATE
                System.out.println("--- Enregistrement du callback (motif: "+cbmotif+") ---");
                linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, cbmotif, new MyCallback(" 1 (option Read/Immediate)"));
                System.out.print("Mémoire: ");linda.debug("");

// Test du callback mode read et FUTURE
                System.out.println("");
                System.out.println("--- Enregistrement du callback (motif: "+cbmotif+") en mode READ et FUTURE ---");
                linda.eventRegister(eventMode.READ, eventTiming.FUTURE, cbmotif, new MyCallback("2 (Option Read/Future)"));
                System.out.print("Mémoire: ");linda.debug("");


// Test du callback mode take et IMMEDIAT
                System.out.println("");
                System.out.println("-- Exécution du callback (motif: "+cbmotif+") en mode TAKE et IMMEDIATE--");
                linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, cbmotif, new MyCallback("3 (Option Take/Immediate)"));
                System.out.print("Mémoire: ");linda.debug("");

// Test du callback mode take et FUTURE
                System.out.println("");
                System.out.println("--- Exécution du callback (motif: "+cbmotif+") en mode TAKE et FUTURE ---");
                linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, cbmotif, new MyCallback("4 (Option Take/Future)"));
                System.out.print("Mémoire: ");linda.debug("");

// TEST de l'appel au futur
                System.out.println(" ");
                System.out.println("Test de l'appel aux callbacks en mode futures: ");
                System.out.println("Ecriture de T2,T3,T4");
                Tuple t4 = new Tuple("t4", 4);
                linda.write(t2);
                linda.write(t3);
                linda.write(t4);

    }

}
