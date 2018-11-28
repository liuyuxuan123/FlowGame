
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


import java.nio.channels.FileLock;
import java.util.*;
import java.io.File;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.Flow;
import java.util.Arrays;

public class FlowGame{
    static int LEFT =   1 << 0;
    static int RIGHT =  1 << 1;
    static int TOP =    1 << 2;
    static int BOTTOM = 1 << 3;

    static int LEFT_RIGHT = LEFT | RIGHT;
    static int TOP_BOTTOM = TOP | BOTTOM;
    static int TOP_LEFT = TOP | LEFT;
    static int TOP_RIGHT = TOP | RIGHT;
    static int BOTTOM_LEFT = BOTTOM | LEFT;
    static int BOTTOM_RIGHT = BOTTOM | RIGHT;

    ArrayList<ArrayList<String>> puzzles;
    HashMap<String, Integer> colors;
    HashMap<ArrayList<Integer>, HashMap<Integer,Integer>> directionDict;
    ArrayList<int[]> colorClauses = new ArrayList<>();
    ArrayList<int[]> directionClauses = new ArrayList<>();


    static int[][] DELTAS =  {
            {LEFT, 0, -1},
            {RIGHT, 0, 1},
            {TOP, -1, 0},
            {BOTTOM, 1, 0}
    };

