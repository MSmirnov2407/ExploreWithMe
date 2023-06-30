package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootApplication
public class EwmMainService {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(EwmMainService.class, args);

//todo delete

//        StatsClient statsClient = new StatsClient(); //клиент, обращающийся к сервису статистики
//        DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
////
//        EndpointHitDto endpointHitDto = new EndpointHitDto("app", "/event/125","172.16.16.16");
//        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
//        EndpointHitDto endpointHitDt2o = new EndpointHitDto("app", "/event/126","172.16.16.16");
//        endpointHitDt2o.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
//
//        System.out.println(StatsClient.postHit(endpointHitDto));
//        System.out.println(StatsClient.postHit(endpointHitDt2o));
//
//       String[] uriArray ={"/events/125", "/events/126"} ;
//        System.out.println("uriArray"+uriArray);
//        System.out.println("uriArray"+uriArray.toString());
//
//        List<EndpointStats> endpointStatsList = StatsClient.getStats(LocalDateTime.now().minusYears(1), LocalDateTime.now().plusYears(1), uriArray, false);
//        System.out.println("EwmMainService" + endpointStatsList.size());
    }
}