package RideSharing;
import java.text.DecimalFormat;
import java.util.Scanner;
import gurobi.*;

/**
 * Created by 戴荣健 on 2018/12/6.
 */


public class Optimization {
    /**
     *Define the class of Optimization
     * Attribute variables: L, N, fleetSize, size, c, Case , grbModel, optimalResult, vehicleUsed, linkUsed;
     * Methods: Optimization (int c, int Case, Model model), optimize (boolean serveAll, Model model), printSolution();
     */
    int L, N, fleetSize, size, c, Case;
    GRBModel grbModel;
    double optimalResult;
    int vehicleUsed[][][];
    int linkUsed[][];

    /**
     * The construction method of class Optimization.
     * @param model - the model needed to be optimize;
     * @param c - the type of AVSnetwork,
     *          c=0: the network with relax time constraints,
     *          c=1: the network with the strict time constraints,
     *          c=2: the network without considering ride-sharing;
     * @param Case - the index of cases,
     *             Case=0: All of AVs departure from a station and arrive another station,
     *             Case=1: AVs can departure from any one of two stations and arrive to the other one,
     *             Case=2: AVs can departure from any one of two stations and arrive to any one of them.
     */
    Optimization (int c, int Case, Model model) {
        this.L = model.L;
        this.N = model.N;
        this.fleetSize = model.fleetSize;
        this.c = c;
        this.Case = Case;
        if (Case == 1) { size = 2*N+1; }
        else { size = 2*N+2; }

    }

    /**
     * The optimize method.
     * @param serveAll - Whether all of these trip demands are served, yes if serveAll = true, no if serveAll = false,
     * @param object - The object of the optimization, the object is total cost if object = 0, the object is total fuel consumption if object = 1;
     * @param model - The model get to be optimized.
     */
    public void optimize (boolean serveAll, int object, Model model) {
        try {
            //define the environment of gurobi;
            GRBEnv env = new GRBEnv();
            //Instantiation of GRBModel;
            GRBModel grbModel = new GRBModel(env);
            //Set the name of this problem
            grbModel.set(GRB.StringAttr.ModelName, "AVS-Optimize");

            //Define variables for optimization
            int availableSpace[][] = new int[2*N+1][2*N+1];
            if (c==0) { availableSpace = model.linkUsed0; } //with the relax time constraint
            if (c==1) { availableSpace = model.linkUsed1; } //with the strict time constraint
            if (c==2) { availableSpace = model.linkUsed2; } //without ride sharing
            int availableSpace2[][] = new int [2*N+2][2*N+2];
            int availableSpace3[][] = new int [2*N+2][2*N+2];
            for (int i=0; i<2*N+1; i++) {
                for (int j=0; j<2*N+1; j++) {
                    availableSpace2[i][j] = availableSpace[i][j];
                    availableSpace3[i][j] = availableSpace[i][j];
                }
            }
            if (Case == 2) {
                for (int i=0; i<N; i++) { availableSpace2[2*N+1][i] = 1; availableSpace2[i+N][2*N+1] = 1; }
                availableSpace2[2*N+1][2*N+1] = 1;
            }
            if (Case == 3) {
                for (int i=0; i<N; i++) { availableSpace3[2*N+1][i] = 1; availableSpace3[i+N][2*N+1] = 1; }
                availableSpace3[2*N+1][2*N] = 1; availableSpace3[2*N][2*N+1] = 1; availableSpace3[2*N+1][2*N+1] = 1;
            }

            //Add variables to the model
            GRBVar y[][][] = new GRBVar[N][size][size];
            for (int n=0; n<N; n++) {
                for (int i=0; i<size; i++) {
                    for (int j=0; j<size; j++) {
                        y[n][i][j] = grbModel.addVar(0, 1, 0.0, GRB.BINARY, "X"+i+","+j+","+n);
                    }
                }
            }
            GRBVar x[][] = new GRBVar[size][size];
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    x[i][j] = grbModel.addVar(0, fleetSize, 0.0, GRB.INTEGER, "X"+i+","+j);
                }
            }

