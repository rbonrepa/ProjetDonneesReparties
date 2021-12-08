package linda.test;

import linda.*;

import java.util.Collection;

public class TestRead {

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


                Tuple t1 = new Tuple("t1", 1);
                linda.write(t1);

                Tuple t2 = new Tuple("t2", 2, 2);
                linda.write(t2);
                Tuple t3 = new Tuple("t3", 3);
                //linda.write(t3);
                System.out.println("Write " + t1 +"," + t2 +"," + t3);

                Tuple r2 = linda.read(t1);
                System.out.println("Read t1: " + r2);

                Tuple ta1 = linda.tryTake(t1);
                System.out.println("Take t1: " + ta1);
                Tuple ta2 = linda.tryTake(t3);
                System.out.println("Take t3, qui n'est pas écrit: " + ta2);

                Tuple tr1 = linda.tryRead(t1);
                System.out.println("TryRead t1: " + tr1);
                Tuple tr2 = linda.tryRead(t3);
                System.out.println("TryRead t3, qui n'est pas écrit: " + tr2);

                Collection<Tuple> tall1 = linda.takeAll(t2);
                System.out.println("takeAll t1: " + tall1);
                                
                linda.debug("(2)");

            }
        }.start();
                
    }
}
