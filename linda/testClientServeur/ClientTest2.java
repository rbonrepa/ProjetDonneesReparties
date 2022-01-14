package linda.testClientServeur;

import java.util.Collection;

import linda.*;
import linda.server.LindaClient;

public class ClientTest2 {

    public static void main(String[] a) {
                
        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client n°2 lancé");
        client.debugActivated = true;
        
        Tuple t1 = new Tuple(4);
        client.write(t1);
        Collection<Tuple> t2 = client.takeAll(new Tuple(Integer.class,Integer.class));
        System.out.println(t2);
        
                
    }
}
