package fr.enssat.lanniontech.core.pathFinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Path {
    private List<UUID> path;

    public Path(){
        path = new ArrayList<>();
    }

    public void addToPath(UUID dest){
        path.add(dest);
    }

    public UUID getStep(int step){
        return path.get(step);
    }
}