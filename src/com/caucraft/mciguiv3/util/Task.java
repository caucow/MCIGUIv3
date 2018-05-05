package com.caucraft.mciguiv3.util;

import com.caucraft.mciguiv3.launch.Launcher;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public abstract class Task {
    
    public static final Task POISON = new Task("Shutdown task manager") {
        @Override
        public synchronized void makeFinal(TaskManager manager) {
            this.manager = manager;
            state = State.FINAL;
        }
        
        @Override
        public float getProgress() {
            return 1.0F;
        }
        
        @Override
        public void run() throws Exception {
            throw new UnsupportedOperationException("Task manager should have shut down before this was thrown!");
        }
    };
    protected State state;
    TaskManager manager;
    private final String desc;
    private List<Runnable> updateListeners;
    private Exception exception;
    
    public Task(String desc) {
        this.state = State.NEW;
        this.desc = desc;
    }
    
    public abstract float getProgress();
    
    public synchronized void makeFinal(TaskManager manager) {
        if (state != State.NEW) {
            throw new IllegalStateException("Task is not new.");
        }
        this.manager = manager;
        state = State.FINAL;
    }
    
    public abstract void run() throws Exception;
    
    public String getDescription() {
        return desc;
    }
    
    public State getState() {
        return state;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public void updateProgress() {
        if (manager != null) {
            manager.updateProgress();
        }
        if (updateListeners != null) {
            synchronized (updateListeners) {
                for (int i = updateListeners.size() - 1; i >= 0; --i) {
                    updateListeners.get(i).run();
                }
            }
        }
    }
    
    public void addUpdateListener(Runnable r) {
        if (updateListeners == null) {
            updateListeners = new ArrayList<>();
        }
        synchronized (updateListeners) {
            updateListeners.add(r);
        }
    }
    
    public final synchronized void doTask() {
        if (state == State.NEW) {
            throw new IllegalStateException("Task list is still new.");
        }
        if (state == State.RUNNING) {
            throw new IllegalStateException("Task is already running.");
        }
        if (state == State.SUCCESS || state == State.FAIL) {
            throw new IllegalStateException("Task has already finished.");
        }
        if (state != State.FINAL) {
            throw new IllegalStateException("Task list is not finalized.");
        }
        state = State.RUNNING;
        if (manager != null) {
            manager.updateProgress();
        }
        try {
            this.run();
            if (manager != null) {
                manager.updateProgress();
            }
            state = State.SUCCESS;
        } catch (Exception e) {
            exception = e;
            Launcher.getLogger().log(Level.WARNING, "Problem in task " + getDescription(), e);
            state = State.FAIL;
        }
    }
    
    public static enum State {
        NEW(),
        FINAL(),
        RUNNING(),
        SUCCESS(),
        FAIL();
    }
}
