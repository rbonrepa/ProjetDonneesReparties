package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collection;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {

    private LindaServerInterface server;

    public boolean debugActivated = false;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {        
        try {
            if (debugActivated) {System.out.println("Tentative de connexion au serveur ...");}
            
            this.server = (LindaServerInterface) Naming.lookup(serverURI);
            
            if (debugActivated) {
                System.out.println("Connexion au serveur réussie !");
                System.out.println("");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            if (debugActivated) {System.out.println("Demande d'écriture du Tuple : " + t.toString());}
            
            this.server.write(t);

            if (debugActivated) {
                System.out.println("Ecriture du Tuple : " + t.toString() + " faite !");
                System.out.println("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de take du template : " + template.toString());}
            
            Tuple retour = this.server.take(template);

            if (debugActivated) {
                System.out.println("Take du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple(0);
        }
    }

    

    @Override
    public Tuple read(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de read du template : " + template.toString());}

            Tuple retour = this.server.read(template);

            if (debugActivated) {
                System.out.println("Read du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple(0);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de tryTake du template : " + template.toString());}

            Tuple retour = this.server.tryTake(template);

            if (debugActivated) {
                System.out.println("tryTake du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple(0);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de tryRead du template : " + template.toString());}

            Tuple retour = this.server.tryRead(template);

            if (debugActivated) {
                System.out.println("tryRead du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new Tuple(0);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de takeAll du template : " + template.toString());}

            Collection<Tuple> retour = this.server.takeAll(template);

            if (debugActivated) {
                System.out.println("takeAll du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Tuple>();
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            if (debugActivated) {System.out.println("Demande de readAll du template : " + template.toString());}

            Collection<Tuple> retour = this.server.readAll(template);

            if (debugActivated) {
                System.out.println("readAll du template : " + template.toString() + " fait !");
                System.out.println("");
            }

            return retour;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Tuple>();
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {

    }

    @Override
    public void debug(String prefix) {
        try {
            server.debug(prefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        LindaClient client = new LindaClient("//localhost:1099/LindaServer");
        System.out.println("Client lancé");
        client.debugActivated = true;
        
        Tuple t1 = new Tuple(1);
        client.write(t1);
        client.debug("");
        Tuple t2 = client.take(new Tuple(Integer.class,Integer.class));
        System.out.println(t2);
       

    }

}
