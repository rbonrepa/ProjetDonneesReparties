package linda.crible;

import linda.server.LindaServer;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class server {

    public static void main(String[] a) {
                
        try {
            LindaServer server = new LindaServer();

            Registry registry = LocateRegistry.createRegistry(1099);

            Naming.rebind("//localhost:1099/LindaServer", server);
            System.out.println("Serveur Linda lancé");
        } catch (Exception e) {
            e.printStackTrace();
        }
                
    }
}
