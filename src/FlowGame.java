
//Direction type	Unicode char	Description
//     1    	        ─	        left-right
//     2    	        │	        top-bottom
//     3    	        ┘	        top-left
//     4   	            └	        top-right
//     5    	        ┐	        bottom-left
//     6    	        ┌	        bottom-right

//  Every cell is assigned a single color.
//  The color of every endpoint cell is known and specified.
//  Every endpoint cell has exactly one neighbor which matches its color.
//  The flow through every non-endpoint cell matches exactly one of the six direction types.
//  The neighbors of a cell specified by its direction type must match its color.
//  The neighbors of a cell not specified by its direction type must not match its color.


import java.util.HashMap;
import java.io.File;
import java.io.InputStream;

public class FlowGame{
    static int LEFT =   1 << 0;
    static int RIGHT =  1 << 1;
    static int TOP =    1 << 2;
    static int BOTTOM = 1 << 3;

    static int LEFT_TOP = LEFT | TOP;
    static int TOP_BOTTOM = TOP | BOTTOM;
    static int TOP_LEFT = TOP | LEFT;
    static int TOP_RIGHT = TOP | RIGHT;
    static int BOTTOM_LEFT = BOTTOM | LEFT;
    static int BOTTOM_RIGHT = BOTTOM | RIGHT;

    static int[] DIRECTION_TYPES = new int[]{LEFT_TOP, TOP_BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};
    static HashMap<Integer, String> DIRECTION_CHARS = new  HashMap<Integer, String>() {{
        put(LEFT_TOP,   "─");
        put(TOP_BOTTOM, "│");
        put(TOP_LEFT, "┘");
        put(TOP_RIGHT, "└");
        put(BOTTOM_LEFT, "┐");
        put(BOTTOM_RIGHT, "┌");
    }};


    public void parsePuzzle(String filePosition){
        //InputStream input = getClass().getResourceAsStream("ListStopWords.txt");
        String relativeFilePosition = "puzzles/" + filePosition;
        System.out.println(getClass().getResource(relativeFilePosition));
    }

    static public void main(String[] args){
        FlowGame game = new FlowGame();
        game.parsePuzzle("extreme_8x8_01.txt");
    }

}
