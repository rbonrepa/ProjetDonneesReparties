package linda.shm;

import java.util.concurrent.Semaphore;
import linda.Tuple;
import linda.Linda.eventMode;

public class SemaphoreTemplate {
    
    private Semaphore semaphore;
    private Tuple tuple;
    private eventMode mode;

    public SemaphoreTemplate(Semaphore s, Tuple t, eventMode mode) {
        this.semaphore = s;
        this.tuple = t;
        this.mode = mode;
    }

    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    public Tuple getTuple() {
        return this.tuple;
    }

    public eventMode getMode() {
        return this.mode;
    }
}
