package linda.testClientServeur;

import linda.*;
import linda.server.LindaClient;

public class ClientTest1 {

    public static void main(String[] a) {
                
        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client n°1 lancé");
        client.debugActivated = true;
        
        Tuple t1 = new Tuple(1);
        client.write(t1);
        client.debug("");
        Tuple t2 = client.take(new Tuple(Integer.class,Integer.class));
        System.out.println(t2);
                
    }
}
