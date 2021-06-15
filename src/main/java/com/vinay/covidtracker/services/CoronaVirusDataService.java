package com.vinay.covidtracker.services;

import com.vinay.covidtracker.models.LocationStats;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;


@Service
public class CoronaVirusDataService {

    public static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> locationStatsList = new ArrayList<>();

    private Long totalNumberOfCasesReported = 0l;

    private int differenceFromPreviousCasesReported = 0;

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request =HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        long sum =0l;
        int sumOfDiff=0;
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();

            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            int latestCases = Integer.parseInt(record.get(record.size()-1));
            int previousDayCases= Integer.parseInt(record.get(record.size()-2));

            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDifferenceFromLastDay(latestCases - previousDayCases);

            sum+=locationStats.getLatestTotalCases();
            sumOfDiff += latestCases - previousDayCases;

            newStats.add(locationStats);
        }
        this.locationStatsList = newStats;
        this.totalNumberOfCasesReported = sum;
        this.differenceFromPreviousCasesReported = sumOfDiff;
    }

    public List<LocationStats> getLocationStatsList() {
        return locationStatsList;
    }

    public Long getTotalNumberOfCasesReported() {
        return totalNumberOfCasesReported;
    }

    public int getDifferenceFromPreviousCasesReported() {
        return differenceFromPreviousCasesReported;
    }
}
