package linda.shm;

import java.util.ArrayList;

import linda.Tuple;

public class ListeTuples {

     private ArrayList<ArrayList<Tuple>> listeTuples;

    public ListeTuples () {
        this.listeTuples = new ArrayList<ArrayList<Tuple>>();
    }

    public void remove(RetourRecherche r) {
        this.listeTuples.get(r.getTailleTuple()).get(r.getPositionTuple()).remove();
    }

    public void add(Tuple t) {
        this.listeTuples.get(t.size()).add(t);
    }

    public Tuple get(RetourRecherche r) {
        return this.listeTuples.get(r.getTailleTuple()).get(r.getPositionTuple());
    }


    public RetourRecherche rechercher(Tuple template) {
        int i = 0;
        int taille_template = template.size();
        while (i < listeTuples.size()) {
            if (listeTuples.get(template.size()).get(i).matches(template)) {
                return new RetourRecherche(taille_template, i);
            }
            i++;
        }

        return new RetourRecherche(false);
    }

}
