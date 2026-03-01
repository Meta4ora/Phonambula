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

        // Находим все связанные сущности с проверками
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        Post post = postService.findById(postId)
                .orElseThrow(() -> new RuntimeException("Должность с ID " + postId + " не найдена"));

        Division division = divisionService.findById(divisionId)
                .orElseThrow(() -> new RuntimeException("Отдел с ID " + divisionId + " не найден"));

        Building building = buildingService.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Здание с ID " + buildingId + " не найдено"));

        // Очистка номеров телефонов от форматирования
        String cleanMobile = cleanPhoneNumber(mobilePhoneNumber);
        String cleanLandline = cleanPhoneNumber(landlinePhoneNumber);
        String cleanInternal = cleanPhoneNumber(internalPhoneNumber);

        // Строгая проверка на уникальность мобильного номера телефона
        if (cleanMobile != null && !cleanMobile.isEmpty()) {
            // Ищем абонента с таким же очищенным номером
            List<Subscriber> existingWithMobile = subscriberRepository.findByCleanMobilePhoneNumber(cleanMobile);
            if (!existingWithMobile.isEmpty()) {
                throw new RuntimeException("Абонент с мобильным телефоном " + mobilePhoneNumber +
                        " (очищенный номер: " + cleanMobile + ") уже существует");
            }
        }

        // Строгая проверка на уникальность городского номера телефона
        if (cleanLandline != null && !cleanLandline.isEmpty()) {
            List<Subscriber> existingWithLandline = subscriberRepository.findByCleanLandlinePhoneNumber(cleanLandline);
            if (!existingWithLandline.isEmpty()) {
                throw new RuntimeException("Абонент с городским телефоном " + landlinePhoneNumber +
                        " (очищенный номер: " + cleanLandline + ") уже существует");
            }
        }

        // Строгая проверка на уникальность внутреннего номера телефона
        if (cleanInternal != null && !cleanInternal.isEmpty()) {
            List<Subscriber> existingWithInternal = subscriberRepository.findByCleanInternalPhoneNumber(cleanInternal);
            if (!existingWithInternal.isEmpty()) {
                throw new RuntimeException("Абонент с внутренним телефоном " + internalPhoneNumber +
                        " (очищенный номер: " + cleanInternal + ") уже существует");
            }
        }

        // Проверяем, нет ли уже абонента для этого пользователя
        if (!subscriberRepository.findByIdUserId(userId).isEmpty()) {
            throw new RuntimeException("У пользователя с ID " + userId + " уже есть абонент");
        }

        // Сохраняем оригинальные номера с форматированием
        Subscriber subscriber = new Subscriber(
                user, post, division, building,
                dateBirth,
                cabinetNumber != null ? cabinetNumber : "",
                internalPhoneNumber != null ? internalPhoneNumber : "",
                landlinePhoneNumber != null ? landlinePhoneNumber : "",
                mobilePhoneNumber != null ? mobilePhoneNumber : ""
        );

        return subscriberRepository.save(subscriber);
    }

    @Transactional
    public Subscriber save(Subscriber subscriber) {
        // Проверка на уникальность при сохранении
        validatePhoneUniqueness(subscriber, null);
        // Проверяем, что все связанные сущности существуют
        validateSubscriberRelations(subscriber);
        return subscriberRepository.save(subscriber);
    }

    @Transactional
    public Subscriber update(Long id, Subscriber subscriber) {
        Subscriber existingSubscriber = findById(id)
                .orElseThrow(() -> new RuntimeException("Абонент с ID " + id + " не найден"));

        // Проверка на уникальность при обновлении (исключая текущего абонента)
        validatePhoneUniqueness(subscriber, id);

        // Обновляем поля
        if (subscriber.getIdUser() != null) {
            User user = userService.findById(subscriber.getIdUser().getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            existingSubscriber.setIdUser(user);
        }

        if (subscriber.getIdPost() != null) {
            Post post = postService.findById(subscriber.getIdPost().getId())
                    .orElseThrow(() -> new RuntimeException("Должность не найдена"));
            existingSubscriber.setIdPost(post);
        }

        if (subscriber.getIdDivision() != null) {
            Division division = divisionService.findById(subscriber.getIdDivision().getId())
                    .orElseThrow(() -> new RuntimeException("Отдел не найден"));
            existingSubscriber.setIdDivision(division);
        }

        if (subscriber.getIdBuilding() != null) {
            Building building = buildingService.findById(subscriber.getIdBuilding().getId())
                    .orElseThrow(() -> new RuntimeException("Здание не найдено"));
            existingSubscriber.setIdBuilding(building);
        }

        // Обновляем остальные поля
        if (subscriber.getDateBirth() != null) {
            existingSubscriber.setDateBirth(subscriber.getDateBirth());
        }

        if (subscriber.getCabinetNumber() != null) {
            existingSubscriber.setCabinetNumber(subscriber.getCabinetNumber());
        }

        if (subscriber.getInternalPhoneNumber() != null) {
            existingSubscriber.setInternalPhoneNumber(subscriber.getInternalPhoneNumber());
        }

        if (subscriber.getLandlinePhoneNumber() != null) {
            existingSubscriber.setLandlinePhoneNumber(subscriber.getLandlinePhoneNumber());
        }

        if (subscriber.getMobilePhoneNumber() != null) {
            existingSubscriber.setMobilePhoneNumber(subscriber.getMobilePhoneNumber());
        }

        return subscriberRepository.save(existingSubscriber);
    }

    /**
     * Проверка уникальности номеров телефонов
     * @param subscriber проверяемый абонент
     * @param excludeId ID абонента для исключения из проверки (при обновлении)
     */
    private void validatePhoneUniqueness(Subscriber subscriber, Long excludeId) {
        String cleanMobile = cleanPhoneNumber(subscriber.getMobilePhoneNumber());
        String cleanLandline = cleanPhoneNumber(subscriber.getLandlinePhoneNumber());
        String cleanInternal = cleanPhoneNumber(subscriber.getInternalPhoneNumber());

        // Проверка мобильного телефона
        if (cleanMobile != null && !cleanMobile.isEmpty()) {
            List<Subscriber> existing = subscriberRepository.findByCleanMobilePhoneNumber(cleanMobile);

            // Фильтруем по excludeId при обновлении
            if (excludeId != null) {
                existing.removeIf(s -> s.getId().equals(excludeId));
            }

            if (!existing.isEmpty()) {
                throw new RuntimeException("Абонент с мобильным телефоном " +
                        subscriber.getMobilePhoneNumber() + " (очищенный номер: " + cleanMobile + ") уже существует");
            }
        }

        // Проверка городского телефона
        if (cleanLandline != null && !cleanLandline.isEmpty()) {
            List<Subscriber> existing = subscriberRepository.findByCleanLandlinePhoneNumber(cleanLandline);

            if (excludeId != null) {
                existing.removeIf(s -> s.getId().equals(excludeId));
            }

            if (!existing.isEmpty()) {
                throw new RuntimeException("Абонент с городским телефоном " +
                        subscriber.getLandlinePhoneNumber() + " (очищенный номер: " + cleanLandline + ") уже существует");
            }
        }

        // Проверка внутреннего телефона
        if (cleanInternal != null && !cleanInternal.isEmpty()) {
            List<Subscriber> existing = subscriberRepository.findByCleanInternalPhoneNumber(cleanInternal);

            if (excludeId != null) {
                existing.removeIf(s -> s.getId().equals(excludeId));
            }

            if (!existing.isEmpty()) {
                throw new RuntimeException("Абонент с внутренним телефоном " +
                        subscriber.getInternalPhoneNumber() + " (очищенный номер: " + cleanInternal + ") уже существует");
            }
        }
    }

    /**
     * Очищает номер телефона от лишних символов для сравнения
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        // Удаляем все НЕ цифры
        return phone.replaceAll("[^0-9]", "");
    }

    private void validateSubscriberRelations(Subscriber subscriber) {
        if (subscriber.getIdUser() != null && subscriber.getIdUser().getId() != null) {
            userService.findById(subscriber.getIdUser().getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь с ID " +
                            subscriber.getIdUser().getId() + " не найден"));
        }
        if (subscriber.getIdPost() != null && subscriber.getIdPost().getId() != null) {
            postService.findById(subscriber.getIdPost().getId())
                    .orElseThrow(() -> new RuntimeException("Должность с ID " +
                            subscriber.getIdPost().getId() + " не найдена"));
        }
        if (subscriber.getIdDivision() != null && subscriber.getIdDivision().getId() != null) {
            divisionService.findById(subscriber.getIdDivision().getId())
                    .orElseThrow(() -> new RuntimeException("Отдел с ID " +
                            subscriber.getIdDivision().getId() + " не найден"));
        }
        if (subscriber.getIdBuilding() != null && subscriber.getIdBuilding().getId() != null) {
            buildingService.findById(subscriber.getIdBuilding().getId())
                    .orElseThrow(() -> new RuntimeException("Здание с ID " +
                            subscriber.getIdBuilding().getId() + " не найдено"));
        }
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

    public List<Subscriber> findByUserId(Long userId) {
        return subscriberRepository.findByIdUserId(userId);
    }

    public List<Subscriber> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return subscriberRepository.searchSubscribers(searchTerm.trim());
    }

    @Transactional
    public void deleteById(Long id) {
        subscriberRepository.deleteById(id);
    }
}