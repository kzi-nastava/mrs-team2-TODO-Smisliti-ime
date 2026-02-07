package com.example.getgo.dtos.route;

import java.io.Serializable;

public class RouteDTO implements Serializable {
    private Long id;
    private String startingPoint;
    private String endingPoint;
    private Double estTimeMin;
    private Double estDistanceKm;
    private String encodedPolyline;

    public RouteDTO() {}

    public RouteDTO(Long id, String startingPoint, String endingPoint, Double estTimeMin, Double estDistanceKm, String encodedPolyline) {
        this.id = id;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.estTimeMin = estTimeMin;
        this.estDistanceKm = estDistanceKm;
        this.encodedPolyline = encodedPolyline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getEndingPoint() {
        return endingPoint;
    }

    public void setEndingPoint(String endingPoint) {
        this.endingPoint = endingPoint;
    }

    public Double getEstTimeMin() {
        return estTimeMin;
    }

    public void setEstTimeMin(Double estTimeMin) {
        this.estTimeMin = estTimeMin;
    }

    public Double getEstDistanceKm() {
        return estDistanceKm;
    }

    public void setEstDistanceKm(Double estDistanceKm) {
        this.estDistanceKm = estDistanceKm;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public void setEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
    }
}

