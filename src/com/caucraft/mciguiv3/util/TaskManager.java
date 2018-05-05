package com.caucraft.mciguiv3.util;

import com.caucraft.mciguiv3.launch.Launcher;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author caucow
 */
public class TaskManager {

    private final Thread taskThread;
//    private final Thread monitorThread;
//    private final Object monitorLock;
//    private boolean update;
    private Task curTask;
    private final LinkedBlockingQueue<Task> tasks;
    private final JFrame progressFrame;
    private final JPanel panel;

    List<Task> lastStack = new ArrayList<>();
    List<Task> curStack = new ArrayList<>();
    List<JProgressBar> barStack = new ArrayList<>();
    
    private boolean shutdown;

    public TaskManager(Frame parent) {
        tasks = new LinkedBlockingQueue<>();
        progressFrame = new JFrame("Task Processor");
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        progressFrame.getContentPane().add(panel);
        progressFrame.setMinimumSize(new Dimension(600, 50));
        progressFrame.pack();
        progressFrame.setLocationRelativeTo(parent);
        progressFrame.dispose();
        taskThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    Task t = curTask = tasks.take();
                    if (t == Task.POISON) {
                        curTask = null;
                        continue;
                    }
                    t.doTask();
                    curTask = null;
                    t.updateProgress();
                } catch (InterruptedException ie) {
                    tasks.clear();
                    Thread.currentThread().interrupt();
                    synchronized (tasks) {
                        tasks.notifyAll();
                    }
                    break;
                }
                if (tasks.isEmpty()) {
                    synchronized (tasks) {
                        tasks.notifyAll();
                    }
                }
            }
            synchronized (tasks) {
                tasks.notifyAll();
            }
        }, "TaskManager Processor");
        
        // <editor-fold>
//        monitorThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Task> lastStack = new ArrayList<>();
//                List<Task> curStack = new ArrayList<>();
//                List<JProgressBar> barStack = new ArrayList<>();
//                while (true) {
//                    while (update) {
//                        update = false;
//                        Task t = curTask;
//                        if (t == Task.POISON) {
//                            return;
//                        }
//                        if (t == null) {
//                            if (!lastStack.isEmpty() || !curStack.isEmpty()) {
//                                lastStack.clear();
//                                curStack.clear();
//                                barStack.clear();
//                                panel.removeAll();
//                            }
//                            EventQueue.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    progressFrame.dispose();
//                                }
//                            });
//                            continue;
//                        }
//                        curStack.clear();
//                        Task next = t;
//                        while (next != null) {
//                            curStack.add(next);
//                            if (next instanceof TaskList) {
//                                next = ((TaskList) next).getTask();
//                            } else {
//                                next = null;
//                            }
//                        }
//                        int i = 0;
//                        while (i < lastStack.size() && i < curStack.size() && lastStack.get(i) == curStack.get(i)) {
//                            float progress = curStack.get(i).getProgress();
//                            JProgressBar b = barStack.get(i);
//                            if (progress < 0.0F || progress > 1.0F) {
//                                b.setIndeterminate(true);
//                            } else {
//                                b.setIndeterminate(false);
//                                b.setValue((int) (progress * 2000));
//                            }
//                            i++;
//                        }
//                        for (int L = lastStack.size() - 1; L >= i; --L) {
//                            lastStack.remove(L);
//                            panel.remove(barStack.remove(L));
//                        }
//                        for (; i < curStack.size(); ++i) {
//                            next = curStack.get(i);
//                            float progress = next.getProgress();
//                            JProgressBar b = new JProgressBar(0, 2000);
//                            b.setMinimumSize(new Dimension(200, 40));
//                            b.setStringPainted(true);
//                            b.setString(next.getDescription());
//                            if (progress < 0.0F || progress > 1.0F) {
//                                b.setIndeterminate(true);
//                            } else {
//                                b.setIndeterminate(false);
//                                b.setValue((int) (progress * 2000));
//                            }
//                            barStack.add(b);
//                            panel.add(b);
//                            lastStack.add(next);
//                        }
//                        EventQueue.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (!progressFrame.isVisible()) {
//                                    progressFrame.setVisible(true);
//                                }
//                                progressFrame.pack();
//                            }
//                        });
//                    }
//                    synchronized (monitorLock) {
//                        if (!update) {
//                            try {
//                                if (curStack.size() > 1) {
//                                    monitorLock.wait(200);
//                                } else {
//                                    monitorLock.wait();
//                                }
//                            } catch (InterruptedException e) {
//                                Thread.currentThread().interrupt();
//                                return;
//                            }
//                        }
//                    }
//                }
//            }
//        }, "TaskManager Monitor");
//        monitorLock = new Object();
        // </editor-fold>
    }

    public synchronized void startTaskThread() {
        taskThread.setDaemon(true);
//        monitorThread.setDaemon(true);
//        monitorThread.start();
        taskThread.start();
    }

    public void waitOnTasks() {
        synchronized (tasks) {
            if (Thread.currentThread() == taskThread) {
                throw new IllegalStateException("Cannot wait on tasks from the task thread.");
            }
            if (tasks.isEmpty() && curTask == null) {
                return;
            }
            try {
                tasks.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void updateProgress() {
        try {
//            synchronized (monitorLock) {
//                update = true;
//                monitorLock.notify();
//            }
            if (curTask == null) {
                if (!lastStack.isEmpty() || !curStack.isEmpty()) {
                    lastStack.clear();
                    curStack.clear();
                    barStack.clear();
                    panel.removeAll();
                }
                progressFrame.dispose();
                return;
            }
            curStack.clear();
            Task next = curTask;
            while (next != null) {
                curStack.add(next);
                if (next instanceof TaskList) {
                    next = ((TaskList) next).getTask();
                } else {
                    next = null;
                }
            }
            int i = 0;
            while (i < lastStack.size() && i < curStack.size() && lastStack.get(i) == curStack.get(i)) {
                float progress = curStack.get(i).getProgress();
                JProgressBar b = barStack.get(i);
                if (progress < 0.0F || progress > 1.01F) {
                    b.setIndeterminate(true);
                } else {
                    b.setIndeterminate(false);
                    b.setValue(Math.min(2000, (int) (progress * 2000)));
                }
                i++;
            }
            for (int L = lastStack.size() - 1; L >= i; --L) {
                lastStack.remove(L);
                panel.remove(barStack.remove(L));
            }
            for (; i < curStack.size(); ++i) {
                next = curStack.get(i);
                float progress = next.getProgress();
                JProgressBar b = new JProgressBar(0, 2000);
                b.setMinimumSize(new Dimension(200, 40));
                b.setStringPainted(true);
                b.setString(next.getDescription());
                if (progress < 0.0F || progress > 1.0F) {
                    b.setIndeterminate(true);
                } else {
                    b.setIndeterminate(false);
                    b.setValue((int) (progress * 2000));
                }
                barStack.add(b);
                panel.add(b);
                lastStack.add(next);
            }
            if (!progressFrame.isVisible()) {
                progressFrame.setVisible(true);
            }
            progressFrame.pack();
        } catch (Exception e) {
            Launcher.getLogger().log(Level.SEVERE, "Problem updating task manager progress.", e);
        }
    }

    public boolean isFinished() {
        return tasks.isEmpty();
    }

    public void addTask(Task t) {
        t.makeFinal(this);
        try {
            if (!shutdown) {
                tasks.put(t);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
        this.shutdown = true;
        try {
            tasks.put(Task.POISON);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        taskThread.interrupt();
    }
}
