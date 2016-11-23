package fr.enssat.lanniontech.core.vehicleElements;

import fr.enssat.lanniontech.core.Tools;
import fr.enssat.lanniontech.core.managers.HistoryManager;
import fr.enssat.lanniontech.core.pathFinding.Path;
import fr.enssat.lanniontech.core.positioning.SpaceTimePosition;
import fr.enssat.lanniontech.core.roadElements.Lane;

import java.util.UUID;

public class Vehicle {
    private final double length;
    private double distanceDone;// in m
    private Side frontSide;
    private Side backSide;
    private int ID;
    private HistoryManager historyManager;
    private Path myPath;

    private double Va = Tools.kphToMph(90);      //speed in m/s
    private double A;           //acceleration
    private double a = 2;       //max acceleration
    private double b = 1;       //deceleration
    private double T = 1;       //gap time between two car
    private double lambda = 4;
    private double v0;          //desired speed
    private double s0 = 2;      //minimum distance between two cars

    private long time; // timespamp

    /**
     * constructor of a vehicle, place the newly created vehicle on the desired lane
     *
     * @param ID             identifier for the vehicle
     * @param start          lane where the vehicle is placed
     * @param startPos       position in the lane of the new vehicle
     * @param length         length of the vehicle
     * @param speed
     * @param historyManager
     */
    public Vehicle(int ID, Lane start, double startPos, double length, double speed, long initialTime, HistoryManager historyManager, Path myPath) {
        this.ID = ID;
        this.length = length;
        this.distanceDone = 0;
        this.v0 = speed;
        this.myPath = myPath;
        this.frontSide = new Side(length + startPos, this, start);
        this.backSide = new Side(startPos, this, start);
        this.time = initialTime;
        this.historyManager = historyManager;
        historyManager.AddPosition(getGPSPosition());
    }

    /**
     * this method will actualise the acceleration of the vehicle accordingly to it's environment and parameters
     */
    public void updateAcceleration() {
        //double Sa = this.distanceToNextCar();
        //double Sprime = s0 + Va * T + (Va * (Va - nextCarSpeed())) / (2 * Math.sqrt(a * b));
        //A = a * (1 - Math.pow(Va / v0, lambda) - Math.pow(Sprime / Sa, 2));
        A = 0;
    }

    /**
     * this method will return the distance to the next car
     */
    private double distanceToNextCar() {
        return frontSide.getDistanceToNextCar();
    }

    /**
     * this method will return the speed of the car toward
     */
    private double nextCarSpeed() {
        return frontSide.getNextCarSpeed();
    }

    /**
     * actualize the position with the speed of the vehicle, then actualize it's speed for the next cycle
     */
    public void updatePos(double time, boolean log) {
        double dDone = Va * time;
        this.distanceDone += dDone;
        if (Double.isNaN(dDone)) {
            System.err.println("overflow");
        }
        backSide.moveOnPath(dDone);
        frontSide.moveOnPath(dDone);
        this.time++;
        if (log) {
            historyManager.AddPosition(getGPSPosition());
        }
        Va += A * time;
    }

    public double getSpeed() {
        return Va;
    }

    public SpaceTimePosition getGPSPosition() {
        return SpaceTimePosition.getMean(frontSide.getGPS(), backSide.getGPS(), time, ID);
    }

    public UUID getPathStep(int i) {
        return myPath.getStep(i);
    }

    public boolean isArrived() {
        return backSide.getNextRoad() == null;
    }
}
