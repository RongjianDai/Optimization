package RideSharing;
import java.text.DecimalFormat;

/**
 * Created by 戴荣健 on 2018/12/8.
 */


/**
 * Define the class Model which represents the problems get to be optimized.
 */
public class Model {
    int L, N;
    //the number of AVs in the stations which denotes the total AVs can be dispatched;
    int fleetSize;
    //the available links (reachable links) for a vehicle;
    int linkUsed0[][], linkUsed1[][], linkUsed2[][];
    //the capacity of all links;
    int capacity[][], capacity2[][], capacity3[][];
    //operate costs when a vehicle uses these links;
    double cost[][], cost2[][];
    //fuel consumption when vehicles uses these links;
    double fuel[][], fuel2[][];
    /**
     * the construction function of class Model.
     * Attribute variables: fleetSize, linkUsed, capacity, cost, fuel;
     * Class methods: Model - construction method, printInformation - print the information of this model;
     * Input parameters: fleetSize - the number of AVs in the stations, ins - the instance of class Instance, c - the type of AVSnetwork;
     */
    Model(Instance ins) {
        this.L = ins.L;
        this.N = ins.N;
        this.fleetSize = N;
        linkUsed0 = ins.network(0);
        linkUsed1 = ins.network(1);
        linkUsed2 = ins.network(2);
        capacity = new int[2*N+1][2*N+1]; capacity2 = new int[2*N+2][2*N+2]; capacity3 = new int[2*N+2][2*N+2];
        fuel = new double[2*N+1][2*N+1]; fuel2 = new double[2*N+2][2*N+2];
        cost = new double[2*N+1][2*N+1]; cost2 = new double[2*N+2][2*N+2];
        //set the capacities of links;
        for (int i=0; i<2*N; i++) {
            for (int j=0; j<2*N; j++) {
                capacity[i][j] = 1;
                capacity2[i][j] = 1;
                capacity3[i][j] = 1;
            }
        }
        for (int i=0; i<2*N; i++) {
            capacity[2*N][i] =1;
            capacity[i][2*N] = 1;
            capacity2[2*N][i] =1; capacity2[i][2*N] = 1; capacity2[2*N+1][i] =1; capacity2[i][2*N+1] = 1;
            capacity3[2*N][i] =1; capacity3[i][2*N] = 1; capacity3[2*N+1][i] =1; capacity3[i][2*N+1] = 1;
        }
        capacity[2*N][2*N] = fleetSize;
        capacity2[2*N][2*N] = fleetSize; capacity2[2*N][2*N+1] = 1; capacity2[2*N+1][2*N] = 1; capacity2[2*N+1][2*N+1] = fleetSize;
        capacity3[2*N][2*N] = fleetSize; capacity3[2*N][2*N+1] = fleetSize; capacity3[2*N+1][2*N] = fleetSize; capacity3[2*N+1][2*N+1] = fleetSize;

        //set the fuel consumption of links;
        for (int i=0; i<2*N+1; i++) {
            for (int j=0; j<2*N+1; j++) {
                fuel[i][j] = 3 * ins.distance[i][j] * 0.15;  //we assume that the unit of distance is 3km and the eletric energy consumpation rate  is 15L/100km that equals 0.15L/km
                fuel2[i][j] = fuel[i][j];
            }
            fuel2[2*N+1][i] = 3 * ins.distance2[2*N+1][i] * 0.15;
            fuel2[i][2*N+1] = 3 * ins.distance2[i][2*N+1] * 0.15;
        }
        fuel[2*N][2*N] = 0;
        fuel2[2*N][2*N] = 0; fuel2[2*N+1][2*N+1] = 0;

        //set the costs of links;
        for (int i=0; i<2*N+1; i++) {
            for (int j=0; j<2*N+1; j++) {
                cost[i][j] = 7 * fuel[i][j];  //we assume that the price of fuel is 7 yuan/L;
                cost2[i][j] = 7 * fuel2[i][j];
            }
            cost2[2*N+1][i] = 7 * fuel2[2*N+1][i]; cost2[i][2*N+1] = 7 * fuel2[i][2*N+1];
        }
        //Plus profits of the services;
        double profit[][] = new double[2*N+1][2*N+1];
        for (int i=0; i<2*N+1; i++) {
            for (int j=N; j<2*N; j++) {
                if (i!=j) {
                    profit[i][j] = 5 * ins.distance[j-N][j];  //we assume that the price of service is 5yuan/km.
                }
                cost[i][j] -=  profit[i][j];
                cost2[i][j] -=  profit[i][j];
            }
        }
        //reset the cost of links(o,i+)
        for (int i=N; i<2*N; i++) {
            cost[2*N][i] = 0;
            cost2[2*N][i] = 0; cost2[2*N+1][i] = 0;
        }
        //set the cost of link(o,d)
        cost[2*N][2*N] = 0;
        cost2[2*N][2*N] = 0; cost2[2*N+1][2*N+1] = 0;

    }

    /**
     * Print the information of this model.
     * @param name - the name of information to be shown;
     * @param x - the value (int) of this attribute;
     */
    public void printInfo(String name, int[][] x) {
        System.out.println("The "+name+" of this model is: ");
        for (int i=0; i<x.length; i++) {
            for (int j=0; j<x.length; j++) {
                System.out.print(x[i][j]+" ");
            }
            System.out.println();
        }
    }

    /**
     * Print the information of this model.
     * @param name - the name of information to be shown;
     * @param x - the value (double) of this attribute;
     */
    public void printInfo(String name, double[][] x) {
        System.out.println("The "+name+" of this model is: ");
        DecimalFormat df = new DecimalFormat("0.0");
        for (int i=0; i<x.length; i++) {
            for (int j=0; j<x.length; j++) {
                System.out.print(df.format(x[i][j])+" ");
            }
            System.out.println();
        }
    }

    public static void main(String args[]) {
        int L = 10;
        int N = 10;
        Instance ins = new Instance(L,N);
        //ins.printInfo("distance", ins.distance);
        Model model = new Model(ins);
        model.printInfo("capacity", model.capacity);
        model.printInfo("capacity2", model.capacity2);
        model.printInfo("capacity3", model.capacity3);
        model.printInfo("fuel", model.fuel);
        model.printInfo("fuel2", model.fuel2);
        model.printInfo("cost", model.cost);
        model.printInfo("cost2", model.cost2);
    }

}