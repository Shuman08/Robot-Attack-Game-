package edu.curtin.saed.assignment1;

import javafx.scene.canvas.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

/**
 * A JavaFX GUI element that displays a grid on which you can draw images, text and lines.
 */
public class JFXArena extends Pane
{
    // Represents an image to draw, retrieved as a project resource.
    private static final String IMAGE_FILE = "1554047213.png";
    private static final String CITADEL_FILE ="rg1024-isometric-tower.png";
    private static final String WALL_FILE ="181478.png";
    private Image robot1;
    private Image citadel;
    public Image wallImage;
    private Scheduler scheduler ;
    public int fortressWallsBuilt = 0;
    private int wallID=0;
    private Score score;
    private Logger logger;
    private Label scoreLabel;
    private Label queueLabel;
    private AddWall producer;
    private Thread producerThread;
    private Thread consumerThread;
    private Thread scoreThread;
    private Thread queueThread;
    // The following values are arbitrary, and you may need to modify them according to the 
    // requirements of your application.
    private int gridWidth = 9;
    private int gridHeight = 9;
//    private double robotX = 1.0;
//    private double robotY = 3.0;
    private int currentRobotIndex = 0;
    private double gridSquareSize; // Auto-calculated
    private Canvas canvas; // Used to provide a 'drawing surface'.
    private List<Robot> robots;
    private List<ArenaListener> listeners = null;
    private final ScheduledExecutorService robotSpawnExecutor = Executors.newScheduledThreadPool(10);
    private BlockingQueue<Wall> wallQueue = new LinkedBlockingQueue<>();
    public List<Wall> walls = new ArrayList<>();
 //   private final ScheduledExecutorService wallBuildExecutor = Executors.newScheduledThreadPool(5);
    
    
    /**
     * Creates a new arena object, loading the robot image and initialising a drawing surface.
     */
    public JFXArena(Score score,Logger logger,Label slabel , Label qlabel)
    {
        // Here's how (in JavaFX) you get an Image object from an image file that's part of the 
        // project's "resources". If you need multiple different images, you can modify this code 
        // accordingly.
        
        // (NOTE: _DO NOT_ use ordinary file-reading operations here, and in particular do not try
        // to specify the file's path/location. That will ruin things if you try to create a 
        // distributable version of your code with './gradlew build'. The approach below is how a 
        // project is supposed to read its own internal resources, and should work both for 
        // './gradlew run' and './gradlew build'.)
    	 robots = new ArrayList<>(); 
    	 scheduler  = new Scheduler();
    	 this.score = score;
    	 this.logger = logger;
    	 this.scoreLabel = slabel;
    	 this.queueLabel = qlabel;
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(IMAGE_FILE))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + IMAGE_FILE);
            }
            robot1 = new Image(is);
         
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + IMAGE_FILE, e);
        }
        
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(CITADEL_FILE))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + CITADEL_FILE);
            }
            citadel = new Image(is);
         
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + CITADEL_FILE, e);
        }
        
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(WALL_FILE))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + WALL_FILE);
            }
            wallImage = new Image(is);
         
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + WALL_FILE, e);
        }

        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        initialize(); // Call initialize for spawningRobot 

        producer = new AddWall(wallQueue, this, logger); 
        BuildWall consumer = new BuildWall(wallQueue, this, logger);

       // create new thread for aadding walls and building wall
        producerThread = new Thread(producer);
        consumerThread = new Thread(consumer);
        producerThread.start();
        consumerThread.start();
        
        
        //create a timer to handle the collision
        AnimationTimer collisionTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleCollision(); // Call the collision handling method
            }
        };

        collisionTimer.start(); // Start the collision timer
        
        
        //create a score Thread for updating the score
        scoreThread = new Thread(() -> {
        	while (!isGameOver()) { // Check the gameOver flag
                try {
                    Thread.sleep(1000); // Sleep for 1 second

                    Platform.runLater(() -> {
                        // Update the score by adding 10 points every second
                        score.increaseScore(10);

                        // Update the label with the new score
                        scoreLabel.setText("Score: " + score.getScore());
                    });

                } catch (InterruptedException e) {
                   System.out.println("score thread ended");

                   Platform.runLater(() -> {
                       // Update the label with a game over message
                       scoreLabel.setText("Game Over! Final Score: " + score.getScore());
                   }); 
                   break;
                }
            }
        });        
        scoreThread.start();
        
        //create a queue Thread for updating the number of wall waiting to build in queue
         queueThread = new Thread(() -> {
            while (!isGameOver()) { // Check the gameOver flag
            	
                try {
                    Thread.sleep(1000); // Sleep for 1 second

                    Platform.runLater(() -> {
                        // Update queueLabel with the current wall queue size
                        queueLabel.setText("\t\t\tQueue Size: " + getWallQueueSize());

                    });
                } catch (InterruptedException e) {
               	 System.out.println("queue thread ended");
               	 
               	 //log a message showing that game over
                 logger.log("Game over!!!");
                 //display a gameOverDialog
                 gameOverDialog();
                 break;
                }
            }
        });
        queueThread.start();

        
    }
    public final void initialize() {
        startSpawningRobots();
    }


    
    // Other methods and code related to drawing the arena...

    /**
     * Moves a robot image to a new grid position. This is highly rudimentary, as you will need
     * many different robots in practice. This method currently just serves as a demonstration.
     */
