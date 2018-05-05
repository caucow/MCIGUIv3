package com.caucraft.mciguiv3.util;

import com.caucraft.mciguiv3.launch.Launcher;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class TaskList extends Task {
    
    private int curTask;
    private final List<Task> tasks;
    private final boolean addWhileFinal;
    
    public TaskList(String desc) {
        super(desc);
        tasks = new ArrayList<>();
        this.addWhileFinal = false;
    }
    
    public TaskList(String desc, boolean addWhileFinal) {
        super(desc);
        tasks = new ArrayList<>();
        this.addWhileFinal = addWhileFinal;
    }
    
    public final void addTask(Task t) {
        if (state != State.NEW && (!addWhileFinal || state != State.FINAL)) {
            throw new IllegalStateException("Task list is not new.");
        }
        if (t.state != State.NEW && (!addWhileFinal || state != State.FINAL)) {
            throw new IllegalStateException("Cannot add tasks that are not new.");
        }
        tasks.add(t);
    }
    
    @Override
    public final void makeFinal(TaskManager manager) {
        super.makeFinal(manager);
        for (Task t : tasks) {
            t.makeFinal(manager);
        }
    }
    
    public final Task getTask() {
        try {
            if (curTask >= tasks.size()) {
                return null;
            }
            return tasks.get(curTask);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public final float getProgress() {
        return tasks.isEmpty() ? 1.0F : (float)curTask / (float)tasks.size();
    }

    @Override
    public final void run() throws Exception {
        Task t;
        while (curTask < tasks.size()) {
            t = tasks.get(curTask);
            try {
                if (this.addWhileFinal && t.getState() == State.NEW) {
                    t.makeFinal(this.manager);
                }
                t.doTask();
            } catch (Exception e) {
                throw e;
            }
            curTask++;
        }
    }
    
}
