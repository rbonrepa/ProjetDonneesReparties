package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.rmi.Naming;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda  {

    private LindaServerInterface server;
    private LindaClientCallback callbackRemote; // "Serveur" des callbacks permettant au serveur de communiquer avec le Client
    public boolean debugActivated = false;   //Si mis à true, va print tout ce qui se passe dans la console
    private int callbackID = 0; // Permet de différencier les différents Callbacks

    private Map<Integer,List<Tuple>> cache;  //liste des tuples du cache client
    private Semaphore mutex;  //On rajoute une sémaphore car la mémoire du client est partagée maintenant

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {        
        try {
            if (debugActivated) {System.out.println("Tentative de connexion au serveur ...");}
            
            this.cache = new HashMap<>();
            this.mutex = new Semaphore(1);

            this.callbackRemote = new LindaClientCallback();
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
            if (debugActivated) {System.out.println("Ecriture de " + t.toString() + "sur le cache en attente.");}
            mutex.acquire();
            //On va accéder à la mémoire du cache (qui est partagée) donc on bloque toute autre intéraction
            //pouvant être effectuée dessus par le serveur
            
            Integer size = t.size();
            //Selon si la taille du tuple on créée une nouvelle clé dans le Hashmap ou non
            if (this.cache.containsKey(size)) {
                this.cache.get(size).add(t);
            }
            else {
                this.cache.put(size,new ArrayList<Tuple>());
                this.cache.get(size).add(t);
            }
            mutex.release();
            if (debugActivated) {System.out.println("Ecriture de " + t.toString() + "sur le cache faite !");}
    
            if (debugActivated) {System.out.println("Demande d'écriture du Tuple : " + t.toString() + " sur le serveur");}

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
    /*
        Rien ne va changer avec l'ajout du cache car on va passer obligatoirement par le serveur pour un take
        même si il est dans le cache
    */
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

            if (debugActivated) {System.out.println("On regarde si le tuple est dans le cache.");}
            mutex.acquire();
            Integer size = template.size();
            if (this.cache.containsKey(size)) {
                Iterator<Tuple> it = this.cache.get(size).iterator();
                while (it.hasNext()) {
                    Tuple elmt = it.next();
                    if (elmt.matches(template)) {
                        mutex.release();
                        if (debugActivated) {System.out.println("Le motif " + template.toString() + " est déjà présent dans le cache client, read de l'élément : " + elmt.toString());}
                        return elmt;
                    }
                }
            }
            
            if (debugActivated) {System.out.println("Le motif " + template.toString() + " n'est pas present dans le cache, on va chercher sur le serveur");}

            mutex.release();

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
    /*
        Rien ne va changer avec l'ajout du cache car on va passer obligatoirement par le serveur pour un take
        même si il est dans le cache
    */
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

            mutex.acquire();
            if (debugActivated) {System.out.println("Debut du tryRead du template : " + template.toString() + " sur le cache");}

            Integer size = template.size();
            Tuple res = null;
            if (this.cache.containsKey(size)) {
                Iterator<Tuple> it = this.cache.get(size).iterator();
                while (it.hasNext()) {
                    Tuple elmt = it.next();
                    if (elmt.matches(template)) {
                        res = elmt;
                        mutex.release();
                        if (debugActivated) {System.out.println("Le tryRead du motif " + template.toString() + " a trouvé l'élément : " + res.toString() + " sur le cache");}
                        return res;
                    }
                }
            }
            if (debugActivated) {System.out.println("Le tryRead du motif " + template.toString() + " n'a pas trouvé d'élément correspondant sur le cache.");}
            mutex.release();

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
    /*
        Rien ne va changer avec l'ajout du cache vu qu'on veut regarder la base de données en entier forcément
    */
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
    /*
        Rien ne va changer avec l'ajout du cache vu qu'on veut regarder la base de données en entier forcément
    */
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
        try {
        CallbackTemplateServer ct = new CallbackTemplateServer(callback, mode, template, this.callbackRemote, callbackID);
        this.callbackRemote.add(ct);
        this.callbackID ++;
        this.server.eventRegister(mode, timing, template, callback, ct);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        Tuple t1 = new Tuple(1,1);
        client.write(t1);
        client.debug("");
        Tuple t2 = client.take(new Tuple(Integer.class,Integer.class));
        System.out.println(t2);
    }

}
