package fr.enssat.lanniontech.roadElements;

import fr.enssat.lanniontech.vehicleElements.FrontBackSide;
import fr.enssat.lanniontech.positioning.Position;

import java.util.ArrayList;

public class Lane {
    private RoadSection myRoadSection;
    private Lane nextLane;
    private ArrayList<FrontBackSide> vehiclesSides;
    private double length;
    private double width;

    Lane(RoadSection myRoadSection, double length,Lane nextLane){
        this.myRoadSection = myRoadSection;
        this.length = length;
        this.nextLane = nextLane;
        vehiclesSides = new ArrayList<FrontBackSide>();
        width = 3.5;
    }

    public double getLength() {
        return length;
    }

    public void getOut(FrontBackSide side) {
        vehiclesSides.remove(side);
    }

    public void getIn(FrontBackSide side) {
        int i = 0;
        System.out.println(vehiclesSides.size());
        while(i<vehiclesSides.size()
                &&
                side.getPos()>vehiclesSides.get(i).getPos())
        {
            i++;
        }
        vehiclesSides.add(i,side);
    }

    public Lane getNextLane(){
        return nextLane;
    }

    public double getDistanceToNext(FrontBackSide side){
        int pos = vehiclesSides.indexOf(side);
        if (pos==vehiclesSides.size()-1) {
            if(nextLane==null){
                return length - side.getPos();
            }else{
                return length - side.getPos() + nextLane.getDistanceToFirst();
            }
        }else {
            return vehiclesSides.get(pos + 1).getPos() - side.getPos();
        }
    }

    public double getDistanceToFirst(){
        if (vehiclesSides.size()==0){
            if(nextLane==null){
                return length;
            }else{
                return length+nextLane.getDistanceToFirst();
            }
        }else{
            return vehiclesSides.get(0).getPos();
        }
    }

    public double getNextCarSpeed(FrontBackSide side) {
        int pos = vehiclesSides.indexOf(side);
        if (pos==vehiclesSides.size()-1) {
            return nextLane.getSpeedOfFirst();
        }else {
            return vehiclesSides.get(pos + 1).getMyVehicle().getSpeed();
        }
    }

    public double getSpeedOfFirst(){
        if (vehiclesSides.size()==0){
            if(nextLane==null){
                return 0;
            }else{
                return nextLane.getSpeedOfFirst();
            }
        }else{
            return vehiclesSides.get(0).getMyVehicle().getSpeed();
        }
    }

    public Position getPosition(double pos){
        return myRoadSection.getPosition(this,pos,width/2);
    }

    public void setNextLane(Lane nextLane) {
        this.nextLane = nextLane;
    }
}
