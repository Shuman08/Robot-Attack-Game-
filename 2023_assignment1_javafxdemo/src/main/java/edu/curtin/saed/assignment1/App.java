package edu.curtin.saed.assignment1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application 
{


    public static void main(String[] args) 
    {
        launch();        
    }
    
    @Override
    public void start(Stage stage) 
    {
    
        stage.setTitle("Example App (JavaFX)");
        TextArea logger = new TextArea();
        Label scoreLabel = new Label();  
        Label queueLabel = new Label();  
        Score score = new Score();
        Logger loggers = new Logger(logger);
        JFXArena arena = new JFXArena(score,loggers,scoreLabel,queueLabel);
        
        arena.addListener((x, y) ->
        {
           // System.out.println("Arena click at (" + x + "," + y + ")");
            if(x==4 && y==4)
            {
            	 System.out.println("Wall cannot be build at the citadel");
            }
            else {
            	arena.addWall(x, y);
            
            }
        });
               
        
        

        
        ToolBar toolbar = new ToolBar();
//         Button btn1 = new Button("My Button 1");
//         Button btn2 = new Button("My Button 2");
        
       
        
//         toolbar.getItems().addAll(btn1, btn2, label);
        toolbar.getItems().addAll(scoreLabel,queueLabel);
        
//         btn1.setOnAction((event) ->
//         {
//             System.out.println("Button 1 pressed");
//         });
                    
//        TextArea logger = new TextArea();
//        logger.appendText("Hello\n");
//        logger.appendText("World\n");
        
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, logger);
        arena.setMinWidth(300.0);
        
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);
        
 
        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
        
    }
    
    
}
