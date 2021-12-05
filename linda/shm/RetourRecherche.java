package linda.shm;

public class RetourRecherche {
    private int taille_tuple;
    private int position_tuple;
    private Boolean a_trouve_resultat;

    public RetourRecherche (int tt, int pt) {
        this.taille_tuple = tt;
        this.position_tuple = pt;
        this.a_trouve_resultat = true;
    }

    public RetourRecherche (Boolean b) { 
        this.taille_tuple = 0;
        this.position_tuple = 0;
        this.a_trouve_resultat = false;
        //On mets à false par defaut dans ce constructeur vu que celui ci est appelé seulement quand
        //on veut creer un resultat sans donnees, cad un resultat ou l'on a rien trouvé
    }

    public int getTailleTuple() {
        return taille_tuple;
    }

    public int getPositionTuple() {
        return position_tuple;
    }

    public boolean ATrouveResultat() {
        return a_trouve_resultat;
    }
}