    static int[] DIRECTION_TYPES = new int[]{
            LEFT_RIGHT,                      // 3
            TOP_BOTTOM,                      // 12
            TOP_LEFT,                        // 5
            TOP_RIGHT,                       // 6
            BOTTOM_LEFT,                     // 9
            BOTTOM_RIGHT};                   // 10
    static HashMap<Integer, String> DIRECTION_CHARS = new  HashMap<Integer, String>() {{
        put(LEFT_RIGHT,   "─");
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
        HashMap<String, Integer> colors = new HashMap<>();

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
        ArrayList<Integer> colorCount = new ArrayList<>();

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
                        int currentColor = colors.get(currentStr);
                        colorCount.set(currentColor,1);
                    }else{
                        int size = colors.keySet().size();
                        colors.put(currentStr,size);
                        colorCount.add(0);
                    }
                }
            }
        }

        for(int i = 0;i < colorCount.size();i++){
            if(colorCount.get(i) != 1){
                System.out.println("Error Puzzles");
                return;
            }
        }


        this.colors = colors;
        //this.puzzles = puzzle;
        this.puzzles = new ArrayList<>();
        this.directionDict = new HashMap<>();

        for(int i = 0;i < game.size();i++){
            this.puzzles.add(new ArrayList<String>());
            String curStr = game.get(i);
            for(int j = 0;j < game.get(i).length();j++){
                this.puzzles.get(i).add(Character.toString(curStr.charAt(j)));
            }
        }
    }

    public void printPuzzle(){
        // If there is no info in this class then it will print error message
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



    public void makeDirectionVariables(int startVariable) {

        int puzzleSize = puzzles.size();
        int numberOfVariable = startVariable;

        for(int i = 0;i < puzzleSize;i++){
            for(int j = 0;j < puzzleSize;j++){

                char currentChar = this.puzzles.get(i).get(j).toCharArray()[0];
                if( (currentChar >= 'a' && currentChar <= 'z') ||(currentChar >= 'A' && currentChar <= 'Z')) {
                    continue;
                }

                int[][] neighbors = FlowGame.validNeighbors(puzzleSize,i,j);
                int[] neighborsDirBit = new int[neighbors.length];
                int cellFlags = 0;
                for(int n = 0; n < neighbors.length;n++){
                    neighborsDirBit[n] = neighbors[n][0];
                    cellFlags |= neighbors[n][0];
                }

//                int[] position = {i,j};
                ArrayList<Integer> position = new ArrayList<>();
                position.add(i);
                position.add(j);

                HashMap<Integer,Integer> dirDict = new HashMap<>();
               //System.out.printf("%5d %5d %5d \n",i,j,cellFlags);
                for(int m = 0;m < FlowGame.DIRECTION_TYPES.length;m++){
                    int code = FlowGame.DIRECTION_TYPES[m];
                    if((cellFlags & code) == code){
                        numberOfVariable += 1;
                        dirDict.put(code, numberOfVariable);
                        //System.out.printf("%5s : %4d",FlowGame.DIRECTION_CHARS.get(code),numberOfVariable);
                    }
                }
                //System.out.print("\n");
                this.directionDict.put(position,dirDict);
            }
        }

        System.out.println("numberOfVariable: " + numberOfVariable);
    }


    public static int colorVar(int i,int j,int color,int puzzleSize,int colorNum){
        return (i * puzzleSize + j) * colorNum + color + 1;
    }

    public void makeDirectionClauses(){

        int numColors = colors.keySet().size();
        int puzzleSize = puzzles.size();

        for(int i = 0;i < puzzleSize;i++){
            for(int j = 0;j < puzzleSize;j++) {

                char currentChar = this.puzzles.get(i).get(j).toCharArray()[0];
                if( (currentChar >= 'a' && currentChar <= 'z') ||(currentChar >= 'A' && currentChar <= 'Z')) {
                    continue;
                }


                ArrayList<Integer> position = new ArrayList<>();
                position.add(i);
                position.add(j);
                HashMap<Integer,Integer> cellDirectionDict = this.directionDict.get(position);
                System.out.println("Position: [" + i + ", " + j + "] -> " + cellDirectionDict);
                int[] cellDirectionVars = new int[cellDirectionDict.keySet().size()];
                int n = 0;
                for(int dir : FlowGame.DIRECTION_TYPES){
                    if(cellDirectionDict.containsKey(dir)) {
                        cellDirectionVars[n] = cellDirectionDict.get(dir);
                        n++;
                    }
                }
                // Can only have one direction
                directionClauses.add(cellDirectionVars);

                // no two neighbors have this color
                int[][] noTwoSameDirectionClauses = FlowGame.noTwoPairs(cellDirectionVars);
                for(int k = 0;k < noTwoSameDirectionClauses.length;k++){
                    if(!directionClauses.contains(noTwoSameDirectionClauses[k])){
                        directionClauses.add(noTwoSameDirectionClauses[k]);
                    }
                }

                for(int m = 0; m < numColors;m ++){
                    // Get Color Variable for this Cell
                    int color_1 = FlowGame.colorVar(i,j,m,puzzleSize,numColors);

                    // for each neighbor
                    int[][] neighbors = FlowGame.allNeighbors(puzzleSize,i,j);
                    for(int k = 0;k < neighbors.length;k++){
                        int direction = neighbors[k][0];
                        int currI = neighbors[k][1];
                        int currJ = neighbors[k][2];
                        // Get Color Variable for Other Cell
                        int color_2 = FlowGame.colorVar(currI,currJ,m,puzzleSize,numColors);

                        for ( Integer directionType : cellDirectionDict.keySet() ) {
                            Integer directionVariable = cellDirectionDict.get(directionType);

                            // if neighbor is hit by this direction type
                            if((directionType & direction) > 0){
                                // this direction type implies the colors are equal
                                // If dir_var is true, then color1 must equal to color2
                                int[] pair1 = {-directionVariable,-color_1,color_2};
                                int[] pair2 = {-directionVariable,color_1,-color_2};
                                directionClauses.add(pair1);
                                directionClauses.add(pair2);
                            }else if(FlowGame.validPosition(puzzleSize,currI,currJ)){
                                // neighbor is not along this direction type,
                                // so this direction type implies the colors are not equal
                                int[] pair3 = {-directionVariable,-color_1,-color_2};
                                directionClauses.add(pair3);
                            }
                        }
                    }
                }
            }
        }

    }

    public void makeColorClauses(){
        int numColors = colors.keySet().size();
        int puzzleSize = puzzles.size();
        ArrayList<String> colorsSet = new ArrayList<>(colors.keySet());


        for(int i = 0;i < puzzleSize;i++){
            for(int j = 0;j < puzzleSize;j ++){

                char currentChar = this.puzzles.get(i).get(j).toCharArray()[0];
                if( (currentChar >= 'a' && currentChar <= 'z') ||(currentChar >= 'A' && currentChar <= 'Z')){
                    int endPointColor = this.colors.get(Character.toString(currentChar));
                    int[] colorClause = {FlowGame.colorVar(i,j,endPointColor,puzzleSize,numColors)};
                    // color in this cell is this one
                    colorClauses.add(colorClause);

                    // color in this cell is not the other ones
                    for(int n = 0;n < numColors;n++){
                        if(n != endPointColor){
                            int[] otherColorClause = {-FlowGame.colorVar(i,j,n,puzzleSize,numColors)};
                            colorClauses.add(otherColorClause);
                        }
                    }

                    // gather neighbors' variables for this color
                    int[][] neighbors = FlowGame.validNeighbors(puzzleSize,i,j);
                    int[] neighborColorClause = new int[neighbors.length];
                    //one neighbor has this color
                    for(int n = 0;n < neighbors.length;n++){

                        int currI = neighbors[n][1];
                        int currJ = neighbors[n][2];
                        neighborColorClause[n] = FlowGame.colorVar(currI,currJ,endPointColor,puzzleSize,numColors);
                    }
                    colorClauses.add(neighborColorClause);

                    // no two neighbors have this color
                    int[][] noTwoSameColorClauses = FlowGame.noTwoPairs(neighborColorClause);
                    for(int k = 0;k < noTwoSameColorClauses.length;k++){
                        if(!colorClauses.contains(noTwoSameColorClauses[k])){
                            colorClauses.add(noTwoSameColorClauses[k]);
                        }
                    }

                }else{

                    // color in this cell is not the other ones
                    int[] possibleColorClause = new int[numColors];
                    for(int n = 0;n < numColors;n++){
                        int otherColorClause = FlowGame.colorVar(i,j,n,puzzleSize,numColors);
                        //colorClauses.add(otherColorClause);
                        possibleColorClause[n] = otherColorClause;
                    }
                    colorClauses.add(possibleColorClause);

                    int[][] noTwoSameColorClauses = FlowGame.noTwoPairs(possibleColorClause);
                    for(int k = 0;k < noTwoSameColorClauses.length;k++){
                        if(!colorClauses.contains(noTwoSameColorClauses[k])){
                            colorClauses.add(noTwoSameColorClauses[k]);
                        }
                    }
                }
            }
        }

        for(int k = 0;k < colorClauses.size();k++){
            for(int g = 0;g < colorClauses.get(k).length;g++){
                System.out.printf("%5d",colorClauses.get(k)[g]);
            }
            System.out.print("\n");
        }
        System.out.println("Size: " + colorClauses.size());

    }
    public void reduceToSAT(){

        int size = this.puzzles.size();
        int colorsNum = this.colors.keySet().size();
        int cellsNum = (int)Math.pow(size,2);
        int cellsColorsNum = colorsNum * cellsNum;


        long startTime = System.nanoTime();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
    }


    public void solveSAT(){

    }



    // Return all combinations of two items from a collection, useful for
    //  making a large number of SAT variables mutually exclusive.
    static public int[][] allPairs(int[] collection){
        int size = collection.length;
        int pairsSize = size * (size - 1) / 2;
        int[][] res = new int[pairsSize][2];
        ArrayList<ArrayList<Integer>> val = FlowGame.combine(collection,2);
        for(int i = 0;i < val.size();i++){
            for(int j = 0;j < val.get(i).size();j++){
                res[i][j] = val.get(i).get(j);
            }
        }
        return res;
    }

    static public ArrayList<ArrayList<Integer>> combine(int[] n, int k) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        ArrayList<Integer> item = new ArrayList<Integer>();
        dfs(n, k, 0, item, result); // because it need to begin from 1

        return result;
    }

    static private void dfs(int[] n, int k, int start, ArrayList<Integer> item,
                     ArrayList<ArrayList<Integer>> res) {
        if (item.size() == k) {
            res.add(new ArrayList<Integer>(item));
            return;
        }

        for (int i = start; i < n.length; i++) {
            item.add(n[i]);
            dfs(n, k, i + 1, item, res);
            item.remove(item.size() - 1);
        }
    }


    // Given a collection of SAT variables, generates clauses specifying
    //  that no two of them can be true at the same time.
    static public int[][] noTwoPairs(int[] collection){
        int[][] pairs = FlowGame.allPairs(collection);
        for(int i = 0;i < pairs.length;i++){
            for(int j = 0;j < pairs[i].length;j++){
                pairs[i][j] = -pairs[i][j];
            }
        }
        return pairs;
    }





    // Check whether a position on a square grid is valid.
    static public int[][]  validNeighbors(int puzzleSize,int i,int j){
        int[][] allNeighbors = FlowGame.allNeighbors(puzzleSize,i,j);
        ArrayList<int[]> res = new ArrayList<>();

        for (int n = 0; n < allNeighbors.length;n++){
            int[] currState = allNeighbors[n];
            int currI = currState[1];
            int currJ = currState[2];
            if( FlowGame.validPosition(puzzleSize,currI,currJ)){
                res.add(currState);
            }
        }

        int size = res.size();
        int[][] resArr = new int[size][3];
        for(int n = 0;n < size;n++){
            resArr[n] = res.get(n);
        }
        return resArr;
    }



    // Check Whether a Position on a Square Grid is Valid.
    static public boolean validPosition(int puzzleSize,int i,int j){
        return i >= 0 && i < puzzleSize && j >= 0 && j < puzzleSize;
    }

    // Return All Neighbors of a Grid Square at Row i, Column j
    static public int[][] allNeighbors(int puzzleSize,int i,int j){
        int[][] res = new int[4][3];
        for(int n = 0;n < FlowGame.DELTAS.length;n++){
            int deltaX = FlowGame.DELTAS[n][1];
            int deltaY = FlowGame.DELTAS[n][2];
            int direction = FlowGame.DELTAS[n][0];

            res[n][0] = direction;
            res[n][1] = i + deltaX;
            res[n][2] = j + deltaY;
        }
        return res;
    }



    static public void main(String[] args) {
        FlowGame game = new FlowGame();
        game.parsePuzzle("extreme_8x8_01.txt");
        game.printPuzzle();
        //game.makeColorClauses();
        game.makeDirectionVariables(320);

        for(ArrayList<Integer> key: game.directionDict.keySet()){
            System.out.println(key + "  :  " +game.directionDict.get(key));
        }
        System.out.println("Size: " + game.directionDict.keySet().size());

        game.makeDirectionClauses();
        for(int i = 0;i < game.directionClauses.size();i++){
            for(int j = 0;j < game.directionClauses.get(i).length;j ++){
                System.out.printf("%4d",game.directionClauses.get(i)[j]);
            }
            System.out.print("\n");
        }
    }

}
