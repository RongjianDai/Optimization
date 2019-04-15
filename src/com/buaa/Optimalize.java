package com.buaa;

/**
 * Created by 戴荣健 on 2017/10/29.
 */
import gurobi.*;

public class Optimalize {
    public static void main(String args[]) {
        try {
            Instance ins = new Instance();
            //车队数量
            int fleetSize = 15;

            //容量矩阵，index[i][j]，i=20,为虚拟起点出发的路径，j=20为到达虚拟终点的路径
            int[][] capacity = new int[21][21];
            //设置容量矩阵
            for (int i = 0; i < capacity.length; i++) {
                for (int j = 0; j < capacity[i].length; j++) {
                    if (i == 20 && j == 20) {
                        capacity[i][j] = fleetSize;
                    } else {
                        capacity[i][j] = 1;
                    }
                }
            }

            //TransportCost矩阵，为AVS网络各条link所带来的花费，index[i][j]，i=20,为虚拟起点出发的路径，j=20为到达虚拟终点的路径；i<10为service link；10<=i<20为relocation link
            int[][] TransportCost = new int[21][21];
            //设置TransportCost矩阵
            int d = 30, f = 30, p = 1, d1 = 5;
            for (int i = 0; i < TransportCost.length; i++) {
                for (int j = 0; j < TransportCost[i].length; j++) {
                    if (i == 20 && j == 20) {
                        TransportCost[i][j] = 0;
                    } else if (i < 20 && j == 20) {
                        TransportCost[i][j] = d;
                    } else if (i == 20 && j != 20) {
                        TransportCost[i][j] = f + d;
                    } else if (i < 10 && ins.networkAVS[i][j] == 1 && i != j) {
                        TransportCost[i][j] = -100 * (Math.abs(ins.demand[i][0] - ins.demand[i][2]) + Math.abs(ins.demand[i][1] - ins.demand[i][3]));
                    } else if (i >= 10 && ins.networkAVS[i][j] == 1 && i != j) {
                        TransportCost[i][j] = d1 * (Math.abs(ins.demand[j][0] - ins.demand[i - 10][2]) + Math.abs(ins.demand[j][1] - ins.demand[i - 10][3]))
                                + p * (ins.demand[j][4] - ins.demand[i - 10][5]);
                    } else {
                        TransportCost[i][j] = 0;
                    }
                }
            }
            //LinkUsed矩阵，为AVS网络各条link的使用情况 1为使用，0为不适用
            int[][] linkUsed = new int[21][21];
            for (int i=0; i<linkUsed.length; i++) {
                for (int j=0; j<linkUsed[i].length; j++) {
                    if (i==20 && j==20) {
                        linkUsed[i][j] = fleetSize;
                    }
                    else if ((i==20 && j<10) || (i>=10 && j==20)) {
                        linkUsed[i][j] = 1;
                    }
                    else if ((i==20 && j>=10) || (i<10 && j==20)) {
                        linkUsed[i][j] = 0;
                    }
                    else {
                        linkUsed[i][j] = ins.networkAVS[i][j];
                    }
                }
            }

            //Model
            GRBEnv env = new GRBEnv();
            GRBModel model =new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "AVS-Optimalize");

            //link use decision variables: which link should be used
            GRBVar[][] linkDecision = new GRBVar[21][21];
            for (int i=0; i<21; i++) {
                for (int j=0; j<21; j++) {
                    //linkDecision[i][j] =
                      //      model.addVar(0, fleetSize, 0, GRB.CONTINUOUS, "From"+i+"To"+j);
                    //第二种方式
                    linkDecision[i][j] =model.addVar(0, fleetSize, 0.0, GRB.INTEGER, "From"+i+"To"+j);
                }
            }


            //The objective is to minimize the total operate cost
            GRBLinExpr obj = new GRBLinExpr();
            for (int i=0; i<linkUsed.length; i++) {
                for (int j=0; j<linkUsed[i].length; j++) {
                    obj.addTerm(TransportCost[i][j], linkDecision[i][j]);
                }
            }
            model.setObjective(obj, GRB.MINIMIZE);
            //配合上面第二种方式
           // model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);

            //Constraints
            //搜索空间限制，即需要在linkUsed[][]中搜索
            for (int i=0; i<linkDecision.length; i++) {
                for (int j=0; j<linkDecision.length; j++) {
                    GRBLinExpr SearchConstraint = new GRBLinExpr();
                    SearchConstraint.addTerm(1, linkDecision[i][j]);
                    model.addConstr(SearchConstraint, GRB.LESS_EQUAL, linkUsed[i][j], "SearchConstraint");
                }
            }

            //capacity constraint
            for (int i=0; i<linkDecision.length; i++) {
                for (int j=0; j<linkDecision.length; j++) {
                    GRBLinExpr capacityConstraint = new GRBLinExpr();
                    capacityConstraint.addTerm(1, linkDecision[i][j]);
                    model.addConstr(capacityConstraint, GRB.LESS_EQUAL, capacity[i][j], "CapacityConstraints");
                }
            }

            //流量平衡约束，进入点i的流量需要等于流出i的流量
            //real demand point
            for (int i=0; i<linkDecision.length-1; i++) {
                GRBLinExpr serviceFlow = new GRBLinExpr();
                for (int j=0; j<linkDecision.length; j++) {
                    serviceFlow.addTerm(1, linkDecision[j][i]);
                }
                for (int j=0; j<linkDecision.length; j++) {
                    serviceFlow.addTerm(-1, linkDecision[i][j]);
                }
                model.addConstr(serviceFlow, GRB.EQUAL, 0, "ServiceLink" + i);
            }
            //dispatch link
            GRBLinExpr dispatchFlow = new GRBLinExpr();
            for (int i=0; i<linkDecision.length; i++) {
                dispatchFlow.addTerm(1, linkDecision[20][i]);
            }
            model.addConstr(dispatchFlow, GRB.EQUAL, fleetSize, "dispatchLink");
            //collection link
            GRBLinExpr collectionFlow = new GRBLinExpr();
            for (int i=0; i<linkDecision.length; i++) {
                collectionFlow.addTerm(1, linkDecision[i][20]);
            }
            model.addConstr(collectionFlow, GRB.EQUAL, fleetSize, "collectionLink");

            // Solve
            model.optimize();

            //Given the demand instance
            System.out.println("需求案例：");
            for(int i = 0; i<ins.demand.length; i++) {
                System.out.println("需求"+i+"： ");
                System.out.print("  O_x: "+ins.demand[i][0]);
                System.out.print("  O_y: "+ins.demand[i][1]);
                System.out.print("  D_x: "+ins.demand[i][2]);
                System.out.print("  D_y: "+ins.demand[i][3]);
                System.out.print("  startTime: "+ins.demand[i][4]);
                System.out.print("  endTime: "+ins.demand[i][5]);
                System.out.println("  durationTime: "+(ins.demand[i][5]-ins.demand[i][4]));
            }
            System.out.println("AVS网络（不包含虚拟OD点）矩阵为：");
            for(int i = 0; i < ins.networkAVS.length; i++) {
                for (int j = 0; j < ins.networkAVS[i].length; j++) {
                    System.out.print(ins.networkAVS[i][j]+"  ");
                }
                System.out.println();
            }
            System.out.println("Transport cost of each link:");
            for(int i = 0; i < TransportCost.length; i++) {
                for (int j = 0; j < TransportCost[i].length; j++) {
                    System.out.print(TransportCost[i][j]+"  ");
                }
                System.out.println();
            }

            // Extract solution
            printSolution(model, linkDecision);

            // Dispose of model and environment
            model.dispose();
            env.dispose();

        }  catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

    }

    //输出解决方案
    public static void printSolution(GRBModel model, GRBVar[][] decision) throws GRBException {
        int[][] used = new int[21][21];
        System.out.println("\nTotalCost: " + model.get(GRB.DoubleAttr.ObjVal));
        System.out.println("Solution:");
        System.out.println("The figure represent the number of AVs that use the relevant link:");
        for (int i=0; i<21; i++) {
            for (int j=0; j<21; j++) {
                used[i][j] = (int)decision[i][j].get(GRB.DoubleAttr.X);
                System.out.print(used[i][j]+"  ");
            }
            System.out.println();
        }
        System.out.println("The trip chains are: ");
       for (int i=0; i<10; i++) {
           String tripchain = "O  to  ";
           if (used[20][i]==0) {
               continue;
           }
           else {
               tripchain = tripchain +i+"-"+"  to  "+i+"+";
               int n = i;
               for (int j=0; j<decision.length; ++j) {
                   if (j==20 && used[n+10][20]==1) {
                       tripchain = tripchain + "  to  "+"D";
                       break;
                   }
                   if (used[n+10][j]==1) {
                       tripchain = tripchain +"  to  "+j+"-"+"  to  "+j+"+";
                       n = j;
                       j = 0;
                   }
               }
           }
           System.out.println(tripchain);
       }
       System.out.println("O  to  D:  The number of empty AVs is: "+used[20][20]);

    }

}