package edu.curtin.saed.assignment1;

import java.util.concurrent.BlockingQueue;

public class BuildWall implements Runnable{

private BlockingQueue<Wall> queue;
private JFXArena arena;
private Logger logger;

    public BuildWall(BlockingQueue<Wall> q,JFXArena arena,Logger logger){
        this.queue=q;
        this.arena = arena ;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (true) {
        	// Check if the wall build is less than 10
            if (arena.fortressWallsBuilt < 10) {
                try {
                	Thread.sleep(2000); // delay for building next wall
                	
                    Wall wall = queue.take(); 
                    if (wall != null) {
                        // Check if the wall can be built
                        if (arena.fortressWallsBuilt < 10 && !arena.isRobotAtPosition(wall.getX(), wall.getY())) {
                        	//increase the number of wall if the wall can be build
                            arena.fortressWallsBuilt++;
                            // reques GUI to update
                            arena.requestLayout();
                            logger.log("Wall " + wall.getId() + " built successfully at (" + wall.getX() + ", " + wall.getY() + ")");
                            if (arena.walls.size() < 10) {
                                arena.walls.add(wall);
                            }
                        } else if (arena.isRobotAtPosition(wall.getX(), wall.getY())) {
                            // Handle the case where a robot is at the target position 
                        	System.out.println("Wall " + wall.getId() + " not build because robot occupied this grid");
                        	// Remove from wall list because it will be ignored
                        	arena.walls.remove(wall); 
                        }
                    }
                } catch (InterruptedException e) {
                	System.out.println("Wall building thread interrupted while waiting");
                    break; 
                }
            } else {
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                	System.out.println("Wall building thread ended");
                    break; 
                }
            }
        }
    }

}