            //The object of this model
            GRBQuadExpr obj = new GRBQuadExpr();
            if (object == 0) {
                for (int i=0; i<size; i++) {
                    for (int j=0; j<size; j++) {
                        if (Case == 1) { obj.addTerm(model.cost[i][j], x[i][j]); }
                        else { obj.addTerm(model.cost2[i][j], x[i][j]); }
                    }
                }
            } else {
                for (int i=0; i<size; i++) {
                    for (int j=0; j<size; j++) {
                        if (Case == 1) {obj.addTerm(model.fuel[i][j], x[i][j]);}
                        else {obj.addTerm(model.fuel2[i][j], x[i][j]);}
                    }
                }
            }
            grbModel.setObjective(obj, GRB.MINIMIZE);

            //Constraints
            //The relationship between decision variables x and y
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    GRBLinExpr relationConstr = new GRBLinExpr();
                    for (int n=0; n<N; n++) {
                        relationConstr.addTerm(1, y[n][i][j]);
                    }
                    relationConstr.addTerm(-1, x[i][j]);
                    grbModel.addConstr(relationConstr, GRB.EQUAL, 0, "Flow constraint");
                }
            }
            //Service times constraints
            for (int i=0; i<N; i++) {
                GRBLinExpr serveTimes1 = new GRBLinExpr();
                for (int j=0; j<size; j++) {
                    serveTimes1.addTerm(1, x[j][i]);
                }
                grbModel.addConstr(serveTimes1, GRB.LESS_EQUAL, 1, "Service times constraints");
            }
            for (int i=N; i<2*N; i++) {
                GRBLinExpr serveTimes2 = new GRBLinExpr();
                for (int j=0; j<size; j++) {
                    serveTimes2.addTerm(1, x[j][i]);
                }
                grbModel.addConstr(serveTimes2, GRB.LESS_EQUAL, 1, "Service times constraints");
            }
            //Services constraints(if it is needed)
            if (serveAll) {
                GRBLinExpr serveices1 = new GRBLinExpr();
                for (int i=0; i<N; i++) {
                    for (int j=0; j<size; j++) {
                        serveices1.addTerm(1, x[j][i]);
                    }
                }
                grbModel.addConstr(serveices1, GRB.EQUAL, N, "Services constraints");
                GRBLinExpr services2 = new GRBLinExpr();
                for (int i=N; i<2*N; i++) {
                    for (int j=0; j<size; j++) {
                        services2.addTerm(1, x[j][i]);
                    }
                }
                grbModel.addConstr(services2, GRB.EQUAL, N, "Services constraints");
            }

            //Add the additional constraints if the problem considering ride sharing with c=0 or c==1
            if (c==0 || c==1) {
                //Ride balance constraints
                for (int n=0; n<N; n++) {
                    for (int i=0; i<N; i++) {
                        GRBLinExpr rideBalance = new GRBLinExpr();
                        for (int j=0; j<size; j++) {
                            rideBalance.addTerm(1, y[n][j][i]);
                            rideBalance.addTerm(-1, y[n][j][i+N]);
                        }
                        grbModel.addConstr(rideBalance, GRB.EQUAL, 0, "Ride balance of vehicles");
                    }
                }
                //Departure place of vehicles
                if (Case != 1) {
                    for (int n=0; n<N; n++) {
                        GRBLinExpr departurePlace = new GRBLinExpr();
                        for (int i=0; i<2*N; i++) {
                            for (int j=0; j<2*N; j++) {
                                departurePlace.addTerm(1, y[n][i][j]);
                            }
                        }
                        departurePlace.addTerm(GRB.INFINITY, y[n][2*N][2*N]);
                        departurePlace.addTerm(GRB.INFINITY, y[n][2*N][2*N+1]);
                        departurePlace.addTerm(GRB.INFINITY, y[n][2*N+1][2*N]);
                        departurePlace.addTerm(GRB.INFINITY, y[n][2*N+1][2*N+1]);
                        grbModel.addConstr(departurePlace, GRB.LESS_EQUAL, GRB.INFINITY, "Departure place constraints");
                    }
                }
                //Interconnection constraints
                for (int n=0; n<N; n++) {
                    for (int i=0; i<2*N; i++) {
                        for (int j=0; j<2*N; j++) {
                            //For two points
                            GRBLinExpr interconnection2 = new GRBLinExpr();
                            interconnection2.addTerm(1, y[n][i][j]);
                            interconnection2.addTerm(1, y[n][j][i]);
                            grbModel.addConstr(interconnection2, GRB.LESS_EQUAL, 1, "Interconnection constraints");
                            //For three points
                            for (int k=0; k<2*N; k++) {
                                GRBLinExpr interconnection3 = new GRBLinExpr();
                                interconnection3.addTerm(1, y[n][i][j]);
                                interconnection3.addTerm(1, y[n][j][k]);
                                interconnection3.addTerm(1, y[n][k][i]);
                                grbModel.addConstr(interconnection3, GRB.LESS_EQUAL, 2, "Interconnection constraints");
                            }
                        }
                    }
                }
                //Vehicle capacity constraints
                for (int n=0; n<N; n++) {
                    GRBQuadExpr vehCapacity = new GRBQuadExpr();
                    for (int i=0; i<N; i++) {
                        for (int j=0; j<size; j++) {
                            vehCapacity.addTerm(1, y[n][j][i]);
                        }
                        for (int j=N; j<2*N; j++) {
                            vehCapacity.addTerm(-1, y[n][i][j]);
                        }
                    }
                    grbModel.addQConstr(vehCapacity, GRB.LESS_EQUAL, 3, "Vehicle capacity constraints");
                }
            }
            //Flow balance constraints
            for (int n=0; n<N; n++) {
                for (int i=0; i<2*N; i++) {
                    GRBLinExpr AVflowBalance = new GRBLinExpr();
                    for (int j=0; j<size; j++) {
                        AVflowBalance.addTerm(1, y[n][j][i]);
                        AVflowBalance.addTerm(-1, y[n][i][j]);
                    }
                    grbModel.addConstr(AVflowBalance, GRB.EQUAL, 0, "Flow balance constraints");
                }
            }
            //Capacity constraint
            for (int i=0; i<size; i++) {
                for (int j = 0; j <size; j++) {
                    GRBLinExpr capacityConstr = new GRBLinExpr();
                    capacityConstr.addTerm(1, x[i][j]);
                    if (Case == 1) { grbModel.addConstr(capacityConstr, GRB.LESS_EQUAL, model.capacity[i][j], "Capacity constraint"); }
                    if (Case == 2) { grbModel.addConstr(capacityConstr, GRB.LESS_EQUAL, model.capacity2[i][j], "Capacity constraint"); }
                    if (Case == 3) { grbModel.addConstr(capacityConstr, GRB.LESS_EQUAL, model.capacity3[i][j], "Capacity constraint"); }
                }
            }

            if (Case == 1) {
                //Search space constraints
                for (int n=0; n<N; n++) {
                    for (int i=0; i<size; i++) {
                        for (int j = 0; j <size; j++) {
                            GRBLinExpr searchSpace = new GRBLinExpr();
                            searchSpace.addTerm(1, y[n][i][j]);
                            grbModel.addConstr(searchSpace, GRB.LESS_EQUAL, availableSpace[i][j], "Search space constraint");
                        }
                    }
                }
                //Dispatch times constraint
                for (int n=0; n<N; n++) {
                    GRBLinExpr dispatchConstr = new GRBLinExpr();
                    for (int j=0; j<size; j++) {
                        dispatchConstr.addTerm(1, y[n][2*N][j]);
                    }
                    grbModel.addConstr(dispatchConstr, GRB.LESS_EQUAL, 1, "Dispatch times constraint");
                }
                //Collection times constraint
                for (int n=0; n<N; n++) {
                    GRBQuadExpr collectionConstr = new GRBQuadExpr();
                    for (int i=N; i<size; i++) {
                        collectionConstr.addTerm(1, y[n][i][2*N]);
                    }
                    grbModel.addQConstr(collectionConstr, GRB.LESS_EQUAL, 1, "Collection times constraint");
                }
                //Flow balance
                //Dispatch links
                GRBLinExpr dispatchLinks = new GRBLinExpr();
                for (int i=0; i<size; i++) {
                    dispatchLinks.addTerm(1, x[2*N][i]);
                }
                grbModel.addConstr(dispatchLinks, GRB.EQUAL, fleetSize, "Flow constraint of dispatch links");
                //Collection links
                GRBLinExpr collectionLinks = new GRBLinExpr();
                for (int i=0; i<size; i++) {
                    collectionLinks.addTerm(1, x[i][2*N]);
                }
                grbModel.addConstr(collectionLinks, GRB.EQUAL, fleetSize, "Flow constraint of dispatch links");
            }

            else {
                //Search space constraints
                for (int n=0; n<N; n++) {
                    for (int i=0; i<size; i++) {
                        for (int j = 0; j <size; j++) {
                            GRBLinExpr searchSpace = new GRBLinExpr();
                            searchSpace.addTerm(1, y[n][i][j]);
                            if (Case == 2) { grbModel.addConstr(searchSpace, GRB.LESS_EQUAL, availableSpace2[i][j], "Search space constraint"); }
                            if (Case == 3) { grbModel.addConstr(searchSpace, GRB.LESS_EQUAL, availableSpace3[i][j], "Search space constraint"); }
                        }
                    }
                }
                //Dispatch times constraint
                for (int n=0; n<N; n++) {
                    GRBLinExpr dispatchConstr = new GRBLinExpr();
                    for (int j=0; j<size; j++) {
                        dispatchConstr.addTerm(1, y[n][2*N][j]);
                        dispatchConstr.addTerm(1, y[n][2*N+1][j]);
                    }
                    grbModel.addConstr(dispatchConstr, GRB.LESS_EQUAL, 1, "Dispatch times constraint");
                }
                //Collection times constraint
                for (int n=0; n<N; n++) {
                    GRBQuadExpr collectionConstr = new GRBQuadExpr();
                    for (int i=N; i<size; i++) {
                        collectionConstr.addTerm(1, y[n][i][2*N]);
                        collectionConstr.addTerm(1, y[n][i][2*N+1]);
                    }
                    grbModel.addQConstr(collectionConstr, GRB.LESS_EQUAL, 1, "Collection times constraint");
                }
                //Flow balance
                //Dispatch links
                GRBLinExpr dispatchLinks = new GRBLinExpr();
                for (int i=0; i<size; i++) {
                    dispatchLinks.addTerm(1, x[2*N][i]);
                    dispatchLinks.addTerm(1, x[2*N+1][i]);
                }
                grbModel.addConstr(dispatchLinks, GRB.EQUAL, fleetSize, "Flow constraint of dispatch links");
                //Collection links
                GRBLinExpr collectionLinks = new GRBLinExpr();
                for (int i=0; i<size; i++) {
                    collectionLinks.addTerm(1, x[i][2*N]);
                    collectionLinks.addTerm(1, x[i][2*N+1]);
                }
                grbModel.addConstr(collectionLinks, GRB.EQUAL, fleetSize, "Flow constraint of dispatch links");

                if (Case == 2) {
                    //Departure and arrival stations constraints
                    for (int n=0; n<N; n++) {
                        GRBLinExpr stationConstr = new GRBLinExpr();
                        for (int j=0; j<size; j++) {
                            stationConstr.addTerm(1, y[n][2*N][j]);
                            stationConstr.addTerm(-1, y[n][j][2*N]);
                        }
                        grbModel.addConstr(stationConstr, GRB.EQUAL, 0, "Stations constraints");
                        GRBLinExpr stationConstr2 = new GRBLinExpr();
                        for (int j=0; j<size; j++) {
                            stationConstr2.addTerm(1, y[n][2*N+1][j]);
                            stationConstr2.addTerm(-1, y[n][j][2*N+1]);
                        }
                        grbModel.addConstr(stationConstr2, GRB.EQUAL, 0, "Stations constraints");
                    }
                }
            }


            //Sole model
            grbModel.optimize();

            //set the attributes of grbModel and optimalResult
            vehicleUsed = new int[fleetSize][size][size];
            linkUsed = new int[size][size];
            //get the value of object
            optimalResult = grbModel.get(GRB.DoubleAttr.ObjVal);
            //get the solution of decision variables
            for (int n=0; n<N; n++) {
                for (int i=0; i<size; i++) {
                    for (int j=0; j<size; j++) {
                        vehicleUsed[n][i][j] = (int)y[n][i][j].get(GRB.DoubleAttr.X);
                    }
                }
            }
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    linkUsed[i][j] = (int)x[i][j].get(GRB.DoubleAttr.X);
                }
            }


        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
            e.printStackTrace();
        }
    }

    public void printSolution(int object) {
        //set the print pattern
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormat df1 = new DecimalFormat("0.0");
        //print the optimal results
        if (object==0) { System.out.println("\nTotal cost is: "+df.format(optimalResult)); }
        else { System.out.println("\nTotal fuel consumption is: "+df.format(optimalResult)); }
        //print the number of vehicles used
        int number = 0;
        if (Case == 1) { number = fleetSize - linkUsed[2*N][2*N];}
        if (Case == 2) { number = fleetSize - linkUsed[2*N][2*N] - linkUsed[2*N+1][2*N+1]; }
        if (Case == 3) { number = fleetSize - linkUsed[2*N][2*N] - linkUsed[2*N][2*N+1] - linkUsed[2*N+1][2*N] - linkUsed[2*N+1][2*N+1]; }
        System.out.println("The number of AVs used is: "+number);
        //print the vehicle use rate
        double useRate = fleetSize / (double)number;
        System.out.println("The vehicle use rate is: "+df1.format(useRate));
        //print the trip chain of vehicles
        System.out.println("Solution:");

        for (int n=0; n<N; n++) {
            /*System.out.println("The service path of vehicle "+n+" is:");
            for (int i=0; i<size; i++) {
                for (int j=0; j<size; j++) {
                    System.out.print(vehicleUsed[n][i][j]+" ");
                }
                System.out.println();
            }*/

            if (Case == 1) {
                String tripchain = "O  to  ";
                if (vehicleUsed[n][2*N][2*N]==1) {tripchain += "D";}
                for (int j=0; j<N; j++) {
                    if (vehicleUsed[n][2*N][j]==1) {
                        tripchain = tripchain +j+"-"+"  to  ";
                        int k = j;
                        for (int i=0; i<size; i++) {
                            if (vehicleUsed[n][k][i]==1) {
                                if (i <N) { tripchain = tripchain + i+"-"+" to "; }
                                else if (i>=N && i<2*N) { tripchain = tripchain + (i-N)+"+"+" to "; }
                                else { tripchain = tripchain + "D";  break;}
                                k = i;
                                i = -1;
                            }
                        }
                    }
                    else { continue; }
                }
                if (vehicleUsed[n][2*N][2*N]!=1) {
                    System.out.println("The trip chain of vehicle "+n+" is: ");
                    System.out.println(tripchain);
                }
            }
            if (Case == 2) {
                String tripchain = "";
                for (int j=0; j<N; j++) {
                    if (vehicleUsed[n][2*N][j]==1) {
                        tripchain += "S1  to  ";
                        if (vehicleUsed[n][2*N][2*N]==1) {tripchain += "S2";}
                        tripchain = tripchain +j+"-"+"  to  ";
                        int k = j;
                        for (int i=0; i<size; i++) {
                            if (vehicleUsed[n][k][i]==1) {
                                if (i <N) { tripchain = tripchain + i+"-"+" to "; }
                                else if (i>=N && i<2*N) { tripchain = tripchain + (i-N)+"+"+" to "; }
                                else { tripchain = tripchain + "S2";  break;}
                                k = i;
                                i = -1;
                            }
                        }
                    }
                    else if (vehicleUsed[n][2*N+1][j]==1) {
                        tripchain += "S2  to  ";
                        if (vehicleUsed[n][2*N][2*N]==1) {tripchain += "S1";}
                        tripchain = tripchain +j+"-"+"  to  ";
                        int k = j;
                        for (int i=0; i<size; i++) {
                            if (vehicleUsed[n][k][i]==1) {
                                if (i <N) { tripchain = tripchain + i+"-"+" to "; }
                                else if (i>=N && i<2*N) { tripchain = tripchain + (i-N)+"+"+" to "; }
                                else { tripchain = tripchain + "S1";  break;}
                                k = i;
                                i = -1;
                            }
                        }
                    }
                    else { continue; }
                }
                if (vehicleUsed[n][2*N][2*N]!=1 && vehicleUsed[n][2*N+1][2*N+1]!=1) {
                    System.out.println("The trip chain of vehicle "+n+" is: ");
                    System.out.println(tripchain);
                }
            }
            if (Case == 3) {
                String tripchain = "";
                for (int j=0; j<N; j++) {
                    if (vehicleUsed[n][2*N][j]==1) {
                        tripchain += "S1  to  ";
                        if (vehicleUsed[n][2*N][2*N]==1) {tripchain += "S2";}
                        if (vehicleUsed[n][2*N][2*N+1]==1) {tripchain += "S1";}
                        tripchain = tripchain +j+"-"+"  to  ";
                        int k = j;
                        for (int i=0; i<size; i++) {
                            if (vehicleUsed[n][k][i]==1) {
                                if (i <N) { tripchain = tripchain + i+"-"+" to "; }
                                else if (i>=N && i<2*N) { tripchain = tripchain + (i-N)+"+"+" to "; }
                                else if (i == 2*N) { tripchain = tripchain + "S2";  break;}
                                else { tripchain = tripchain + "S1";  break;}
                                k = i;
                                i = -1;
                            }
                        }
                    }
                    else if (vehicleUsed[n][2*N+1][j]==1) {
                        tripchain += "S2  to  ";
                        if (vehicleUsed[n][2*N+1][2*N]==1) {tripchain += "S2";}
                        if (vehicleUsed[n][2*N+1][2*N+1]==1) {tripchain += "S1";}
                        tripchain = tripchain +j+"-"+"  to  ";
                        int k = j;
                        for (int i=0; i<size; i++) {
                            if (vehicleUsed[n][k][i]==1) {
                                if (i <N) { tripchain = tripchain + i+"-"+" to "; }
                                else if (i>=N && i<2*N) { tripchain = tripchain + (i-N)+"+"+" to "; }
                                else if (i == 2*N+1){ tripchain = tripchain + "S1";  break;}
                                else { tripchain = tripchain + "S2";  break;}
                                k = i;
                                i = -1;
                            }
                        }
                    }
                    else { continue; }
                }
                if (vehicleUsed[n][2*N][2*N]!=1 && vehicleUsed[n][2*N][2*N+1]!=1 && vehicleUsed[n][2*N+1][2*N]!=1 && vehicleUsed[n][2*N+1][2*N+1]!=1) {
                    System.out.println("The trip chain of vehicle "+n+" is: ");
                    System.out.println(tripchain);
                }
            }

        }

        /*System.out.println("The condition of used links is:");
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                System.out.print(linkUsed[i][j]+" ");
            }
            System.out.println();
        }*/
    }

    public static void main(String args[]) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入服务区域尺寸L：");
        int L = sc.nextInt();
        System.out.println("请输入出行需求数量N：");
        int N = sc.nextInt();
        //int L = 20;
        //int N = 15;
        System.out.println("出行需求信息：");
        Instance ins = new Instance(L, N);
        ins.printDemand();
        ins.traditionalFuel();
        //ins.traditionalFuel();
        Model model = new Model(ins);
        //c=0 the relax time constraint, c=1 the strict time constraint, c=2 without ride-sharing
        //Case=1 from o to d, Case=2 from s1 to s2 or from s2 to s1, Case=3 from s1 to s1 and from s2 to s2 are added
        Optimization op = new Optimization(2, 1, model);
        Optimization op1 = new Optimization(0, 1, model);
        Optimization op2 = new Optimization(0, 2, model);
        Optimization op3 = new Optimization(0, 3, model);
        //object=0 cost, object=1 energy consumption
        System.out.println("开始优化...");
        op.optimize(true, 1, model);
        op.printSolution(1);
        op1.optimize(true, 1, model);
        op1.printSolution(1);
        op2.optimize(true, 1, model);
        op2.printSolution(1);
        op3.optimize(true, 1, model);
        op3.printSolution(1);
        //model.printInfo("cost", model.cost);
    }
}