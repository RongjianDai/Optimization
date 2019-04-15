package RideSharing;

/**
 * Created by 戴荣健 on 2018/12/4.
 */

/**
定义TripDemand类
参数：L，生成OD所用的网格规模 L*L; N, 生成需求的个数
方法：生成OD的起始点
 */
class TripDemand {
    int L, N;
    //定义二维数组origination[i][j]、destination[i][j]存储起始点坐标,i为第i个需求，j=0为横坐标，j=1为纵坐标；
    int origination[][];
    int destination[][];
    /*
    定义 N*7 数组demand[i][j]存储需求，i为需求编号，j=0为起点横坐标，j=1为起点纵坐标，j=2位终点横坐标, j=3为终点纵坐标,
     j=4为出发时间, j=5为最早到达时间, j=6为最晚到达时间;
     */
    int demand[][];

    /**
     * the construction method of class TripDemand. This method can generate the trip demand including the start point, end point, start time, and minimal, maximal end time.
     * @param L - the size of network where trip demands are generated;
     * @param N - the number of trip demands;
     */
    TripDemand (int L,int N){
        this.L = L;
        this.N = N;
        this.origination = new int[N][2];
        this.destination = new int[N][2];
        this.demand = new int[N][7];
        //生成N个OD的起始点位置坐标
        for (int i=0; i <N; i++) {
            origination[i][0] = (int)(Math.random() * L);
            origination[i][1] = (int)(Math.random() * L);
            destination[i][0] = (int)(Math.random() * L);
            destination[i][1] = (int)(Math.random() * L);
            while (origination[i][0] == destination[i][0] && origination[i][1] == destination[i][1]) {
                destination[i][0] = (int)(Math.random() * L);
                destination[i][1] = (int)(Math.random() * L);
            }
        }
        //生成完整的需求矩阵demand[][];
        for (int i=0; i<N; i++) {
            //定义需求出发时间，到达时间
            double startTime, minEndTime, maxEndTime, travelTime;
            /*
             随机生成出发时间，并计算最早到达时间和最晚到达时间，其中假设网格单位长度所需时间为5min，在[0,240)min内生成出发时间t1,
             最早到达时间为 t1+5*d ,设最晚到达时间为 t1 + 1.5*5*d;
             */
            startTime = Math.random() * 240;
            travelTime = 5 * (Math.abs(origination[i][0] - destination[i][0]) + Math.abs(origination[i][1] - destination[i][1])) + 3;
            minEndTime = startTime + travelTime;
            maxEndTime = minEndTime + 0.5 * travelTime;
            //得到需求矩阵
            demand[i][0] = origination[i][0];
            demand[i][1] = origination[i][1];
            demand[i][2] = destination[i][0];
            demand[i][3] = destination[i][1];
            demand[i][4] = (int)startTime;
            demand[i][5] = (int)minEndTime;
            demand[i][6] = (int)maxEndTime;
        }
    }
}
/**
Define the class Stations, which represents the departure point and destination point.
 */
class Stations {
    //define symbols for the abscissas and ordinates of the stations;
    int x, y;

    /**
     * The construction method of class Stations.
     * @param x - the abscissa of the station;
     * @param y - the ordinate of the station;
     */
    Stations (double x, double y) {
        this.x = (int)x;
        this.y = (int)y;
    }
}

/**
 Define the class Instance which represent all the information of the trip demand set.
 Parameters: L - the size of network where the trip demands are generated, N - the number of trip demands;
 */
public class Instance {
    //定义网络规模变量 L ,需求个数变量 N 和需求数组demand[N][7]；
    public  int L, N;
    public Stations station1, station2;
    public  int demand[][];
    //define the matrix of distance of links(i,j);
    public int distance[][];
    public int distance2[][];
    //define the matrix of travel time of links(i,j);
    public int travelTime[][];
    public int travelTime2[][];
    //define the variable of fuel consumption in traditional environment
    double fuel, cost;

