package edu.curtin.saed.assignment1;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.image.Image;

public class Wall {
	private int id;
    private int x;
    private int y;
    private Image image;
    private Image damagedWall;
    private int status; // To recognize if wall is 100% or damaged (50%)
    private static final String DAMAGEDWALL_FILE ="181479.png";

    public Wall(int id,int x, int y, Image image) {
        this.id = id;
    	this.x = x;
        this.y = y;
        this.image = image;
        this.status = 100; //wall is 100% 

        try(InputStream is = getClass().getClassLoader().getResourceAsStream(DAMAGEDWALL_FILE))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + DAMAGEDWALL_FILE);
            }
            damagedWall = new Image(is);
         
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + DAMAGEDWALL_FILE, e);
        }
        
        
    }
    
    public Wall (int id)
    {
    	this.id = id;
    }
    public int getId()
    {
    	return id;
    }

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Image getImage() {
		if (status == 50) {
		 return damagedWall;
		} else {
		   return image;
		}
		
	}

	public void setImage(Image image) {
		this.image = image;
	}

    public int getStatus()
    {
    	return status;
    }
    
    public void setStatus(int newStatus)
    {
    	status =newStatus;

    }
    
}