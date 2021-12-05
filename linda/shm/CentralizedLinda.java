package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
<<<<<<< Updated upstream
	
=======

    private ListeTuples listeTuples;
    private List<SemaphoreTemplate> listeSemaphores;
	private List<CallbackTemplate> listeCallback;

>>>>>>> Stashed changes
    public CentralizedLinda() {
        this.listeTuples = new ListeTuples();
        this.listeSemaphores = new ArrayList<SemaphoreTemplate>();
        this.listeCallback =  = new ArrayList<CallbackTemplate>();
    }

<<<<<<< Updated upstream
    // TO BE COMPLETED

}
=======
    @Override
    public void write(Tuple t) {
        listeTuples.add(t);

        //Ce bloc va débloquer un thread en attente de read ou take en releasant la sémaphore
        //associée si elle vient d'être ajoutée
        int i = 0; 
        while (i < listeSemaphores.size()) {
            if (listeSemaphores.get(i).getTuple().matches(t)) {
                listeSemaphores.get(i).getSemaphore().release();
                i = listeSemaphores.size();
            }
            i++;
        }
    }

    @Override
    public Tuple take(Tuple template) {
        RetourRecherche retourRecherche = attente_bloquante(template);
        Tuple resultat = listeTuples.get(retourRecherche); //On stock le resultat avant de l'enlever de la liste
        listeTuples.remove(retourRecherche);
        return resultat;
    }

    @Override
    public Tuple read(Tuple template) {
        RetourRecherche retourRecherche = attente_bloquante(template);
        return listeTuples.get(retourRecherche);
    }

    @Override
    public Tuple tryTake(Tuple template) {
        RetourRecherche retour_recherche = listeTuples.rechercher(template); //On prend l'index du tuple au motif recherché
        if (!retour_recherche.ATrouveResultat()) { //Le tuple n'est pas présent
            return null;
        } else { //on a trouvé un tuple, on le retire de la liste et on le return
            Tuple resultat = listeTuples.get(retour_recherche);
            listeTuples.remove(retour_recherche);
            return resultat;
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        RetourRecherche retourRecherche = listeTuples.rechercher(template);
        if (!retourRecherche.ATrouveResultat()) {
            return null;
        } else {
            return listeTuples.get(retourRecherche);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        ArrayList<Tuple> collection = new ArrayList<Tuple>();
        RetourRecherche retourRecherche = listeTuples.rechercher(template);
        while (!retourRecherche.ATrouveResultat()) { 
            //Tant qu'on trouve encore des elements matchant avec le motif
            collection.add(listeTuples.get(retourRecherche));
            listeTuples.remove(retourRecherche);
            retourRecherche = listeTuples.rechercher(template);
        }
        return collection;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        ArrayList<Tuple> collection = new ArrayList<Tuple>();
        RetourRecherche retourRecherche = listeTuples.rechercher(template);
        while (!retourRecherche.ATrouveResultat()) {
            //Tant qu'on trouve encore des elements matchant avec le motif
            collection.add(listeTuples.get(retourRecherche));
            retourRecherche = listeTuples.rechercher(template);
        }
        return collection;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        this.listeCallback.add(new CallbackTemplate(callback,template,mode)());
        if (timing == eventTiming.IMMEDIATE) {
            this.callbackCheck(callback,template);
        }
        
    }

    private void callbackCheck(Callback callback, Tuple template) {

    }

    @Override
    public void debug(String prefix) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param template
     * @return l'index de l'element à read ou take
     */
    private RetourRecherche attente_bloquante(Tuple template) {
        RetourRecherche retourRecherche = listeTuples.rechercher(template);
        if (retourRecherche.ATrouveResultat()) {   //Aucun tuple n'est associé à ce template dans la liste de tuples
            Semaphore semaphore = new Semaphore(0);
            SemaphoreTemplate semtemplate = new SemaphoreTemplate(semaphore, template);
            listeSemaphores.add(semtemplate);
            try {
                semaphore.acquire();  //Bloquant
            } catch (InterruptedException e) {e.printStackTrace();}
            retourRecherche = listeTuples.rechercher(template);
            listeSemaphores.remove(semtemplate);
        } //Sinon l'element est deja present
        return retourRecherche;
    }
>>>>>>> Stashed changes