    /**
     * The construction method of class Instance.
     * @param L - the size of network where trip demands are generated;
     * @param N - the number of trip demands;
     */
    public Instance (int L, int N) {
        this.L = L;
        this.N = N;
        //在 L*L 的网格中生成 N 个OD的起始点坐标对象 OD；
        TripDemand OD = new TripDemand(L,N);
        //set the location of stations;
        station1 = new Stations(0.25*L,0.25*L);
        station2 = new Stations(0.75*L,0.75*L);
        this.demand = OD.demand;

        //set the matrix of distance;
        distance = new int[2*N+1][2*N+1];
        distance2 = new int[2*N+2][2*N+2];
        // 0<=i<N;
        for (int i=0; i<N; i++) {
            // 0<=i<N, 0<=j<N;
            for (int j=0; j<N; j++) {
                distance[i][j] = Math.abs(demand[i][0] - demand[j][0]) + Math.abs(demand[i][1] - demand[j][1]);
                distance2[i][j] = distance[i][j];
            }
            // 0<=i<N, N<=j<2N;
            for (int j=N; j<2*N; j++) {
                distance[i][j] = Math.abs(demand[i][0] - demand[j-N][2]) + Math.abs(demand[i][1] - demand[j-N][3]);
                distance2[i][j] = distance[i][j];
            }
        }
        // N<=i<2N;
        for (int i=N; i<2*N; i++) {
            // N<=i<2N, 0<=j<N;
            for (int j=0; j<N; j++) {
                distance[i][j] = Math.abs(demand[i-N][2] - demand[j][0]) + Math.abs(demand[i-N][3] - demand[j][1]);
                distance2[i][j] = distance[i][j];
            }
            // N<=i<2N, N<=j<2N;
            for (int j=N; j<2*N; j++) {
                distance[i][j] = Math.abs(demand[i-N][2] - demand[j-N][2]) + Math.abs(demand[i-N][3] - demand[j-N][3]);
                distance2[i][j] = distance[i][j];
            }
        }
        //reset the unreachable links(i+, i-);
        for (int i=0; i<N; i++) {
            distance[N+i][i] = 0;
            distance2[N+i][i] = 0;
        }
        //set the distance of links(o,i) and links(j,d);
        for (int i=0; i<N; i++) {
            // for the links(o,i);
            distance[2*N][i] = Math.abs(station1.x - demand[i][0]) + Math.abs(station1.y - demand[i][1]);
            // for the links(s1, i) of distance2
            distance2[2*N][i] = distance[2*N][i];
            // for the links(s2, i) of distance2
            distance2[2*N+1][i] = Math.abs(station2.x - demand[i][0]) + Math.abs(station2.y - demand[i][1]);
            // for the links(j,d);
            distance[i+N][2*N] = Math.abs(station2.x - demand[i][2]) + Math.abs(station2.y - demand[i][3]);
            // for the links(j,s2) of distance2
            distance2[i+N][2*N] = distance[i+N][2*N];
            // for the links(j,s1) of distance2
            distance2[i+N][2*N+1] = Math.abs(station1.x - demand[i][2]) + Math.abs(station1.y - demand[i][3]);
            //for the unreachable links;
            distance[2*N][i+N] = 0;
            distance[i][2*N] = 0;
            distance2[2*N][i+N] = 0; distance2[2*N+1][i+N] = 0;
            distance2[i][2*N] = 0; distance2[i][2*N+1] = 0;
        }
        //set the distance of the link(o,d);
        distance[2*N][2*N] = Math.abs(station1.x - station2.x) + Math.abs(station1.y - station2.y);
        //set the distance of links(s1,s2) and (s2,s1)
        distance2[2*N][2*N] = distance[2*N][2*N];
        distance2[2*N+1][2*N+1] = Math.abs(station1.x - station2.x) + Math.abs(station1.y - station2.y);

        //calculate the travel time of all the reachable links;
        travelTime = new int[2*N+1][2*N+1];
        travelTime2 = new int[2*N+2][2*N+2];
        for (int i=0; i<2*N+1; i++) {
            for (int j=0; j<2*N+1; j++) {
                //The velocity of vehicle is set 36 km/h, and the unit of distance equals to 3 km, the buffer time is set to 3 minutes
                travelTime[i][j] = 5 * distance[i][j] + 3;
                travelTime2[i][j] = travelTime[i][j] + 3;
            }
        }
        for (int i=0; i<2*N+2; i++) {
            travelTime2[2*N+1][i] = 5 * distance2[2*N+1][i] + 3;
            travelTime2[i][2*N+1] = 5 * distance2[i][2*N+1] + 3;
        }
    }

