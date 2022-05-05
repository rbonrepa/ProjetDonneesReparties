package linda.test;

import linda.*;

public class BasicTest1 {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 5);
                linda.write(t11);

            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t2 = linda.read(new Tuple(Integer.class, Integer.class));
                Tuple t3 = linda.read(new Tuple(Integer.class, Integer.class));
                Tuple t4 = linda.read(new Tuple(Integer.class, Integer.class));
                Tuple t5 = linda.read(new Tuple(Integer.class, Integer.class));
                Tuple t6 = linda.read(new Tuple(Integer.class, Integer.class));


            }
        }.start();

                
    }
}
