package edu.curtin.saed.assignment1;


import java.util.concurrent.BlockingQueue;

public class AddWall implements Runnable {

    private BlockingQueue<Wall> queue;
    private JFXArena arena;
    private Logger logger;
    private volatile boolean isRunning;
    
    public AddWall(BlockingQueue<Wall> q,JFXArena arena,Logger logger){
        this.queue=q;
        this.arena = arena;
        this.logger = logger;
    }

    public void addToQueue(int gridX, int gridY, int wallID) {
        // Check if a robot is at the target position
    	if(isRunning) {
        if (arena.isRobotAtPosition(gridX, gridY)) {
            System.out.println("Wall not added. Square occupied by a robot.");
            return; 
        }

        // Check if a wall is already scheduled or built at the target position
        if (arena.isWallAtPosition(gridX, gridY)) {
            System.out.println("Wall not added. Another wall already scheduled or built.");
            return; 
        }

        Wall wall = new Wall(wallID, gridX, gridY, arena.wallImage);
        try {
        	// add wall into queue
            queue.put(wall);
            logger.log("Wall " + wallID + " is added to queue");
        } catch (InterruptedException e) {
            // Handle the exception appropriately
            System.out.println("Wall not added due to interruption.");
        }
    	}
    }

    @Override
    public void run() {
    	isRunning = true;
    }
    
    public void stop() {
    	isRunning = false;
    }

}