    /**
    The method to determine the value of AVSnetwork[][], set the value of AVSnetwork[i][j] = 1 if the link(i,j) is reachable;
    Method name: network;
    Input parameters: c - the index of AVSnetwork types, c=0 if the condition is relax, c=1 if the condition is strict;
    Output parameters: the AVSnetwork;
     */
    public int[][] network (int c) {
        //定义AVSnetwork[i][j]为网络矩阵，若 AVSnetwork[i][j]=1 则代表link(i,j)为reachable，否则为unreachable；
        int AVSnetwork[][] = new int[2*N+1][2*N+1];;
        //set the AVSnetwork according to the time constraints of reachable links;
        //firstly, set these links （O,i-）and （i+,D）without the time constraints;
        for (int i=0; i<N; i++) {
            //set the value 1 for links（O,i-）which are reachable links;
            AVSnetwork[2*N][i] = 1;
            //set the value 1 for links（i+,D）which are reachable links;
            AVSnetwork[i+N][2*N] = 1;
        }
        //secondly, set the link(o,d);
        AVSnetwork[2*N][2*N] = 1;
        // we just need set the relocation links if c=2 which represents the case without ride-sharing;
        if (c==2) {
            //set the value of relocation links (i+,j-);
            for (int i=N; i<2*N; i++) {
                for (int j=0; j<N; j++) {
                    if (i-N!=j) {
                        if (travelTime[i][j] <= demand[j][4] - demand[i-N][5]) { AVSnetwork[i][j] = 1; }
                    }
                }
            }
            //set the value of service links (i+,i-)
            for (int i=0; i<N; i++) {
                AVSnetwork[i][i+N] = 1;
            }
        }
        // set the pickup links and delivery links with ride-sharing;
        else {
            //select reachable links from the links with the index of i belonging to [0,N);
            for (int i=0; i<N; i++) {
                //for the pickup links representing links (i-,j-);
                for (int j=0; j<N; j++) {
                    if (j!=i) {
                        if (travelTime[i][j] <= demand[j][4] - demand[i][4]) {
                            AVSnetwork[i][j] = 1;
                        }
                    }
                }
                //for the service links and delivery links representing links (i-,j+);
                for (int j=N; j<2*N; j++) {
                    if (j-N==i) {
                        AVSnetwork[i][j] = 1;
                    }
                    else {
                        if (travelTime[i][j] <= demand[j-N][6] - demand[i][4]) {
                            AVSnetwork[i][j] = 1;
                        }
                    }
                }
            }
            //select reachable links from the links with the index of i belonging to [N,2N);
            for (int i=N; i<2*N; i++) {
                //the case with the relax condition;
                if (c==0) {
                    //for the pickup links representing links (i+,j-);
                    for (int j=0; j<N; j++) {
                        if (i-N!=j) {
                            if (travelTime[i][j] <= demand[j][4] - demand[i-N][5]) {
                                AVSnetwork[i][j] = 1;
                            }
                        }
                    }
                    //for the delivery links representing links (i+,j+);
                    for (int j=N; j<2*N; j++) {
                        if (i!=j) {
                            if (travelTime[i][j] <= demand[j-N][6] - demand[i-N][5]) {
                                AVSnetwork[i][j] = 1;
                            }
                        }
                    }
                }
                //the case with the strict condition;
                if (c==1) {
                    //for the pickup links representing links (i+,j-);
                    for (int j=0; j<N; j++) {
                        if (i-N!=j) {
                            if (travelTime[i][j] <= demand[j][4] - demand[i-N][6]) {
                                AVSnetwork[i][j] = 1;
                            }
                        }
                    }
                    //for the delivery links representing links (i+,j+);
                    for (int j=N; j<2*N; j++) {
                        if (i!=j) {
                            if (travelTime[i][j] <= demand[j-N][6] - demand[i-N][6]) {
                                AVSnetwork[i][j] = 1;
                            }
                        }
                    }
                }
            }
        }

        return AVSnetwork;
    }

