public class Graph {
    int vertices; // number of vertices
    int[][] adjacencyMatrix; // adjacenecy matrix adjacencyMatrix[i][j] means vertex i connect to vertex j
    //ArrayList<Integer> edgeList;

    public Graph(int vertices){
        this.vertices = vertices;
        this.adjacencyMatrix = new int[vertices][vertices];
        //this.edgeList = new ArrayList<>();
    }

    public void addEdge(int startPoint,int endPoint){
        this.adjacencyMatrix[startPoint][endPoint] = 1;
    }

    public void printGraph(){
        for(int i = 0;i < this.adjacencyMatrix.length;i++){
            for(int j = 0;j < this.adjacencyMatrix[0].length;j++){
                System.out.printf("%3d",this.adjacencyMatrix[i][j]);
            }
            System.out.print("\n");
        }
    }

    public static void main(String[] args){
        Graph graph = new Graph(10);
        graph.addEdge(3,5);
        graph.addEdge(5,7);
        graph.printGraph();
    }


}
