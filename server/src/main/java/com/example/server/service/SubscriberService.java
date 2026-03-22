package com.example.server.service;

import com.example.server.dto.ContactDTO;
import com.example.server.model.*;
import com.example.server.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final UserService userService;
    private final PostService postService;
    private final DivisionService divisionService;
    private final BuildingService buildingService;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository,
                             UserService userService,
                             PostService postService,
                             DivisionService divisionService,
                             BuildingService buildingService,
                             AuditLogService auditLogService,
                             CurrentUserService currentUserService) {
        this.subscriberRepository = subscriberRepository;
        this.userService = userService;
        this.postService = postService;
        this.divisionService = divisionService;
        this.buildingService = buildingService;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    // ================= БАЗОВЫЕ МЕТОДЫ =================

    public List<Subscriber> findAll() {
        return subscriberRepository.findAll();
    }

    public Optional<Subscriber> findById(Long id) {
        return subscriberRepository.findById(id);
    }

    // ================= МЕТОДЫ СОЗДАНИЯ =================

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

        Subscriber savedSubscriber = subscriberRepository.save(subscriber);

        // Логирование
        try {
            Map<String, Object> subscriberData = Map.of(
                    "id", savedSubscriber.getId(),
                    "userId", savedSubscriber.getIdUser() != null ? savedSubscriber.getIdUser().getId() : null,
                    "postId", savedSubscriber.getIdPost() != null ? savedSubscriber.getIdPost().getId() : null,
                    "divisionId", savedSubscriber.getIdDivision() != null ? savedSubscriber.getIdDivision().getId() : null,
                    "buildingId", savedSubscriber.getIdBuilding() != null ? savedSubscriber.getIdBuilding().getId() : null,
                    "mobilePhone", savedSubscriber.getMobilePhoneNumber()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    "INSERT",
                    "subscribers",
                    savedSubscriber.getId().intValue(),
                    null,
                    subscriberData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedSubscriber;
    }

    // ================= МЕТОДЫ СОХРАНЕНИЯ И ОБНОВЛЕНИЯ =================

    @Transactional
    public Subscriber save(Subscriber subscriber) {
        boolean isNew = subscriber.getId() == null;
        Map<String, Object> beforeData = null;

        if (!isNew) {
            Optional<Subscriber> existing = findById(subscriber.getId());
            if (existing.isPresent()) {
                beforeData = Map.of(
                        "id", existing.get().getId(),
                        "userId", existing.get().getIdUser() != null ? existing.get().getIdUser().getId() : null,
                        "postId", existing.get().getIdPost() != null ? existing.get().getIdPost().getId() : null,
                        "divisionId", existing.get().getIdDivision() != null ? existing.get().getIdDivision().getId() : null,
                        "buildingId", existing.get().getIdBuilding() != null ? existing.get().getIdBuilding().getId() : null,
                        "mobilePhone", existing.get().getMobilePhoneNumber()
                );
            }
        }

        // Проверка на уникальность при сохранении
        validatePhoneUniqueness(subscriber, isNew ? null : subscriber.getId());
        // Проверяем, что все связанные сущности существуют
        validateSubscriberRelations(subscriber);

        Subscriber savedSubscriber = subscriberRepository.save(subscriber);

        // Логирование
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedSubscriber.getId(),
                    "userId", savedSubscriber.getIdUser() != null ? savedSubscriber.getIdUser().getId() : null,
                    "postId", savedSubscriber.getIdPost() != null ? savedSubscriber.getIdPost().getId() : null,
                    "divisionId", savedSubscriber.getIdDivision() != null ? savedSubscriber.getIdDivision().getId() : null,
                    "buildingId", savedSubscriber.getIdBuilding() != null ? savedSubscriber.getIdBuilding().getId() : null,
                    "mobilePhone", savedSubscriber.getMobilePhoneNumber()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    isNew ? "INSERT" : "UPDATE",
                    "subscribers",
                    savedSubscriber.getId().intValue(),
                    beforeData,
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedSubscriber;
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

        return save(existingSubscriber);
    }

    // ================= МЕТОДЫ УДАЛЕНИЯ =================

    @Transactional
    public void deleteById(Long id) {
        Optional<Subscriber> subscriberOpt = findById(id);
        if (subscriberOpt.isPresent()) {
            Subscriber subscriber = subscriberOpt.get();

            // Логирование до удаления
            try {
                Map<String, Object> beforeData = Map.of(
                        "id", subscriber.getId(),
                        "userId", subscriber.getIdUser() != null ? subscriber.getIdUser().getId() : null,
                        "postId", subscriber.getIdPost() != null ? subscriber.getIdPost().getId() : null,
                        "divisionId", subscriber.getIdDivision() != null ? subscriber.getIdDivision().getId() : null,
                        "buildingId", subscriber.getIdBuilding() != null ? subscriber.getIdBuilding().getId() : null,
                        "mobilePhone", subscriber.getMobilePhoneNumber()
                );

                auditLogService.createAuditLog(
                        currentUserService.getActorForLogging(),
                        "DELETE",
                        "subscribers",
                        id.intValue(),
                        beforeData,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            subscriberRepository.deleteById(id);
        }
    }

    // ================= МЕТОДЫ ПОИСКА =================

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

    // ================= МЕТОДЫ ДЛЯ CONTACTDTO (из второго файла) =================

    public List<ContactDTO> findAllContacts() {
        return subscriberRepository.findAllContacts();
    }

    /**
     * ПОИСК ПО ВСЕМ ПОЛЯМ (возвращает полные сущности Subscriber)
     */
    public List<Subscriber> searchSubscribers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return subscriberRepository.searchSubscribers(searchTerm.trim());
    }

    /**
     * ПОИСК ПО НОМЕРУ ТЕЛЕФОНА (возвращает полные сущности Subscriber)
     */
    public List<Subscriber> searchByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return findAll();
        }
        return subscriberRepository.searchByPhone(phone.trim());
    }

    /**
     * УНИВЕРСАЛЬНЫЙ ПОИСК ПО ВСЕМ ПОЛЯМ (быстрый, возвращает ContactDTO)
     */
    public List<ContactDTO> searchAllContacts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllContacts();
        }
        return subscriberRepository.searchAllContacts(searchTerm.trim());
    }

    /**
     * ПОИСК ТОЛЬКО ПО ТЕЛЕФОНУ (быстрый, возвращает ContactDTO)
     */
    public List<ContactDTO> searchContactsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return findAllContacts();
        }
        return subscriberRepository.searchContactsByPhone(phone.trim());
    }

    /**
     * ПОИСК С АВТООПРЕДЕЛЕНИЕМ ТИПА (если строка похожа на телефон - ищем по телефону, иначе по всем полям)
     */
    public List<ContactDTO> smartSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllContacts();
        }

        String trimmed = query.trim();

        // Если запрос содержит хотя бы одну цифру - ищем по телефону
        if (trimmed.matches(".*\\d.*")) {
            // Очищаем от всех не-цифровых символов для поиска
            String cleanPhone = trimmed.replaceAll("[^0-9]", "");
            return searchContactsByPhone(cleanPhone);
        } else {
            // Иначе ищем по текстовым полям
            return searchAllContacts(trimmed);
        }
    }

    /**
     * ПОИСК ПО ДОЛЖНОСТИ (быстрый, возвращает ContactDTO)
     */
    public List<ContactDTO> findContactsByPostId(Integer postId) {
        return subscriberRepository.findContactsByPostId(postId);
    }

    /**
     * Получить контакты по отделу (быстрый DTO)
     */
    public List<ContactDTO> findContactsByDivisionId(Integer divisionId) {
        return subscriberRepository.findContactsByDivisionId(divisionId);
    }

    /**
     * Получить контакты по зданию (быстрый DTO)
     */
    public List<ContactDTO> findContactsByBuildingId(Integer buildingId) {
        return subscriberRepository.findContactsByBuildingId(buildingId);
    }

    // ================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================

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
}