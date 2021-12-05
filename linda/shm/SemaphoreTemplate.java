package linda.shm;

import java.util.concurrent.Semaphore;
import linda.Tuple;

public class SemaphoreTemplate {
    
    private Semaphore semaphore;
    private Tuple tuple;

    public SemaphoreTemplate(Semaphore s, Tuple t) {
        this.semaphore = s;
        this.tuple = t;
    }

    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    public Tuple getTuple() {
        return this.tuple;
    }
}
