package linda.test;

import linda.*;

public class TestsAdditionnels {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("(1) Write pour debloquer le take du test1 : [1]");
                linda.write(new Tuple(1));

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

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(2) write: " + t1);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 5);
                System.out.println("(2) write: " + t11);
                linda.write(t11);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(2) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(3, 4, "foo");
                System.out.println("(2) write: " + t3);
                linda.write(t3);

                System.out.println("(2) veut take : [1]");
                linda.take(new Tuple(1));
                System.out.println("(2) a effectue le take : [1]");

                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {}

                

                linda.debug("(2)");

            }
        }.start();
                
    }
}
