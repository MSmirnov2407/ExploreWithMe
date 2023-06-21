package ru.practicum.dataJpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.EndpointStats;
import ru.practicum.repository.StatsJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
public class StatsJpaRepositoryTest {

    @Autowired
    private StatsJpaRepository statsJpaRepository;

    /*тестовые объекты*/
    private EndpointHit hit1;
    private EndpointHit hit2;
    private EndpointHit hit3;
    private EndpointHit hit4;
    private EndpointHit hit5;

    @BeforeEach
    public void setUp() {
        /*создаем тестовые объекты*/
        hit1 = new EndpointHit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setTimestamp(LocalDateTime.now());
        hit1.setIp("172.16.64.100");

        hit2 = new EndpointHit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setTimestamp(LocalDateTime.now().minusMinutes(5));
        hit2.setIp("172.16.64.100");

        hit3 = new EndpointHit();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/1");
        hit3.setTimestamp(LocalDateTime.now().minusMinutes(10));
        hit3.setIp("172.16.64.111");

        hit4 = new EndpointHit();
        hit4.setApp("ewm-main-service");
        hit4.setUri("/events/1");
        hit4.setTimestamp(LocalDateTime.now().minusMinutes(15));
        hit4.setIp("172.16.64.111");

        hit5 = new EndpointHit();
        hit5.setApp("ewm-main-service");
        hit5.setUri("/events/2");
        hit5.setTimestamp(LocalDateTime.now().minusMinutes(20));
        hit5.setIp("172.16.64.111");

        /*сохраняем объекты в БД*/
        statsJpaRepository.save(hit1);
        statsJpaRepository.save(hit2);
        statsJpaRepository.save(hit3);
        statsJpaRepository.save(hit4);
        statsJpaRepository.save(hit5);


    }

    @AfterEach
    public void afterTest() {
        statsJpaRepository.deleteAll();//очистили бд
    }

    @Test
    void getStatsNotUniqueWithUriTest() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(25); //стартовое время выборки
        LocalDateTime end = LocalDateTime.now().plusMinutes(25); //конечное время выборки
        String[] uris = {"/events/1", "/events/2"}; //список uri

        List<EndpointStats> result = statsJpaRepository.getStatsNotUniqueWithUris(start, end, uris);
        System.out.println("result ===  " + result);
        assertThat(result.size(), equalTo(2)); //проверка велчины списка (два эндпоинта)
        assertThat(result.get(0).getHits(), equalTo(4L)); //запросы с повторного ip тоже считаются
    }

    @Test
    void getStatsUniqueWithUriTest() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(25); //стартовое время выборки
        LocalDateTime end = LocalDateTime.now().plusMinutes(25); //конечное время выборки
        String[] uris = {"/events/1", "/events/2"}; //список uri

        List<EndpointStats> result = statsJpaRepository.getStatsUniqueWithUris(start, end, uris);
        System.out.println("result ===  " + result);
        assertThat(result.size(), equalTo(2)); //проверка велчины списка (два эндпоинта)
        assertThat(result.get(0).getHits(), equalTo(2L)); //запросы с повторного ip НЕ считаются
    }

    @Test
    void getStatsNotUniqueTestWithStartCondition() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(8); //стартовое время выборки такое, чтобы не все запросы попали в выборку
        LocalDateTime end = LocalDateTime.now().plusMinutes(25); //конечное время выборки
        String[] uris = {"/events/1", "/events/2"}; //список uri

        List<EndpointStats> result = statsJpaRepository.getStatsNotUniqueWithUris(start, end, uris);
        System.out.println("result ===  " + result);
        assertThat(result.size(), equalTo(1)); //проверка велчины списка
        assertThat(result.get(0).getHits(), equalTo(2L)); //запросы, не попадающие в границы выборки, не считаются
    }
}
