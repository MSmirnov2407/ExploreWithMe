package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;

import java.time.LocalDateTime;
import java.util.List;

public class MainClient {

    public static void main(String[] args) {

        EndpointHit epHit = new EndpointHit();
        epHit.setIp("111.111.111.111");
        epHit.setApp("myAppppp");
        epHit.setUri("/myUri/33");
        epHit.setTimestamp(LocalDateTime.now());

        StatsClient statsClient = new StatsClient();
        System.out.println("main:statsClient: = " + statsClient);


        EndpointHitDto hit1 = new EndpointHitDto();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setTimestamp("2020-05-05 00:00:00");
        hit1.setIp("172.16.64.100");

        EndpointHitDto hit2 = new EndpointHitDto();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setTimestamp("2020-05-05 00:00:00");
        hit2.setIp("172.16.64.100");

        EndpointHitDto hit3 = new EndpointHitDto();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/1");
        hit3.setTimestamp("2020-05-05 00:00:00");
        hit3.setIp("172.16.64.111");

        EndpointHitDto hit4 = new EndpointHitDto();
        hit4.setApp("ewm-main-service");
        hit4.setUri("/events/1");
        hit4.setTimestamp("2020-05-05 00:00:00");
        hit4.setIp("172.16.64.111");

        EndpointHitDto hit5 = new EndpointHitDto();
        hit5.setApp("ewm-main-service");
        hit5.setUri("/events/2");
        hit5.setTimestamp("2020-05-05 00:00:00");
        hit5.setIp("172.16.64.111");

        ResponseEntity<String> responseHit1 = statsClient.postHit(hit1);
        System.out.println("mess1: " + responseHit1.getBody());
        System.out.println("code1: " + responseHit1.getStatusCode());

        ResponseEntity<String> responseHit2 = statsClient.postHit(hit1);
        System.out.println("mess2: " + responseHit2.getBody());
        System.out.println("code2: " + responseHit2.getStatusCode());

        ResponseEntity<String> responseHit3 = statsClient.postHit(hit1);
        System.out.println("mess3: " + responseHit3.getBody());
        System.out.println("code3: " + responseHit3.getStatusCode());

        ResponseEntity<String> responseHit4 = statsClient.postHit(hit1);
        System.out.println("mess4: " + responseHit4.getBody());
        System.out.println("code4: " + responseHit4.getStatusCode());

        ResponseEntity<String> responseHit5 = statsClient.postHit(hit1);
        System.out.println("mess5: " + responseHit5.getBody());
        System.out.println("code5: " + responseHit5.getStatusCode());

        String[] uris = {"/events/2", "/events/3"};
        Boolean unique = false;
        List<EndpointStats> stats = statsClient.getStats(LocalDateTime.of(2020, 01, 01, 01, 01, 01),
                LocalDateTime.of(2025, 01, 01, 01, 01, 01),
                uris,
                unique
        );
        System.out.println(stats);


    }

}
