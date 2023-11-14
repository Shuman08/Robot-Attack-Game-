package edu.curtin.saed.assignment1;

import java.util.Random;

public class Robot implements Runnable {
    private int id;
    private RobotMoveHandler moveHandler;
    private int x;
    private int y;
    private int delay;
    private Random random = new Random();
    private int gridWidth; 
    private int gridHeight; 
    private boolean isRunning = true; 
   private int min = 500;
   private int max = 2000;
    
    public Robot(int id, JFXArena arena, int initialX, int initialY,
                 int gridWidth, int gridHeight) {

        this.id = id;
        this.x = initialX;
        this.y = initialY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.delay = 1500+ random.nextInt(max-min)+min;
        
        moveHandler = new RobotMoveHandler(arena,gridWidth, gridHeight);

    }

    @Override
    public void run() {
        while (isRunning) {
        	try {
        		//check if current thread itself is not interrupted
        		if (!Thread.currentThread().isInterrupted()) {
            

            int newTargetX = x;
            int newTargetY = y;
            
            //calculate a random move
            do {
            	int randomDirection = random.nextInt(4);
                switch (randomDirection) {
                    case 0: // Move left
                        newTargetX = Math.max(0, x - 1);
                        break;
                    case 1: // Move right
                        newTargetX = Math.min(gridWidth - 1, x + 1);
                        break;
                    case 2: // Move up
                        newTargetY = Math.max(0, y - 1);
                        break;
                    case 3: // Move down
                        newTargetY = Math.min(gridHeight - 1, y + 1);
                        break;
                    default:
                        // Do nothing in the default case
                        break;
                }
            } while (newTargetX == x && newTargetY == y); // Keep trying until a valid move is found

            // The delay for each moves
            	Thread.sleep(delay);
       //     	System.out.println("robot " + id + " delay for  " + delay);
            	moveHandler.handleRobotMove(this, newTargetX, newTargetY);
            	
                
        		 }
            } catch (InterruptedException e) {
            	System.out.println("robot " + id + " thread ended ");
               //  break;
            }
        }
    }

    public int getId() {
        return id;
    }

	public void setX(int x2) {
		this.x  = x2;
	}

	public void setY(int y2) {
		this.y = y2;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getDelay()
	{
		return delay;
	}
	public boolean isRobotAlive()
	{
		return isRunning;
	}
	// Method to stop the robot thread
	  public void stopRobot() {
	        isRunning = false; 
	    }

}

