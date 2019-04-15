package com.buaa;

/**
 * Created by 戴荣健 on 2017/10/28.
 */
class OD {
    //index[i][j] i为第i个需求；j=0 为横坐标，j=1 为纵坐标；共10个需求，在8*8的网格中生成起始点
    int[][] origination = new int[10][2];
    int[][] destination = new int[10][2];
    OD() {
        for (int i = 0; i < origination.length; i++) {
            origination[i][0] = (int)(Math.random() * 8);
            origination[i][1] = (int)(Math.random() * 8);
            destination[i][0] = (int)(Math.random() * 8);
            destination[i][1] = (int)(Math.random() * 8);
            while (origination[i][0] == destination[i][0] && origination[i][1] == destination[i][1]) {
                destination[i][0] = (int)(Math.random() * 8);
                destination[i][1] = (int)(Math.random() * 8);
            }
        }
    }
}
public class Instance {
    //index[i][j]: i 第i个需求；j=0 起点横坐标，j=1 起点纵坐标
    // j=2 终点横坐标，j=3 终点纵坐标，j=4 出发时间，j=5 到达时间；共 10 个需求
    public int[][] demand = new int[10][6];
    //AVS网络矩阵，index[i][j]，i为需求起点，j为需求终点；连接存在为1，不存在为0
    public int[][] networkAVS = new int[20][20];

    //需求案例构造方法
    Instance() {
        OD od = new OD();
        for(int i = 0; i<demand.length; i++) {
            demand[i][0] = od.origination[i][0];
            demand[i][1] = od.origination[i][1];
            demand[i][2] = od.destination[i][0];
            demand[i][3] = od.destination[i][1];
            int start;
            int end;
                start = (int)(Math.random() * 235);
                end = start + 5 * (Math.abs(od.destination[i][0] - od.origination[i][0]) + Math.abs(od.destination[i][1] - od.origination[i][1]));
            demand[i][4] = start;
            demand[i][5] = end;
        }
        //initialize the matrix of networkAVS with time buffer constraint
        for (int i = 0; i < networkAVS.length; i++) {
            networkAVS[i][i] = 0;
            if (i < 10) {
                networkAVS[i][i+10] = 1;
            }
            else {
                for (int j = 0; j < 10; j++) {
                    int gapTime = demand[j][4]-demand[i-10][5];
                    int gapX = Math.abs(demand[i-10][0]-demand[j][2]);
                    int gapY = Math.abs(demand[i-10][1]-demand[j][3]);
                    int gapD = gapX + gapY;
                    if (i-10 == j) {continue;}
                    else if ((gapTime >= 5 + 5 * gapD) && (gapTime <= 30 + 5 * gapD)) {
                        networkAVS[i][j] = 1;
                    }
                }
            }
        }
    }
    //打印输出方法
    public void print() {
        for(int i = 0; i<demand.length; i++) {
            System.out.println("需求"+i+"： ");
            System.out.print("  O_x: "+demand[i][0]);
            System.out.print("  O_y: "+demand[i][1]);
            System.out.print("  D_x: "+demand[i][2]);
            System.out.print("  D_y: "+demand[i][3]);
            System.out.print("  startTime: "+demand[i][4]);
            System.out.print("  endTime: "+demand[i][5]);
            System.out.println("  durationTime: "+(demand[i][5]-demand[i][4]));
        }
        System.out.println("AVS网络矩阵为：");
        for(int i = 0; i < networkAVS.length; i++) {
            for (int j = 0; j < networkAVS[i].length; j++) {
                System.out.print(networkAVS[i][j]+" ");
            }
            System.out.println();
        }
    }

    public static void main(String args[]) {
        Instance ins = new Instance();
        ins.print();
        }
    }