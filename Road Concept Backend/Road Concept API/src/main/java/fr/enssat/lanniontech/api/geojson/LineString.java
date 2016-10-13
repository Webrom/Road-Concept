package fr.enssat.lanniontech.api.geojson;

public class LineString extends MultiPoint {

    public LineString() {
    }

    public LineString(Coordinates... points) {
        super(points);
    }

    @Override
    public String toString() {
        return "LineString{} " + super.toString();
    }
}