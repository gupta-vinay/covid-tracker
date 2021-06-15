package com.vinay.covidtracker.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LocationStats {
    private String state;
    private String country;
    private Integer latestTotalCases;
    private Integer differenceFromLastDay;

}
