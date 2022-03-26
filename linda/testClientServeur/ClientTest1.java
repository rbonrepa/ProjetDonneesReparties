package linda.testClientServeur;

import java.io.Serializable;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.server.LindaClient;

public class ClientTest1 {

    private static class MyCallback implements Callback,Serializable {
        public void call(Tuple t) {
            System.out.println("CB got "+t);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println("CB done with "+t);
        }
    }

    public static void main(String[] a) {

        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client n°1 lancé");
        client.debugActivated = true;
        
        Tuple t1 = new Tuple(1);
        client.write(t1);
        client.eventRegister(eventMode.READ, eventTiming.FUTURE, new Tuple(Integer.class), new MyCallback());
        //System.out.println(t2);

    }
}