    /**
    The method of showing the information of one instance;
    method name: print;
    Input parameters: void;
    Output parameters: void;
     */
    public void printDemand () {
        System.out.println("Station1: "+station1.x+", "+station1.y+"  Station2: "+station2.x+", "+station2.y);
        System.out.println("The trip demands are showed as following:");
        //print the instance of demand set;
        for (int i=0; i<N; i++) {
            System.out.print("Trip "+i+" : ");
            System.out.print("O_x: "+demand[i][0]);
            System.out.print("  O_y: "+demand[i][1]);
            System.out.print("  D_x: "+demand[i][2]);
            System.out.print("  D_y: "+demand[i][3]);
            System.out.print("  startTime: "+demand[i][4]);
            System.out.print("  minEndTime: "+demand[i][5]);
            System.out.println("  maxEndTime: "+demand[i][6]);
        }
    }
    /**
    The method of showing the travel time of one instance;
    method name: printTravelTime;
    Input parameters: void;
    Output parameters: void;
     */
    public void printInfo (String name, int[][] x) {
        System.out.println("The "+name+" of links:");
        for (int i=0; i<x.length; i++) {
            for (int j=0; j<x.length; j++) {
                System.out.print(x[i][j]+ " ");
            }
            System.out.println();
        }
    }

    /**
    The method of showing the available network of one instance;
    method name: printNetwork;
    Input parameters: int c - the type of network, int[][] - the network of one instance;
    Output parameters: void;
     */
    public void printNetwork (int c, int[][] network) {
        //print the title of this network;
        if (c==0) {
            System.out.println("The AVSnetwork with relax time constraints is::");
        } else if (c==1) {
            System.out.println("The AVSnetwork with strict time constraints is::");
        } else {
            System.out.println("The AVSnetwork without considering ride-sharing is::");
        }
        for (int i=0; i<network.length; i++) {
            for (int j=0; j<network.length; j++) {
                System.out.print(network[i][j]+ " ");
            }
            System.out.println();
        }
    }

    /**
     * The method for calculation of fuel consumption in traditional environment
     */
    public void traditionalFuel() {
        for (int i=0; i<N; i++) {
            fuel += 3 * distance[2*N][i] * 0.15;
            fuel += 3 * distance[i+N][2*N] * 0.15;
            fuel += 3 * distance[i][i+N] * 0.15;
        }
        System.out.println("The fuel consumption in traditional environment is: "+fuel);
    }

    public void traditionalCost() {
        cost = 7 * fuel;
        for (int i=0; i<N; i++) {
             cost -= 5 * distance[i][i+N];
        }
        System.out.println("The total cost in traditional environment is: "+cost);
    }

    public static void main(String args[]) {
        int L = 10;
        int N = 10;
        Instance ins = new Instance( L,N);
        ins.printDemand();
        ins.printInfo("distance", ins.distance);
        ins.printInfo("distance2", ins.distance2);
        ins.printInfo("travel time", ins.travelTime);
        ins.printInfo("travel time2", ins.travelTime2);
        int AVSnetwork0[][] = ins.network(0);
        int AVSnetwork1[][] = ins.network(1);
        int networkWORS[][] = ins.network(2);
        ins.printNetwork(0, AVSnetwork0);
        ins.printNetwork(1, AVSnetwork1);
        ins.printNetwork(2, networkWORS);

    }
}