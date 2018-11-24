
//Direction type	Unicode char	Description
//     1    	        ─	        left-right
//     2    	        │	        top-bottom
//     3    	        ┘	        top-left
//     4   	            └	        top-right
//     5    	        ┐	        bottom-left
//     6    	        ┌	        bottom-right


//  Letter	Color
//     R	 red
//     B	 blue
//     Y	 yellow
//     G	 green
//     O	 orange
//     C	 cyan
//     M	 magenta
//     m	 maroon
//     P	 purple
//     A	 gray
//     W	 white
//     g	 bright green
//     T	 tan
//     b	 dark blue
//     c	 dark cyan
//     p	 pink



//  Every cell is assigned a single color.
//  The color of every endpoint cell is known and specified.
//  Every endpoint cell has exactly one neighbor which matches its color.
//  The flow through every non-endpoint cell matches exactly one of the six direction types.
//  The neighbors of a cell specified by its direction type must match its color.
//  The neighbors of a cell not specified by its direction type must not match its color.


import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;

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

    ArrayList<ArrayList<String>> puzzles;
    HashMap<String, Integer> colors;

    static int[] DIRECTION_TYPES = new int[]{LEFT_TOP, TOP_BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT};
    static HashMap<Integer, String> DIRECTION_CHARS = new  HashMap<Integer, String>() {{
        put(LEFT_TOP,   "─");
        put(TOP_BOTTOM, "│");
        put(TOP_LEFT, "┘");
        put(TOP_RIGHT, "└");
        put(BOTTOM_LEFT, "┐");
        put(BOTTOM_RIGHT, "┌");
    }};

    static HashMap<String, Integer> ANSI_LOOKUP = new HashMap<>() {{
        put("R",101);   put("B",104);   put("Y",103);   put("G",42);
        put("O",43);    put("C",106);   put("M",105);   put("m",41);
        put("P",45);    put("A",100);   put("W",107);   put("g",102);
        put("T",47);    put("b",44);    put("c",46);    put("p",35);
    }};

    static HashMap<String,String> COLORS = new HashMap<>() {{

        put("R","#FF0000");         //     R	 red
        put("B","#0000FF");         //     B	 blue
        put("Y","#FFFF00");         //     Y	 yellow
        put("G","#008000");         //     G	 green
        put("O","#FFA500");         //     O	 orange
        put("C","#00FFFF");         //     C	 cyan
        put("M","#FF00FF");         //     M	 magenta
        put("m","#800000");         //     m	 maroon
        put("P","#800080");         //     P	 purple
        put("A","#808080");         //     A	 gray
        put("W","#FFFFFF");         //     W	 white
        put("g","#90EE90");         //     g	 bright green
        put("T","#D2B48C");         //     T	 tan
        put("b","#00008B");         //     b	 dark blue
        put("c","#008B8B");         //     c	 dark cyan
        put("p","#FF69B4");         //     p	 pink
    }};


    public void parsePuzzle(String filePosition){
        //InputStream input = getClass().getResourceAsStream("ListStopWords.txt");
        String relativeFilePosition = "puzzles/" + filePosition;
        URL relativeFileURL = getClass().getResource(relativeFilePosition);
        File gameFile = new File(relativeFileURL.getPath());
        ArrayList<String> game = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gameFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                game.add(line);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        int row = game.size();
        int col = game.get(0).length();
        HashMap<String, Integer> colors = new HashMap<>();
        for(int i = 0;i < row;i++){
            if(game.get(i).length() != col){
                System.out.println("FLOW GAME File Error");
                return;
            }
            for(int j = 0;j < col;j ++){
                char currentChar = game.get(i).charAt(j);
                if( (currentChar > 'a' && currentChar < 'z') ||(currentChar > 'A' && currentChar < 'Z')){
                    String currentStr = String.valueOf(game.get(i).charAt(j));
                    if(colors.containsKey(currentStr)){
                        int currentStrSum = colors.get(currentStr) + 1;
                        if(currentStrSum > 2){
                            System.out.println("FLOW GAME File Error");
                            return;
                        }else {
                            colors.put(currentStr, currentStrSum);
                        }
                    }else{
                        colors.put(currentStr,1);
                    }
                }
            }
        }

        this.colors = colors;
        //this.puzzles = puzzle;
        this.puzzles = new ArrayList<>();
        for(int i = 0;i < game.size();i++){
            this.puzzles.add(new ArrayList<String>());
            String curStr = game.get(i);
            for(int j = 0;j < game.get(i).length();j++){
                this.puzzles.get(i).add(Character.toString(curStr.charAt(j)));
            }
        }
    }

    public void printPuzzle(){
        if(this.puzzles.size() == 0){
            System.out.println("No Puzzle");
        }else{
            for(int i = 0 ;i < this.puzzles.size();i++){
                for(int j = 0;j < this.puzzles.get(i).size();j++){
                    System.out.printf("%4s", this.puzzles.get(i).get(j));
                }
                System.out.print("\n");
            }
        }
    }


    public void makeDirectionClauses(){
        ArrayList<String> directionClause = new ArrayList<>();
        int numColor = colors.keySet().size();
//        int puzzleSize = puzzle.size();
//
//        for(int i = 0;i < puzzle.size();i++){
//
//        }
    }
    public void makeColorClauses(){}
    public void reduceToSAT(){

        int size = this.puzzles.size();
        int colorsNum = this.colors.keySet().size();
        int cellsNum = (int)Math.pow(size,2);
        int cellsColorsNum = colorsNum * cellsNum;


        long startTime = System.nanoTime();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
    }








    static public void main(String[] args){
        FlowGame game = new FlowGame();
        game.parsePuzzle("extreme_8x8_01.txt");
        game.printPuzzle();
    }

}
