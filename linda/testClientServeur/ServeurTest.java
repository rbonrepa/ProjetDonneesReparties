package linda.testClientServeur;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import linda.*;
import linda.server.LindaServer;

public class ServeurTest {

    public static void main(String[] a) {
                
        try {
            LindaServer server = new LindaServer();
            server.debugActivated = true; 

            Registry registry = LocateRegistry.createRegistry(1099);

            Naming.rebind("//localhost:1099/LindaServer", server);
            System.out.println("Serveur Linda lancé");
            /*
            LindaClient client = new LindaClient("//localhost:1099/LindaServer");
            System.out.println("Client lancé");
            
            Tuple t1 = new Tuple(1,2);
            client.write(t1);
            client.debug("");
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
                
    }
}
