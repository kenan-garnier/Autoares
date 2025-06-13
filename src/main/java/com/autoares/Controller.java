package com.autoares;

import javafx.scene.media.AudioClip;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Objects;

public class Controller {
    public static WebAPI api;
    public static double demanded_old = -1.0;
    public AudioClip meltdown = new AudioClip(Objects.requireNonNull(getClass().getResource("/MELTDOWN.wav")).toString());
    private Thread compensateur;
    Thread mesure = new Thread(() -> {
        // simple thread for mapping the central
        try {
            for (int mscv = 10; mscv < 56 ; mscv++) {
                api.postVariable("MSCV_0_OPENING_ORDERED", String.valueOf(mscv));
                api.postVariable("MSCV_1_OPENING_ORDERED", String.valueOf(mscv));
                api.postVariable("MSCV_2_OPENING_ORDERED", String.valueOf(mscv));

                System.out.println("Attente 20s...");
                Thread.sleep(20000);

                double kw1_1 = Double.parseDouble(DATA_Web.table.get("GENERATOR_0_KW").replace(",", "."));
                double kw2_1 = Double.parseDouble(DATA_Web.table.get("GENERATOR_1_KW").replace(",", "."));
                double kw3_1 = Double.parseDouble(DATA_Web.table.get("GENERATOR_2_KW").replace(",", "."));
                double gen_1 = (kw1_1 + kw2_1 + kw3_1)/1000;

                try (PrintWriter writer = new PrintWriter(new FileWriter("mesures.txt", true))) {
                    writer.println(mscv + " = " + gen_1 + " ;");
                }
            }

            meltdown.play();
            while (meltdown.isPlaying()) {
                System.out.println("BOUM !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    });
    public static void main(String[] args) {
        api = new WebAPI(new Controller(), true);
    }
    public void process(){
        double rods = Double.parseDouble(DATA_Web.table.get("RODS_POS_ACTUAL").replace(",", "."));
        double tmp0 = Double.parseDouble(DATA_Web.table.get("CORE_TEMP").replace(",", "."));
        double psi0 = Double.parseDouble(DATA_Web.table.get("CORE_PRESSURE").replace(",", "."));

        double clc1 = Double.parseDouble(DATA_Web.table.get("COOLANT_CORE_CIRCULATION_PUMP_0_SPEED").replace(",", "."));
        double clc2 = Double.parseDouble(DATA_Web.table.get("COOLANT_CORE_CIRCULATION_PUMP_1_SPEED").replace(",", "."));
        double clc3 = Double.parseDouble(DATA_Web.table.get("COOLANT_CORE_CIRCULATION_PUMP_2_SPEED").replace(",", "."));

        double clt0 = Double.parseDouble(DATA_Web.table.get("CONDENSER_CIRCULATION_PUMP_SPEED").replace(",", "."));
        double clt1 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_CIRCULATION_PUMP_0_SPEED").replace(",", "."));
        double clt2 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_CIRCULATION_PUMP_1_SPEED").replace(",", "."));
        double clt3 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_CIRCULATION_PUMP_2_SPEED").replace(",", "."));

        double tmp1 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_0_TEMPERATURE").replace(",", "."));
        double tmp2 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_1_TEMPERATURE").replace(",", "."));
        double tmp3 = Double.parseDouble(DATA_Web.table.get("COOLANT_SEC_2_TEMPERATURE").replace(",", "."));

        double mscv1 = Double.parseDouble(DATA_Web.table.get("MSCV_0_OPENING_ACTUAL").replace(",", "."));
        double mscv2 = Double.parseDouble(DATA_Web.table.get("MSCV_1_OPENING_ACTUAL").replace(",", "."));
        double mscv3 = Double.parseDouble(DATA_Web.table.get("MSCV_2_OPENING_ACTUAL").replace(",", "."));

        double bp1 = Double.parseDouble(DATA_Web.table.get("STEAM_TURBINE_0_BYPASS_ACTUAL").replace(",", "."));
        double bp2 = Double.parseDouble(DATA_Web.table.get("STEAM_TURBINE_1_BYPASS_ACTUAL").replace(",", "."));
        double bp3 = Double.parseDouble(DATA_Web.table.get("STEAM_TURBINE_2_BYPASS_ACTUAL").replace(",", "."));

        double demande = Double.parseDouble(DATA_Web.table.get("POWER_DEMAND_MW").replace(",", "."));

        double kw1 = Double.parseDouble(DATA_Web.table.get("GENERATOR_0_KW").replace(",", "."));
        double kw2 = Double.parseDouble(DATA_Web.table.get("GENERATOR_1_KW").replace(",", "."));
        double kw3 = Double.parseDouble(DATA_Web.table.get("GENERATOR_2_KW").replace(",", "."));
        double gen = (kw1 + kw2 + kw3)/1000;


        if (clc1 == 100 && clc2 == 100 && clc3 == 100){
            if (psi0 > 140){
                    if (tmp0 <= 310.0){
                        rods =rods-0.1;
                        api.postVariable("RODS_ALL_POS_ORDERED", String.valueOf(rods));
                    }
                    if (tmp0 >= 320.0){
                        rods = rods+0.1;
                        api.postVariable("RODS_ALL_POS_ORDERED", String.valueOf(rods));
                    }
            }else{
                rods = 100;
                api.postVariable("RODS_ALL_POS_ORDERED", String.valueOf(rods));
            }
        }else {
            api.postVariable("COOLANT_CORE_CIRCULATION_PUMP_0_ORDERED_SPEED", "100");
            api.postVariable("COOLANT_CORE_CIRCULATION_PUMP_1_ORDERED_SPEED", "100");
            api.postVariable("COOLANT_CORE_CIRCULATION_PUMP_2_ORDERED_SPEED", "100");
        }
        if (tmp1 >= 100 && tmp2 >= 100 && tmp3 >= 0){
            if (clt1 <= 99.5 && clt2 <= 99.5 && clt3 <= 99.5){
                api.postVariable("COOLANT_SEC_CIRCULATION_PUMP_0_ORDERED_SPEED", "100");
                api.postVariable("COOLANT_SEC_CIRCULATION_PUMP_1_ORDERED_SPEED", "100");
                api.postVariable("COOLANT_SEC_CIRCULATION_PUMP_2_ORDERED_SPEED", "100");
            }
            if (clt0 != 100){
                api.postVariable("CONDENSER_CIRCULATION_PUMP_ORDERED_SPEED", "100");
            }
            if (bp1 != 0 && bp2 != 0 && bp3 != 0){
                api.postVariable("STEAM_TURBINE_0_BYPASS_ORDERED", "0");
                api.postVariable("STEAM_TURBINE_1_BYPASS_ORDERED", "0");
                api.postVariable("STEAM_TURBINE_2_BYPASS_ORDERED", "0");
            }
            if (mscv1 < 10 && mscv2 < 10 && mscv3 < 10){
                mscv1 = 10;
                mscv2 = 10;
                mscv3 = 10;
                api.postVariable("MSCV_0_OPENING_ORDERED", String.valueOf(mscv1));
                api.postVariable("MSCV_1_OPENING_ORDERED", String.valueOf(mscv2));
                api.postVariable("MSCV_2_OPENING_ORDERED", String.valueOf(mscv3));
            }else{
//                if (!mesure.isAlive()){
//                    mesure.start();
//                }
                if (demande != demanded_old){
                    if (compensateur != null){
                        if (compensateur.isAlive()){
                            compensateur.interrupt();
                            try {
                                compensateur.join();
                            } catch (InterruptedException e) {
                                System.err.println("Compensateur interrupted" + e.getMessage());
                            }
                        }
                    }
                    api.postVariable("MSCV_0_OPENING_ORDERED", String.valueOf(calculerMSCV(demande)));
                    api.postVariable("MSCV_1_OPENING_ORDERED", String.valueOf(calculerMSCV(demande)));
                    api.postVariable("MSCV_2_OPENING_ORDERED", String.valueOf(calculerMSCV(demande)));
                    demanded_old = demande;
                    compensateur = init(calculerMSCV(demande), gen, demande);
                    compensateur.start();
                }
            }
        }else{
            if (bp1 != 100 && bp2 != 100 && bp3 != 100) {
                api.postVariable("STEAM_TURBINE_0_BYPASS_ORDERED", "100");
                api.postVariable("STEAM_TURBINE_0_BYPASS_ORDERED", "100");
                api.postVariable("STEAM_TURBINE_0_BYPASS_ORDERED", "100");
            }
            if (clt0 != 100){
                api.postVariable("CONDENSER_CIRCULATION_PUMP_ORDERED_SPEED", "100");
            }
        }
    }
    public static Thread init (double mscv, double gen, double demande){
         Thread thread= new Thread(() -> {
            try {
                Thread.sleep(40000);
                if (gen - demande > 70){
                    api.postVariable("MSCV_0_OPENING_ORDERED", String.valueOf(mscv - 1));
                    api.postVariable("MSCV_1_OPENING_ORDERED", String.valueOf(mscv - 1));
                    api.postVariable("MSCV_2_OPENING_ORDERED", String.valueOf(mscv - 1));
                }else if (gen - demande < 0){
                    api.postVariable("MSCV_0_OPENING_ORDERED", String.valueOf(mscv + 1));
                    api.postVariable("MSCV_1_OPENING_ORDERED", String.valueOf(mscv + 1));
                    api.postVariable("MSCV_2_OPENING_ORDERED", String.valueOf(mscv + 1));
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage() + " DEAD in Slepp");
            }
        });
         return thread;
    }
    public static double calculerMSCV(double valeur) {
        double a = -0.3968;
        double b = 57.3888;
        double c = -274.6357;

        double discriminant = b*b - 4*a*(c - valeur);
        if (discriminant < 0) {
            return 10;
        }
        double sqrtDisc = Math.sqrt(discriminant);
        double x1 = (-b + sqrtDisc) / (2*a);
        double x2 = (-b - sqrtDisc) / (2*a);

        boolean x1Valide = (x1 >= 10 && x1 <= 56);
        boolean x2Valide = (x2 >= 10 && x2 <= 56);

        double solution;
        if (x1Valide && x2Valide) {
            solution = (Math.abs(x1 - 33) < Math.abs(x2 - 33)) ? x1 : x2;
        } else if (x1Valide) {
            solution = x1;
        } else if (x2Valide) {
            solution = x2;
        } else {
            solution = (Math.abs(x1 - 33) < Math.abs(x2 - 33)) ? x1 : x2;
            if (solution < 10) solution = 10;
            if (solution > 56) solution = 56;
        }
        double decimalPart = solution - Math.floor(solution);
        if (decimalPart >= 0.75) {
            solution = Math.floor(solution) + 1;
        }
        return solution - 1;
    }
}