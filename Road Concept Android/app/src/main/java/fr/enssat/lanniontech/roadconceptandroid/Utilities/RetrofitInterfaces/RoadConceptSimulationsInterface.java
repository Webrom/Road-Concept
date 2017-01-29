package fr.enssat.lanniontech.roadconceptandroid.Utilities.RetrofitInterfaces;

import fr.enssat.lanniontech.roadconceptandroid.Entities.CongestionResult;
import fr.enssat.lanniontech.roadconceptandroid.Entities.Simulation;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface RoadConceptSimulationsInterface {

    @GET("api/maps/{mapID}/simulations")
    Call<List<Simulation>> getSimulationsFor1Map(@Path("mapID") int mapId);

    @GET("api/maps/{mapID}/simulations/{simulationUUID}")
    Call<Simulation> getSimulation(@Path("mapID") int mapId, @Path("simulationUUID") String simulationUUID);

    @GET("/api/simulations/{simulationUUID}/results/congestions/{timestamp}")
    Call<List<CongestionResult>> getCongestions(@Path("simulationUUID") String simulationUUID, @Path("timestamp") int timestamp);

    @GET("/api/simulations/{simulationUUID}")
    Call<Simulation> getZones(@Path("simulationUUID") String simulationUUID);

    @GET("api/users/{userID}/simulations")
    Call<List<Simulation>> getSimulationUser(@Path("userID") int userID);
}
