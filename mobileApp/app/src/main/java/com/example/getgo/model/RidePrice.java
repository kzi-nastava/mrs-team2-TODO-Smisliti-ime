package com.example.getgo.model;

public class RidePrice {
    VehicleType vehicleType;
    double startPrice;
    double pricePerKm;

    public RidePrice() {
    }

    public RidePrice(VehicleType vehicleType, double startPrice, double pricePerKm) {
        this.vehicleType = vehicleType;
        this.startPrice = startPrice;
        this.pricePerKm = pricePerKm;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
}
