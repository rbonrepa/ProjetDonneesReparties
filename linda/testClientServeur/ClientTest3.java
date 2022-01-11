package linda.testClientServeur;

import linda.*;
import linda.server.LindaClient;

public class ClientTest3 {

    public static void main(String[] a) {
                
        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client n°3 lancé");
        client.debugActivated = true;
        
        Tuple t1 = new Tuple(1,1);
        client.write(t1);
        Tuple t2 = new Tuple(1-4,19);
        client.write(t2);
        Tuple t3 = new Tuple(2,1000000);
        client.write(t3);
                
    }
}
