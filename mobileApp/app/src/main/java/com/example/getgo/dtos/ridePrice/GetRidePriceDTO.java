package com.example.getgo.dtos.ridePrice;

public class GetRidePriceDTO {
    private Double pricePerKm;
    private Double startPrice;

    public GetRidePriceDTO() { }

    public GetRidePriceDTO(Double pricePerKm, Double startPrice) {
        this.pricePerKm = pricePerKm;
        this.startPrice = startPrice;
    }

    public Double getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(Double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public Double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(Double startPrice) {
        this.startPrice = startPrice;
    }
}
