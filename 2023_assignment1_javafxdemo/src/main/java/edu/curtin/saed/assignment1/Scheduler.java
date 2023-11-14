package edu.curtin.saed.assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private List<Robot> robotList = new ArrayList<>();
    private ExecutorService executor;

    public void addRobot(Robot newRobot) {
        synchronized (robotList) {
            robotList.add(newRobot);
         //   System.out.println("Adding New Robot");
        }
    }

    public void start() {
        executor = Executors.newCachedThreadPool();

        // Submit all robot tasks to the executor
        for (Robot robot : robotList) {
        	if(robot.isRobotAlive())
        	{
            executor.execute(robot);
       //    System.out.println("\nRobot thread started for robot ID: " + robot.getId());
        	}
            // Introduce a small delay between starting each robot thread
            try {
                TimeUnit.MILLISECONDS.sleep(100); 
            } catch (InterruptedException e) {
            	 System.out.println("scheduler thread ended");
            }
        }
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow(); // Interrupt threads in the pool immediately
        }
    }

    public void stopAllRobots() {
        synchronized (robotList) {
            for (Robot robot : robotList) {
                robot.stopRobot();
            }
            robotList.clear(); // Clear the robot list to release resources
        }
    }
}
