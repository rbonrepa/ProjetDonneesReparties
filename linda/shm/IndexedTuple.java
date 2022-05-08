package linda.shm;

import linda.Tuple;

public class IndexedTuple {

    protected int index;
    protected Tuple tuple;

    public IndexedTuple(int i, Tuple t) {
        this.index = i;
        this.tuple = t;
    }

    public int getIndex() {
        return this.index;
    }

    public Tuple getTuple() {
        return this.tuple;
    } 

}
