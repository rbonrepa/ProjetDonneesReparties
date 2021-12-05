package linda.shm;

import java.util.concurrent.Semaphore;
import linda.Tuple;

public class SemaphoreTemplate {
    
    private Semaphore semaphore;
    private Tuple tuple;
    private Integer mode;

    public SemaphoreTemplate(Semaphore s, Tuple t, Integer mode) {
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

    public Integer getMode() {
        return this.mode;
    }
}