//    public void setRobotPosition(double x, double y)
//    {
//        robotX = x;
//        robotY = y;
//        requestLayout();
//    }
//    
    /**
     * Adds a callback for when the user clicks on a grid square within the arena. The callback 
     * (of type ArenaListener) receives the grid (x,y) coordinates as parameters to the 
     * 'squareClicked()' method.
     */
    public void addListener(ArenaListener newListener)
    {
        if(listeners == null)
        {
            listeners = new LinkedList<>();
            setOnMouseClicked(event ->
            {
                int gridX = (int)(event.getX() / gridSquareSize);
                int gridY = (int)(event.getY() / gridSquareSize);
                
                if(gridX < gridWidth && gridY < gridHeight)
                {
                    for(ArenaListener listener : listeners)
                    {   
                        listener.squareClicked(gridX, gridY);
                    }
                }
            });
        }
        listeners.add(newListener);
    }


    // start spawning the robots with intervals of 1500 milliseconds
    public final void startSpawningRobots() {
    	  if (!isGameOver()) {
	        robotSpawnExecutor.schedule(() -> {
	            // Spawn a new robot
	            if (!isGameOver()) {
	           spawnRobot(currentRobotIndex);
	            scheduler.start();
	            currentRobotIndex++;
	            // Schedule the next spawn after the delay
	            startSpawningRobots();
	            }
	        }, 1500, TimeUnit.MILLISECONDS);
    	
    	  }
    }

    //Responsible to spawn robot at unoccupied corner
    private void spawnRobot(int index) {
    	if(!isGameOver())
    	{
	    	List<Corner> unoccupiedCorners = getUnoccupiedCorners();
	        Corner corner = unoccupiedCorners.remove(0); // Remove and use the first unoccupied corner
	
	        Robot robot = new Robot(index+1, this, corner.getX(), corner.getY(), gridWidth, gridHeight);
	        robots.add(robot);
	        scheduler.addRobot(robot);
	        setRobotPosition(index, corner.getX(),corner.getY()); // Set initial position
	        logger.log("Robot " + (index+1) + "  spawned at( " + corner.getX() + "," +corner.getY() + ")");
    	}
    }
   
    // handling user mouseclick event in App 
    public void addWall(int gridX,int gridY)
    {
    	
    	wallID++;
    	producer.addToQueue(gridX, gridY, wallID); // Add the wall to the queue
    	requestLayout();
    }


    // method to get the unoccupied corner for spawning robots
    private List<Corner> getUnoccupiedCorners() {
        List<Corner> unoccupiedCorners = new ArrayList<>();
        
        if (!isCornerOccupied(0, 0)) {
            unoccupiedCorners.add(new Corner(0, 0));
        }
        if (!isCornerOccupied(gridWidth - 1, 0)) {
            unoccupiedCorners.add(new Corner(gridWidth - 1, 0));
        }
        if (!isCornerOccupied(0, gridHeight - 1)) {
            unoccupiedCorners.add(new Corner(0, gridHeight - 1));
        }
        if (!isCornerOccupied(gridWidth - 1, gridHeight - 1)) {
            unoccupiedCorners.add(new Corner(gridWidth - 1, gridHeight - 1));
        }
        
        return unoccupiedCorners;
    }

    //check if the corner is occupied
    private boolean isCornerOccupied(int x, int y) {
        for (Robot robot : robots) {
            if (robot.getX() == x && robot.getY() == y) {
                return true;
            }
        }
        return false;
    }
    
    // method to update the robot position
    public void setRobotPosition(int id, int x, int y) {
        if (id >= 0 && id < robots.size()) { // Check if id is within the valid range
            Robot robot = robots.get(id);
            if (robot != null) {
                robot.setX(x);
                robot.setY(y);
                requestLayout();
            }
        }
    }
    
