import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.TaskManager;
import java.awt.EventQueue;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author caucow
 */
public class TaskManagerTest {
    
    public static void main(String[] args) {
        TaskManager tm = new TaskManager(null);
        tm.startTaskThread();
        
        Task t = new Task("Task 1") {
            private float progress = 0.0F;
            
            @Override
            public float getProgress() {
                return progress;
            }
            
            @Override
            public void run() {
                while (progress < 1.0F) {
                    this.progress += progress_int;
                    this.updateProgress();
                    try {
                        Thread.sleep(sleep_int);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        TaskList t2 = new TaskList("Task List Test");
        Task t3 = new Task("Listed Task 1") {
            private float progress = 0.0F;
            
            @Override
            public float getProgress() {
                return progress;
            }
            
            @Override
            public void run() {
                while (progress < 1.0F) {
                    this.progress += progress_int;
                    this.updateProgress();
                    try {
                        Thread.sleep(sleep_int);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Task t4 = new Task("Listed Task 2") {
            private float progress = 0.0F;
            
            @Override
            public float getProgress() {
                return progress;
            }
            
            @Override
            public void run() {
                while (progress < 1.0F) {
                    this.progress += progress_int;
                    this.updateProgress();
                    try {
                        Thread.sleep(sleep_int);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Task t5 = new Task("Listed Task 3") {
            private float progress = 0.0F;
            
            @Override
            public float getProgress() {
                return progress;
            }
            
            @Override
            public void run() {
                while (progress < 1.0F) {
                    this.progress += progress_int;
                    this.updateProgress();
                    try {
                        Thread.sleep(sleep_int);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        t2.addTask(t3);
        t2.addTask(t4);
        t2.addTask(t5);
        
        tm.addTask(t);
        tm.addTask(t2);
        System.out.println("Awaiting first test set.");
        tm.waitOnTasks();
        System.out.println("Finished.");
        tm.addTask(new Task("Final Test") {
            private float progress = 0.0F;
            
            @Override
            public float getProgress() {
                return progress;
            }
            
            @Override
            public void run() {
                while (progress < 1.0F) {
                    this.progress += progress_int;
                    this.updateProgress();
                    try {
                        Thread.sleep(sleep_int);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        System.out.println("Awaiting final test set.");
        tm.waitOnTasks();
        System.out.println("Finished.");
    }
    
    private static float progress_int = 0.1F;
    private static long sleep_int = 10;
}
