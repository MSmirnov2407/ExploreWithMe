package ru.practicum.dto.participationRequest;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат подтверждения/отклонения заявок на участие в событии
 */
@Getter
@Setter
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>(); //подтвержденные заявки
    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>(); //отклоненные заявки
}
