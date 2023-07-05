package ru.practicum.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatsClient {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final RestTemplate rest; //HTTP-клиент

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
//        String serverUrl = "http://localhost:9090";
        String serverUrl = "http://ewm-stat-server:9090";

        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
    }


    public static List<EndpointStats> getStats(LocalDateTime startTime, LocalDateTime endTime, @Nullable String[] uris, @Nullable Boolean unique) {
        String startString = startTime.format(TIME_FORMAT);
        String endString = endTime.format(TIME_FORMAT);
        String startEncoded = URLEncoder.encode(startString, StandardCharsets.UTF_8);
        String endEncoded = URLEncoder.encode(endString, StandardCharsets.UTF_8);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", startEncoded);
        parameters.put("end", endEncoded);

        StringBuilder sb = new StringBuilder();
        sb.append("/stats?start={start}&end={end}");
        if (uris != null) {
            parameters.put("uris", uris);
            sb.append("&uris={uris}");
        }
        if (unique != null) {
            parameters.put("unique", unique);
            sb.append("&unique={unique}");
        }
        return makeAndSendGetStatsRequest(HttpMethod.GET, sb.toString(), parameters, null);
    }

    public static ResponseEntity<String> postHit(EndpointHitDto hit) {
        ResponseEntity<String> responseEntity = makeAndSendPostHitRequest(HttpMethod.POST, "/hit", null, hit);
        return responseEntity;
    }

    /**
     * Получение данных о просмторах id
     *
     * @param eventsId - списоок id сбытий
     * @return - <id события, количество просмотров>
     */
    public static Map<Integer, Long> getMapIdViews(Collection<Integer> eventsId) {
        if (eventsId == null || eventsId.isEmpty()) {
            return new HashMap<>();
        }
        /*составляем список URI событий из подборки*/
        List<String> eventUris = eventsId.stream()
                .map(i -> "/events/" + i)
                .collect(Collectors.toList()); //преобразовали список событий в список URI

        String[] uriArray = new String[eventUris.size()]; //создали массив строк
        eventUris.toArray(uriArray); //заполнили массив строками из списка URI

        /*запрашиваем у клиента статистики данные по нужным URI*/
        List<EndpointStats> endpointStatsList = getStats(LocalDateTime.of(1970, 01, 01, 01, 01), LocalDateTime.now(), uriArray, true);

        if (endpointStatsList == null || endpointStatsList.isEmpty()) { //если нет статистики по эндпоинтам, возвращаем мапу с нулевыми просмотрами
            return eventsId.stream()
                    .collect(Collectors.toMap(e -> e, e -> 0L));
        }
        /*превращаем список EndpointStats в мапу <id события, кол-во просмотров>*/
        Map<Integer, Long> idViewsMap = endpointStatsList.stream()
                .collect(Collectors.toMap(e -> {
                            String[] splitUri = e.getUri().split("/"); //делим URI /events/1
                            Arrays.asList(splitUri).forEach(s -> System.out.println("idViewsMap + elements+///+ " + s));
                            return Integer.valueOf(splitUri[splitUri.length - 1]); //берем последний элемент разбитой строки - это id
                        },
                        EndpointStats::getHits));
        return idViewsMap;
    }

    private static <T> List<EndpointStats> makeAndSendGetStatsRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<List<EndpointStats>> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<EndpointStats>>() {
                }, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, new ParameterizedTypeReference<List<EndpointStats>>() {
                });
            }
        } catch (HttpStatusCodeException e) {
            return null;
        }
        return ewmServerResponse.getBody();
    }

    private static <T> ResponseEntity<String> makeAndSendPostHitRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<String> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, String.class, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, String.class);
            }
        } catch (HttpStatusCodeException e) {
            return null;
        }
        return ewmServerResponse;
    }

    private static HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
