package edu.curtin.saed.assignment1;


import javafx.animation.AnimationTimer;

public class RobotMoveHandler {
    private Object[][] positionLocks; // Use a 2D array of locks for each grid position
    private boolean[][] positionOccupied; // Use a 2D array to track occupied positions
    private int gridWidth;
    private int gridHeight;
    private JFXArena arena;
   
    public RobotMoveHandler( JFXArena arena ,int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        positionLocks = new Object[gridWidth][gridHeight];
        positionOccupied = new boolean[gridWidth][gridHeight];
        this.arena = arena;

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                positionLocks[x][y] = new Object();
            }
        }
    }

   // Ensure that only one robot can occupy the target position at a time
    public void handleRobotMove(Robot robot, int targetX, int targetY) {
        int gridX =targetX;
        int gridY =targetY;

      
        synchronized (positionLocks[gridX][gridY]) {
        	 if (positionOccupied[gridX][gridY]) {
                 // Target position is already occupied, then find a empty position randomly 
                 findRandomEmptySpace(robot);
             } else {
              
               positionOccupied[gridX][gridY] = true;
               positionOccupied[robot.getX()][robot.getY()]=true;
               //Handle the robot movement animation here 
                AnimationTimer timer = new AnimationTimer() {
                    private long startTime = System.currentTimeMillis()+ robot.getDelay();
                    private long duration = 2000; // Animation duration in milliseconds

                   
                    @Override
                    public void handle(long now) {
                        long elapsedTime = System.currentTimeMillis() - startTime  ;

                        if (elapsedTime < 0) {
                            // Delay period, do nothing
                            return;
                        }

                        double progress = (double) elapsedTime / duration;

                        if (progress >= 1.0) {
                        	// Release the previous position after the animation
                        	
                        	releasePosition(robot.getX(), robot.getY());
                            
                        	// Set the robot X and Y after animation completed
                            robot.setX(targetX);
                            robot.setY(targetY);

                            stop(); // Stop the animation
                        } else {
                            // Interpolate the robot's position between the current and target positions
                            int interpolatedX = (int) (robot.getX() + (targetX - robot.getX()) * progress);
                            int interpolatedY = (int) (robot.getY() + (targetY - robot.getY()) * progress);

                            robot.setX(interpolatedX);
                            robot.setY(interpolatedY);
                        }
                    }
                };

                timer.start(); 
            //  System.out.println(" Robot " + robot.getId() + " move from (" + previousX + "," + previousY  +") to (" + targetX + "," + targetY +")");

            } 
        }
        	arena.requestLayout();
    }

    private void findRandomEmptySpace(Robot robot) {
        // Generate random positions until an empty space is found
        while (true) {
            int randomX ; 
            int randomY ;

            do {
                randomX = (int) (Math.random() * gridWidth); // Adjust to your grid size
                randomY = (int) (Math.random() * gridHeight);
            } while (randomX == 4 && randomY == 4); // Cannot randomly move to citadel 
            synchronized (positionLocks[randomX][randomY]) {
                if (!positionOccupied[randomX][randomY]) {
                    
                	// Reserve the targeted position positions
                    positionOccupied[randomX][randomY] = true;

                	// Release the previous position
                    releasePosition(robot.getX(),robot.getY());

                    
                    
                    // Update the robot's position
                    robot.setX(randomX);
                    robot.setY(randomY);

          //        System.out.println("Robot " + robot.getId() + " randomly moved to (" +robot.getX() + "," + robot.getY()+")");
                    
                    break; 
                    
                }
            }
        }
    }

    public void releasePosition(int x, int y) {
        // Release a position after the robot has moved away
        synchronized (positionLocks[x][y]) {
            positionOccupied[x][y] = false;
        }
    }
}