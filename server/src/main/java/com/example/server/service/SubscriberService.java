package com.example.server.service;

import com.example.server.model.*;
import com.example.server.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final UserService userService;
    private final PostService postService;
    private final DivisionService divisionService;
    private final BuildingService buildingService;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository,
                             UserService userService,
                             PostService postService,
                             DivisionService divisionService,
                             BuildingService buildingService) {
        this.subscriberRepository = subscriberRepository;
        this.userService = userService;
        this.postService = postService;
        this.divisionService = divisionService;
        this.buildingService = buildingService;
    }

    public List<Subscriber> findAll() {
        return subscriberRepository.findAll();
    }

    public Optional<Subscriber> findById(Long id) {
        return subscriberRepository.findById(id);
    }

    @Transactional
    public Subscriber createSubscriber(Long userId, Integer postId, Integer divisionId,
                                       Integer buildingId, LocalDate dateBirth,
                                       String cabinetNumber, String internalPhoneNumber,
                                       String landlinePhoneNumber, String mobilePhoneNumber) {

        // Находим все связанные сущности
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        Post post = postService.findById(postId)
                .orElseThrow(() -> new RuntimeException("Должность с ID " + postId + " не найдена"));

        Division division = divisionService.findById(divisionId)
                .orElseThrow(() -> new RuntimeException("Отдел с ID " + divisionId + " не найден"));

        Building building = buildingService.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Здание с ID " + buildingId + " не найдено"));

        // Используем конструктор
        Subscriber subscriber = new Subscriber(user, post, division, building,
                dateBirth, cabinetNumber, internalPhoneNumber,
                landlinePhoneNumber, mobilePhoneNumber);

        return subscriberRepository.save(subscriber);
    }

    @Transactional
    public Subscriber save(Subscriber subscriber) {
        return subscriberRepository.save(subscriber);
    }

    @Transactional
    public void deleteById(Long id) {
        subscriberRepository.deleteById(id);
    }

    @Transactional
    public Subscriber update(Long id, Subscriber subscriber) {
        subscriber.setId(id);
        return subscriberRepository.save(subscriber);
    }

    public List<Subscriber> findByDivisionId(Integer divisionId) {
        return subscriberRepository.findByIdDivisionId(divisionId);
    }

    public List<Subscriber> findByBuildingId(Integer buildingId) {
        return subscriberRepository.findByIdBuildingId(buildingId);
    }

    public List<Subscriber> findByPostId(Integer postId) {
        return subscriberRepository.findByIdPostId(postId);
    }

    public List<Subscriber> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return subscriberRepository.searchSubscribers(searchTerm.trim());
    }
}