// // Modify the addWall method to check conditions before adding to the queue
//    public synchronized  void addWall(int gridX, int gridY) {
//    	if(queueThread.isAlive()) {
//    		wallID++;
//            // Check if a robot is at the target position
//            if (isRobotAtPosition(gridX, gridY)) {
//                System.out.println("Wall not added. Square occupied by a robot.");
//                return; // Do not add the wall if a robot is present
//            }
//
//            // Check if a wall is already scheduled or built at the target position
//            if (isWallAtPosition(gridX, gridY)) {
//                System.out.println("Wall not added. Another wall already scheduled or built.");
//                return; // Do not add the wall if a wall is present
//            }
//            Wall wall = new Wall(wallID,gridX, gridY, wallImage);
//            	try {
//					wallQueue.put(wall);
//					logger.log("Wall " + wallID + "is added to queue");
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					System.out.println("Wall not added.");
//				}
//            	
//
//    }
//
//    }
//
    //method to check if there is already a wall at the position
   public boolean isWallAtPosition(int x, int y) {
        for (Wall wall : walls) {
            if (wall.getX() == x && wall.getY() == y) {
                return true;
            }
        }
        return false;
    }

//    
//    
//    public void startBuildingWalls() {
//        wallBuildExecutor.scheduleWithFixedDelay(() -> {
//            // Try to build a wall from the queue
//        	 if (fortressWallsBuilt < 10) {
//            try {
//                Wall wall = wallQueue.take();
//                if (wall != null) {
//                    // Check if the wall can be built
//                    if (fortressWallsBuilt < 10 && !isRobotAtPosition(wall.getX(), wall.getY())) {
//                        // Build the wall if conditions are met
//                        fortressWallsBuilt++;
//                       // addWall(wall.getX(), wall.getY());
//                        requestLayout(); // Request a layout update on the JavaFX application thread
//                       // System.out.println("Wall " + wall.getId() +" built successfully at (" + wall.getX() + ", " + wall.getY() + ")");
//                        logger.log("Wall " + wall.getId() +" built successfully at (" + wall.getX() + ", " + wall.getY() + ")");
//                        if(walls.size()<10)
//                        {
//                        	     walls.add(wall); // Add the wall to the list of built walls
//                        }
//                       
//                    } else if(isRobotAtPosition(wall.getX(), wall.getY())){
//                     //   System.out.println("Wall " + wall.getId() + " build is ignored because square occupied by a robot.");
//                        walls.remove(wall);
//                    }else {
//                    //	System.out.println("Wall " + wall.getId() + " is waiting in the queue.");
//                    }
//                }
//            } catch (InterruptedException e) {
//            	 System.out.println("wall building thread pool ended");
//            }}
//        }, 0, 2000, TimeUnit.MILLISECONDS);
//    }

   // check if the position is already occupied by a robot
    public boolean isRobotAtPosition(int x, int y) {
        for (Robot robot : robots) {
            if (robot.getX() == x && robot.getY() == y) {
                return true;
            }
        }
        return false;
    }

    // method to handle the collision of robot and wall 
    public void handleCollision() {
        Iterator<Robot> iterator = robots.iterator();
        List<Wall> wallsToRemove = new ArrayList<>();

        while (iterator.hasNext()) {
            Robot robot = iterator.next();
            int robotX = robot.getX();
            int robotY = robot.getY();
            boolean collisionDetected = false;

            for (Wall wall : walls) {
                int wallX = wall.getX();
                int wallY = wall.getY();

                // Check if the robot and wall overlap
                if (robotX == wallX && robotY == wallY) {
                	// if yes that set collisionDetect to true
                    collisionDetected = true;
                    System.out.println("Robot " + robot.getId() + " hits the wall.");

                    // updating the wall status based on the collisions
                    if(wall.getStatus()==100)
                    {
                    	wall.setStatus(50);
                    	logger.log("Wall " + wall.getId() + " is weakened! Beware of the robot destroying the walls!");
                  //  	System.out.println("Wall " + wall.getId() + " is weakened! Beware of the robot destroying the walls!");
                    	
                    }
                    else if(wall.getStatus()==50)
                    {
                    	wall.setStatus(0);

                        fortressWallsBuilt--; // Decrement the count of built walls when second collision
                 //   	System.out.println("Wall " + wall.getId() + " is destroyed! Build more walls to protect the citadel!!");
                    }
                  
                }
                
            	if(wall.getStatus()==0)
        		{
            		wallsToRemove.add(wall);
                	logger.log("Wall " + wall.getId() + " is destroyed! Build more walls to protect the citadel!!");

        		}
        		
            }
            
            walls.removeAll(wallsToRemove);

         
            if (collisionDetected) {
            	score.increaseScore(100);
            	//stop the robot thread
            	robot.stopRobot();
                iterator.remove(); // Remove the robot safely from the list using the iterator
                System.out.println("Robot " + robot.getId() + " stopped.");
            }
            
        }
        
        
        if(isGameOver())
        	
        { 
        	// stop all the thread
        	producer.stop();
            producerThread.interrupt();
            consumerThread.interrupt();
            
            
        	scheduler.stopAllRobots();
            scheduler.stop(); 
            robotSpawnExecutor.shutdownNow();

            queueThread.interrupt();
            scoreThread.interrupt();
            
        

        }
        requestLayout();
        
    }
    
    // a game over dialog
    public void gameOverDialog()
    {
    	 Platform.runLater(() -> {
        Alert alert = new Alert(AlertType.INFORMATION);
        
        alert.setTitle("Game over Dialog");
        alert.setHeaderText("Robot attacked the citadel. Game over!");
        alert.setContentText(scoreLabel.getText());
        
        alert.show();
        
    	 });
    }

    // check if the game is over 
    public final boolean isGameOver() {
        for (Robot robot : robots) {
            if (robot.getX() == 4 && robot.getY() == 4) {
            //	System.out.println("Robot " + robot.getId() + " attacked the citadel at ( " + robot.getX() + "," + robot.getY() + ")!" );
                 return true; // Game is over if a robot reaches the citadel
               
            }
        }
        return false; // Game is not over
    }
    
    // get queuesize for the queueThread
    public String getWallQueueSize()
    {
    	return String.valueOf(wallQueue.size());
    }
    


    /**
     * This method is called in order to redraw the screen, either because the user is manipulating 
     * the window, OR because you've called 'requestLayout()'.
     *
     * You will need to modify the last part of this method; specifically the sequence of calls to
     * the other 'draw...()' methods. You shouldn't need to modify anything else about it.
     */
    @Override
    public void layoutChildren()
    {
        super.layoutChildren(); 
        GraphicsContext gfx = canvas.getGraphicsContext2D();
        gfx.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        
        // First, calculate how big each grid cell should be, in pixels. (We do need to do this
        // every time we repaint the arena, because the size can change.)
        gridSquareSize = Math.min(
            getWidth() / (double) gridWidth,
            getHeight() / (double) gridHeight);
            
        double arenaPixelWidth = gridWidth * gridSquareSize;
        double arenaPixelHeight = gridHeight * gridSquareSize;
            
            
        // Draw the arena grid lines. This may help for debugging purposes, and just generally
        // to see what's going on.
        gfx.setStroke(Color.DARKGREY);
        gfx.strokeRect(0.0, 0.0, arenaPixelWidth - 1.0, arenaPixelHeight - 1.0); // Outer edge

        for(int gridX = 1; gridX < gridWidth; gridX++) // Internal vertical grid lines
        {
            double x = (double) gridX * gridSquareSize;
            gfx.strokeLine(x, 0.0, x, arenaPixelHeight);
        }
        
        for(int gridY = 1; gridY < gridHeight; gridY++) // Internal horizontal grid lines
        {
            double y = (double) gridY * gridSquareSize;
            gfx.strokeLine(0.0, y, arenaPixelWidth, y);
        }
        
        drawImage(gfx,citadel,4.0,4.0);
        
        for (Robot robot : robots) {
            drawImage(gfx, robot1, robot.getX(), robot.getY());
            drawLabel(gfx, String.valueOf(robot.getId()), robot.getX(), robot.getY());
        }
       
        //draw Wall
        for (Wall wall : walls) {
            drawImage(gfx, wall.getImage(), wall.getX(), wall.getY());
        }


        // Invoke helper methods to draw things at the current location.
        // ** You will need to adapt this to the requirements of your application. **

//        drawImage(gfx, robot1, robotX, robotY);
//        drawLabel(gfx, "Robot Name", robotX, robotY);
        
        
        
        
        
    }
    
    
    /** 
     * Draw an image in a specific grid location. *Only* call this from within layoutChildren(). 
     *
     * Note that the grid location can be fractional, so that (for instance), you can draw an image 
     * at location (3.5,4), and it will appear on the boundary between grid cells (3,4) and (4,4).
     *     
     * You shouldn't need to modify this method.
     */
    private void drawImage(GraphicsContext gfx, Image image, double gridX, double gridY)
    {
        // Get the pixel coordinates representing the centre of where the image is to be drawn. 
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;
        
        // We also need to know how "big" to make the image. The image file has a natural width 
        // and height, but that's not necessarily the size we want to draw it on the screen. We 
        // do, however, want to preserve its aspect ratio.
        double fullSizePixelWidth = robot1.getWidth();
        double fullSizePixelHeight = robot1.getHeight();
        
        double displayedPixelWidth, displayedPixelHeight;
        if(fullSizePixelWidth > fullSizePixelHeight)
        {
            // Here, the image is wider than it is high, so we'll display it such that it's as 
            // wide as a full grid cell, and the height will be set to preserve the aspect 
            // ratio.
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        }
        else
        {
            // Otherwise, it's the other way around -- full height, and width is set to 
            // preserve the aspect ratio.
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        // Actually put the image on the screen.
        gfx.drawImage(image,
            x - displayedPixelWidth / 2.0,  // Top-left pixel coordinates.
            y - displayedPixelHeight / 2.0, 
            displayedPixelWidth,              // Size of displayed image.
            displayedPixelHeight);
    }
    
    
    /**
     * Displays a string of text underneath a specific grid location. *Only* call this from within 
     * layoutChildren(). 
     *     
     * You shouldn't need to modify this method.
     */
    private void drawLabel(GraphicsContext gfx, String label, double gridX, double gridY)
    {
        gfx.setTextAlign(TextAlignment.CENTER);
        gfx.setTextBaseline(VPos.TOP);
        gfx.setStroke(Color.BLUE);
        gfx.strokeText(label, (gridX + 0.5) * gridSquareSize, (gridY + 1.0) * gridSquareSize);
    }
    
    /** 
     * Draws a (slightly clipped) line between two grid coordinates.
     *     
     * You shouldn't need to modify this method.
     */
    private void drawLine(GraphicsContext gfx, double gridX1, double gridY1, 
                                               double gridX2, double gridY2)
    {
        gfx.setStroke(Color.RED);
        
        // Recalculate the starting coordinate to be one unit closer to the destination, so that it
        // doesn't overlap with any image appearing in the starting grid cell.
        final double radius = 0.5;
        double angle = Math.atan2(gridY2 - gridY1, gridX2 - gridX1);
        double clippedGridX1 = gridX1 + Math.cos(angle) * radius;
        double clippedGridY1 = gridY1 + Math.sin(angle) * radius;
        
        gfx.strokeLine((clippedGridX1 + 0.5) * gridSquareSize, 
                       (clippedGridY1 + 0.5) * gridSquareSize, 
                       (gridX2 + 0.5) * gridSquareSize, 
                       (gridY2 + 0.5) * gridSquareSize);
    }
}
