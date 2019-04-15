package com.buaa;

/**
 * Created by 戴荣健 on 2017/11/2.
 */

import gurobi.*;

public class Signal {
    public static void main(String args[]) {
        try {
            int gmin = 0, gmax = 100;
            int vmax = 60;
            int gap = 2, h = 2;
            int M = 1000;
            //movement
            int[][] movement = {{50,40,5},{35,30,6},{70,55,4},{25,20,5},{55,40,3},{30,10,3},{80,55,6},{40,35,5}};
            //crosstime and phi
            int[] crosstime = new int[8];
            int[] phi = new int[8];
            for (int i=0; i<crosstime.length; i++) {
                crosstime[i] = (3+h)*movement[i][2];
                phi[i] = crosstime[i];
            }
            //tmax,tmin and tn
            double[] tmax = new double[8];
            double[] tmin = new double[8];
            double[] tn = new double[8];
            for (int i=0; i<tmax.length; i++) {
                tmax[i] = (2*movement[i][0]*3.6)/movement[i][1];
                tn[i] = (movement[i][0]*3.6)/movement[i][1];
                tmin[i] = (2*movement[i][0]*3.6)/(movement[i][1]+vmax);
            }



            //Model
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "Signal-Optimalize");

            //Order of conflict movement:
            GRBVar[][] omg = new GRBVar[8][8];
            omg[0][2] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[0][3] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[0][5] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[0][6] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[1][3] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[1][4] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[1][6] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[1][7] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[2][4] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[2][5] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[2][7] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[2][0] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[3][5] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[3][6] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[3][0] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[3][1] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[4][1] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[4][2] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[4][6] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[4][7] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[5][7] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[5][0] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[5][2] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[5][3] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[6][4] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[6][3] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[6][0] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[6][1] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            omg[7][1] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[7][2] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[7][4] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");
            omg[7][5] =model.addVar(0, 1, 0, GRB.BINARY, "Signal conflict");

            //the start time of Green light phase
            GRBVar[] sta = new GRBVar[8];
            for (int i=0; i<sta.length; i++) {
                sta[i] = model.addVar(tmin[i], gmax, 0.0, GRB.CONTINUOUS, "StartTime of green phase "+i);
            }

            //the object is to minimize the travel time for all the AVs
            GRBLinExpr obj = new GRBLinExpr();
            for (int i=0; i<sta.length; i++) {
                    obj.addTerm(movement[i][2], sta[i]);
            }
            model.setObjective(obj, GRB.MINIMIZE);

            //Constraints of conflict movements and start time of green phase
            GRBLinExpr conflictConstraint0 = new GRBLinExpr();
            conflictConstraint0.addTerm(1,omg[0][2]); conflictConstraint0.addTerm(1,omg[2][0]);
            model.addConstr(conflictConstraint0, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint0 = new GRBLinExpr();
            staConstraint0.addTerm(1,sta[2]);
            staConstraint0.addTerm(M,omg[0][2]);
            staConstraint0.addTerm(-1,sta[0]);
            model.addConstr(staConstraint0, GRB.GREATER_EQUAL, phi[0] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint1 = new GRBLinExpr();
            conflictConstraint1.addTerm(1,omg[0][3]); conflictConstraint1.addTerm(1,omg[3][0]);
            model.addConstr(conflictConstraint1, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint1 = new GRBLinExpr();
            staConstraint1.addTerm(1,sta[3]);
            staConstraint1.addTerm(M,omg[0][3]);
            staConstraint1.addTerm(-1,sta[0]);
            model.addConstr(staConstraint1, GRB.GREATER_EQUAL, phi[0] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint2 = new GRBLinExpr();
            conflictConstraint2.addTerm(1,omg[0][5]); conflictConstraint2.addTerm(1,omg[5][0]);
            model.addConstr(conflictConstraint2, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint2 = new GRBLinExpr();
            staConstraint2.addTerm(1,sta[5]);
            staConstraint2.addTerm(M,omg[0][5]);
            staConstraint2.addTerm(-1,sta[0]);
            model.addConstr(staConstraint2, GRB.GREATER_EQUAL, phi[0] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint3 = new GRBLinExpr();
            conflictConstraint3.addTerm(1,omg[0][6]); conflictConstraint3.addTerm(1,omg[6][0]);
            model.addConstr(conflictConstraint3, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint3 = new GRBLinExpr();
            staConstraint3.addTerm(1,sta[6]);
            staConstraint3.addTerm(M,omg[0][6]);
            staConstraint3.addTerm(-1,sta[0]);
            model.addConstr(staConstraint3, GRB.GREATER_EQUAL, phi[0] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint4 = new GRBLinExpr();
            conflictConstraint4.addTerm(1,omg[1][3]); conflictConstraint4.addTerm(1,omg[3][1]);
            model.addConstr(conflictConstraint4, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint4 = new GRBLinExpr();
            staConstraint4.addTerm(1,sta[3]);
            staConstraint4.addTerm(M,omg[1][3]);
            staConstraint4.addTerm(-1,sta[1]);
            model.addConstr(staConstraint4, GRB.GREATER_EQUAL, phi[1] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint5 = new GRBLinExpr();
            conflictConstraint5.addTerm(1,omg[1][4]); conflictConstraint5.addTerm(1,omg[4][1]);
            model.addConstr(conflictConstraint5, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint5 = new GRBLinExpr();
            staConstraint5.addTerm(1,sta[4]);
            staConstraint5.addTerm(M,omg[1][4]);
            staConstraint5.addTerm(-1,sta[1]);
            model.addConstr(staConstraint5, GRB.GREATER_EQUAL, phi[1] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint6 = new GRBLinExpr();
            conflictConstraint6.addTerm(1,omg[1][6]); conflictConstraint6.addTerm(1,omg[6][1]);
            model.addConstr(conflictConstraint6, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint6 = new GRBLinExpr();
            staConstraint6.addTerm(1,sta[6]);
            staConstraint6.addTerm(M,omg[1][6]);
            staConstraint6.addTerm(-1,sta[1]);
            model.addConstr(staConstraint6, GRB.GREATER_EQUAL, phi[1] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint7 = new GRBLinExpr();
            conflictConstraint7.addTerm(1,omg[1][7]); conflictConstraint7.addTerm(1,omg[7][1]);
            model.addConstr(conflictConstraint7, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint7 = new GRBLinExpr();
            staConstraint7.addTerm(1,sta[7]);
            staConstraint7.addTerm(M,omg[1][7]);
            staConstraint7.addTerm(-1,sta[1]);
            model.addConstr(staConstraint7, GRB.GREATER_EQUAL, phi[1] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint8 = new GRBLinExpr();
            conflictConstraint8.addTerm(1,omg[2][4]); conflictConstraint8.addTerm(1,omg[4][2]);
            model.addConstr(conflictConstraint8, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint8 = new GRBLinExpr();
            staConstraint8.addTerm(1,sta[4]);
            staConstraint8.addTerm(M,omg[2][4]);
            staConstraint8.addTerm(-1,sta[2]);
            model.addConstr(staConstraint8, GRB.GREATER_EQUAL, phi[2] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint9 = new GRBLinExpr();
            conflictConstraint9.addTerm(1,omg[2][5]); conflictConstraint9.addTerm(1,omg[5][2]);
            model.addConstr(conflictConstraint9, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint9 = new GRBLinExpr();
            staConstraint9.addTerm(1,sta[5]);
            staConstraint9.addTerm(M,omg[2][5]);
            staConstraint9.addTerm(-1,sta[2]);
            model.addConstr(staConstraint9, GRB.GREATER_EQUAL, phi[2] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint10 = new GRBLinExpr();
            conflictConstraint10.addTerm(1,omg[2][7]); conflictConstraint10.addTerm(1,omg[7][2]);
            model.addConstr(conflictConstraint10, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint10 = new GRBLinExpr();
            staConstraint10.addTerm(1,sta[7]);
            staConstraint10.addTerm(M,omg[2][7]);
            staConstraint10.addTerm(-1,sta[2]);
            model.addConstr(staConstraint10, GRB.GREATER_EQUAL, phi[2] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint11 = new GRBLinExpr();
            conflictConstraint11.addTerm(1,omg[2][0]); conflictConstraint11.addTerm(1,omg[0][2]);
            model.addConstr(conflictConstraint11, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint11 = new GRBLinExpr();
            staConstraint11.addTerm(1,sta[0]);
            staConstraint11.addTerm(M,omg[2][0]);
            staConstraint11.addTerm(-1,sta[2]);
            model.addConstr(staConstraint11, GRB.GREATER_EQUAL, phi[2] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint12 = new GRBLinExpr();
            conflictConstraint12.addTerm(1,omg[3][5]); conflictConstraint12.addTerm(1,omg[5][3]);
            model.addConstr(conflictConstraint12, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint12 = new GRBLinExpr();
            staConstraint12.addTerm(1,sta[5]);
            staConstraint12.addTerm(M,omg[3][5]);
            staConstraint12.addTerm(-1,sta[3]);
            model.addConstr(staConstraint12, GRB.GREATER_EQUAL, phi[3] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint13 = new GRBLinExpr();
            conflictConstraint13.addTerm(1,omg[3][6]); conflictConstraint13.addTerm(1,omg[6][3]);
            model.addConstr(conflictConstraint13, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint13 = new GRBLinExpr();
            staConstraint13.addTerm(1,sta[6]);
            staConstraint13.addTerm(M,omg[3][6]);
            staConstraint13.addTerm(-1,sta[3]);
            model.addConstr(staConstraint13, GRB.GREATER_EQUAL, phi[3] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint14 = new GRBLinExpr();
            conflictConstraint14.addTerm(1,omg[3][0]); conflictConstraint14.addTerm(1,omg[0][3]);
            model.addConstr(conflictConstraint14, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint14 = new GRBLinExpr();
            staConstraint14.addTerm(1,sta[0]);
            staConstraint14.addTerm(M,omg[3][0]);
            staConstraint14.addTerm(-1,sta[3]);
            model.addConstr(staConstraint14, GRB.GREATER_EQUAL, phi[3] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint15 = new GRBLinExpr();
            conflictConstraint15.addTerm(1,omg[3][1]); conflictConstraint15.addTerm(1,omg[1][3]);
            model.addConstr(conflictConstraint15, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint15 = new GRBLinExpr();
            staConstraint15.addTerm(1,sta[1]);
            staConstraint15.addTerm(M,omg[3][1]);
            staConstraint15.addTerm(-1,sta[3]);
            model.addConstr(staConstraint15, GRB.GREATER_EQUAL, phi[3] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint16 = new GRBLinExpr();
            conflictConstraint16.addTerm(1,omg[4][1]); conflictConstraint16.addTerm(1,omg[1][4]);
            model.addConstr(conflictConstraint16, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint16 = new GRBLinExpr();
            staConstraint16.addTerm(1,sta[1]);
            staConstraint16.addTerm(M,omg[4][1]);
            staConstraint16.addTerm(-1,sta[4]);
            model.addConstr(staConstraint16, GRB.GREATER_EQUAL, phi[4] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint17 = new GRBLinExpr();
            conflictConstraint17.addTerm(1,omg[4][2]); conflictConstraint17.addTerm(1,omg[2][4]);
            model.addConstr(conflictConstraint17, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint17 = new GRBLinExpr();
            staConstraint17.addTerm(1,sta[2]);
            staConstraint17.addTerm(M,omg[4][2]);
            staConstraint17.addTerm(-1,sta[4]);
            model.addConstr(staConstraint17, GRB.GREATER_EQUAL, phi[4] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint18 = new GRBLinExpr();
            conflictConstraint18.addTerm(1,omg[4][6]); conflictConstraint18.addTerm(1,omg[6][4]);
            model.addConstr(conflictConstraint18, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint18 = new GRBLinExpr();
            staConstraint18.addTerm(1,sta[6]);
            staConstraint18.addTerm(M,omg[4][6]);
            staConstraint18.addTerm(-1,sta[4]);
            model.addConstr(staConstraint18, GRB.GREATER_EQUAL, phi[4] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint19 = new GRBLinExpr();
            conflictConstraint19.addTerm(1,omg[4][7]); conflictConstraint19.addTerm(1,omg[7][4]);
            model.addConstr(conflictConstraint19, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint19 = new GRBLinExpr();
            staConstraint19.addTerm(1,sta[7]);
            staConstraint19.addTerm(M,omg[4][7]);
            staConstraint19.addTerm(-1,sta[4]);
            model.addConstr(staConstraint19, GRB.GREATER_EQUAL, phi[4] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint20 = new GRBLinExpr();
            conflictConstraint20.addTerm(1,omg[5][7]); conflictConstraint20.addTerm(1,omg[7][5]);
            model.addConstr(conflictConstraint20, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint20 = new GRBLinExpr();
            staConstraint20.addTerm(1,sta[7]);
            staConstraint20.addTerm(M,omg[5][7]);
            staConstraint20.addTerm(-1,sta[5]);
            model.addConstr(staConstraint20, GRB.GREATER_EQUAL, phi[5] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint21 = new GRBLinExpr();
            conflictConstraint21.addTerm(1,omg[5][0]); conflictConstraint21.addTerm(1,omg[0][5]);
            model.addConstr(conflictConstraint21, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint21 = new GRBLinExpr();
            staConstraint21.addTerm(1,sta[0]);
            staConstraint21.addTerm(M,omg[5][0]);
            staConstraint21.addTerm(-1,sta[5]);
            model.addConstr(staConstraint21, GRB.GREATER_EQUAL, phi[5] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint22 = new GRBLinExpr();
            conflictConstraint22.addTerm(1,omg[5][2]); conflictConstraint22.addTerm(1,omg[2][5]);
            model.addConstr(conflictConstraint22, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint22 = new GRBLinExpr();
            staConstraint22.addTerm(1,sta[2]);
            staConstraint22.addTerm(M,omg[5][2]);
            staConstraint22.addTerm(-1,sta[5]);
            model.addConstr(staConstraint22, GRB.GREATER_EQUAL, phi[5] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint23 = new GRBLinExpr();
            conflictConstraint23.addTerm(1,omg[5][3]); conflictConstraint23.addTerm(1,omg[3][5]);
            model.addConstr(conflictConstraint23, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint23 = new GRBLinExpr();
            staConstraint23.addTerm(1,sta[3]);
            staConstraint23.addTerm(M,omg[5][3]);
            staConstraint23.addTerm(-1,sta[5]);
            model.addConstr(staConstraint23, GRB.GREATER_EQUAL, phi[5] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint24 = new GRBLinExpr();
            conflictConstraint24.addTerm(1,omg[6][4]); conflictConstraint24.addTerm(1,omg[4][6]);
            model.addConstr(conflictConstraint24, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint24 = new GRBLinExpr();
            staConstraint24.addTerm(1,sta[4]);
            staConstraint24.addTerm(M,omg[6][4]);
            staConstraint24.addTerm(-1,sta[6]);
            model.addConstr(staConstraint24, GRB.GREATER_EQUAL, phi[6] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint25 = new GRBLinExpr();
            conflictConstraint25.addTerm(1,omg[6][3]); conflictConstraint25.addTerm(1,omg[3][6]);
            model.addConstr(conflictConstraint25, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint25 = new GRBLinExpr();
            staConstraint25.addTerm(1,sta[3]);
            staConstraint25.addTerm(M,omg[6][3]);
            staConstraint25.addTerm(-1,sta[6]);
            model.addConstr(staConstraint25, GRB.GREATER_EQUAL, phi[6] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint26 = new GRBLinExpr();
            conflictConstraint26.addTerm(1,omg[6][0]); conflictConstraint26.addTerm(1,omg[0][6]);
            model.addConstr(conflictConstraint26, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint26 = new GRBLinExpr();
            staConstraint26.addTerm(1,sta[0]);
            staConstraint26.addTerm(M,omg[6][0]);
            staConstraint26.addTerm(-1,sta[6]);
            model.addConstr(staConstraint26, GRB.GREATER_EQUAL, phi[6] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint27 = new GRBLinExpr();
            conflictConstraint27.addTerm(1,omg[6][1]); conflictConstraint27.addTerm(1,omg[1][6]);
            model.addConstr(conflictConstraint27, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint27 = new GRBLinExpr();
            staConstraint27.addTerm(1,sta[1]);
            staConstraint27.addTerm(M,omg[6][1]);
            staConstraint27.addTerm(-1,sta[6]);
            model.addConstr(staConstraint27, GRB.GREATER_EQUAL, phi[6] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint28 = new GRBLinExpr();
            conflictConstraint28.addTerm(1,omg[7][1]); conflictConstraint28.addTerm(1,omg[1][7]);
            model.addConstr(conflictConstraint28, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint28 = new GRBLinExpr();
            staConstraint28.addTerm(1,sta[1]);
            staConstraint28.addTerm(M,omg[7][1]);
            staConstraint28.addTerm(-1,sta[7]);
            model.addConstr(staConstraint28, GRB.GREATER_EQUAL, phi[7] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint29 = new GRBLinExpr();
            conflictConstraint29.addTerm(1,omg[7][2]); conflictConstraint29.addTerm(1,omg[2][7]);
            model.addConstr(conflictConstraint29, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint29 = new GRBLinExpr();
            staConstraint29.addTerm(1,sta[2]);
            staConstraint29.addTerm(M,omg[7][2]);
            staConstraint29.addTerm(-1,sta[7]);
            model.addConstr(staConstraint29, GRB.GREATER_EQUAL, phi[7] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint30= new GRBLinExpr();
            conflictConstraint30.addTerm(1,omg[7][4]); conflictConstraint30.addTerm(1,omg[4][7]);
            model.addConstr(conflictConstraint30, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint30 = new GRBLinExpr();
            staConstraint30.addTerm(1,sta[4]);
            staConstraint30.addTerm(M,omg[7][4]);
            staConstraint30.addTerm(-1,sta[7]);
            model.addConstr(staConstraint30, GRB.GREATER_EQUAL, phi[7] + gap, "constraints of conflict green phase");

            GRBLinExpr conflictConstraint31 = new GRBLinExpr();
            conflictConstraint31.addTerm(1,omg[7][5]); conflictConstraint31.addTerm(1,omg[5][7]);
            model.addConstr(conflictConstraint31, GRB.EQUAL, 1, "conflictSignal");
            GRBLinExpr staConstraint31 = new GRBLinExpr();
            staConstraint31.addTerm(1,sta[5]);
            staConstraint31.addTerm(M,omg[7][5]);
            staConstraint31.addTerm(-1,sta[7]);
            model.addConstr(staConstraint31, GRB.GREATER_EQUAL, phi[7] + gap, "constraints of conflict green phase");

            // Solve
            model.optimize();

            // Extract solution
            printSolution(model, sta);

            // Dispose of model and environment
            model.dispose();
            env.dispose();


        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }
    //输出解决方案
    public static void printSolution(GRBModel model, GRBVar[] sta) throws GRBException {
        double[] greenStart = new double[8];
        System.out.println("\nTotal Travel Time: " + model.get(GRB.DoubleAttr.ObjVal));
        System.out.println("Solution:");
        System.out.println("The start time of each green phase:");
        for (int i=0; i<sta.length; i++) {
                greenStart[i] = sta[i].get(GRB.DoubleAttr.X);
                System.out.print(greenStart[i]+" ");
        }
    }
}