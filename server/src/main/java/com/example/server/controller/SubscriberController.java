package com.example.server.controller;

import com.example.server.dto.ContactDTO;
import com.example.server.model.*;
import com.example.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = {"http://localhost:53000", "http://localhost:*"}, allowCredentials = "true")
public class SubscriberController {

    private final SubscriberService subscriberService;
    private final UserService userService;
    private final PostService postService;
    private final DivisionService divisionService;
    private final BuildingService buildingService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService,
                                UserService userService,
                                PostService postService,
                                DivisionService divisionService,
                                BuildingService buildingService) {
        this.subscriberService = subscriberService;
        this.userService = userService;
        this.postService = postService;
        this.divisionService = divisionService;
        this.buildingService = buildingService;
    }

    // ================= ОСНОВНЫЕ МЕТОДЫ (из первого файла) =================

    // Получение всех абонентов текущего пользователя
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getSubscribers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User currentUser = userService.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Subscriber> subscribers = subscriberService.findAll();

        List<Map<String, Object>> result = subscribers.stream()
                .map(this::toContactMap)
                .toList();

        return ResponseEntity.ok(result);
    }

    // Маппинг абонента в формат для фронтенда
    private Map<String, Object> toContactMap(Subscriber sub) {
        User user = sub.getIdUser();
        Map<String, Object> map = new HashMap<>();

        map.put("id", sub.getId());
        map.put("key", "sub_" + sub.getId());
        map.put("name", String.format("%s %s %s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? user.getPatronymic() : "").trim());

        map.put("mobilePhone", sub.getMobilePhoneNumber() != null ? sub.getMobilePhoneNumber() : "");
        map.put("landlinePhone", sub.getLandlinePhoneNumber() != null ? sub.getLandlinePhoneNumber() : "");
        map.put("internalPhone", sub.getInternalPhoneNumber() != null ? sub.getInternalPhoneNumber() : "");
        map.put("cabinet", sub.getCabinetNumber() != null ? sub.getCabinetNumber() : "");
        map.put("position", sub.getIdPost() != null ? sub.getIdPost().getNamePost() : "");
        map.put("positionId", sub.getIdPost() != null ? sub.getIdPost().getId() : null);
        map.put("department", sub.getIdDivision() != null ? sub.getIdDivision().getNameDivision() : "");
        map.put("departmentId", sub.getIdDivision() != null ? sub.getIdDivision().getId() : null);
        map.put("building", sub.getIdBuilding() != null ? sub.getIdBuilding().getNameBuilding() : "");
        map.put("buildingId", sub.getIdBuilding() != null ? sub.getIdBuilding().getId() : null);
        map.put("dateBirth", sub.getDateBirth());

        map.put("userId", user.getId());
        map.put("role", user.getIdRole().getNameRole());

        return map;
    }

    // Обновление абонента (из первого файла - через Map)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscriber(@PathVariable Long id, @RequestBody Map<String, Object> updatedFields) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String login = auth.getName();

            User currentUser = userService.findByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Subscriber existing = subscriberService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Абонент не найден"));

            boolean isAdmin = currentUser.getIdRole().getNameRole().equalsIgnoreCase("Администратор");
            boolean isSelf = existing.getIdUser().getId().equals(currentUser.getId());

            // Проверка прав (закомментирована в первом файле, оставляем как есть)
            //if (!isAdmin && !isSelf) {
            //    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            //            .body(Map.of("error", "Нет прав на редактирование"));
            //}

            Subscriber updatedSubscriber = new Subscriber();
            updatedSubscriber.setId(existing.getId());
            updatedSubscriber.setIdUser(existing.getIdUser());

            // Обновляем простые поля
            updatedSubscriber.setMobilePhoneNumber((String) updatedFields.getOrDefault("mobilePhoneNumber", existing.getMobilePhoneNumber()));
            updatedSubscriber.setLandlinePhoneNumber((String) updatedFields.getOrDefault("landlinePhoneNumber", existing.getLandlinePhoneNumber()));
            updatedSubscriber.setInternalPhoneNumber((String) updatedFields.getOrDefault("internalPhoneNumber", existing.getInternalPhoneNumber()));
            updatedSubscriber.setCabinetNumber((String) updatedFields.getOrDefault("cabinetNumber", existing.getCabinetNumber()));
            updatedSubscriber.setDateBirth((LocalDate) updatedFields.getOrDefault("dateBirth", existing.getDateBirth()));

            // Обновляем связи через ID
            if (updatedFields.containsKey("postId") && updatedFields.get("postId") != null) {
                Integer postId = Integer.parseInt(updatedFields.get("postId").toString());
                Post post = postService.findById(postId)
                        .orElseThrow(() -> new RuntimeException("Должность не найдена"));
                updatedSubscriber.setIdPost(post);
            } else {
                updatedSubscriber.setIdPost(existing.getIdPost());
            }

            if (updatedFields.containsKey("departmentId") && updatedFields.get("departmentId") != null) {
                Integer divisionId = Integer.parseInt(updatedFields.get("departmentId").toString());
                Division division = divisionService.findById(divisionId)
                        .orElseThrow(() -> new RuntimeException("Отдел не найден"));
                updatedSubscriber.setIdDivision(division);
            } else {
                updatedSubscriber.setIdDivision(existing.getIdDivision());
            }

            if (updatedFields.containsKey("buildingId") && updatedFields.get("buildingId") != null) {
                Integer buildingId = Integer.parseInt(updatedFields.get("buildingId").toString());
                Building building = buildingService.findById(buildingId)
                        .orElseThrow(() -> new RuntimeException("Здание не найдено"));
                updatedSubscriber.setIdBuilding(building);
            } else {
                updatedSubscriber.setIdBuilding(existing.getIdBuilding());
            }

            Subscriber saved = subscriberService.update(id, updatedSubscriber);

            return ResponseEntity.ok(toContactMap(saved));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Удаление абонента (из первого файла)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscriber(@PathVariable Long id) {
        try {
            subscriberService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ (из второго файла) =================

    @GetMapping
    public ResponseEntity<List<Subscriber>> getAllSubscribers() {
        List<Subscriber> subscribers = subscriberService.findAll();
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscriber> getSubscriberById(@PathVariable Long id) {
        return subscriberService.findById(id)
                .map(subscriber -> new ResponseEntity<>(subscriber, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/create-simple")
    public ResponseEntity<Subscriber> createSubscriberSimple(
            @RequestParam Long userId,
            @RequestParam Integer postId,
            @RequestParam Integer divisionId,
            @RequestParam Integer buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateBirth,
            @RequestParam(required = false) String cabinetNumber,
            @RequestParam(required = false) String internalPhoneNumber,
            @RequestParam(required = false) String landlinePhoneNumber,
            @RequestParam(required = false) String mobilePhoneNumber) {

        // Валидация обязательных полей
        if (userId == null || postId == null || divisionId == null || buildingId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Subscriber createdSubscriber = subscriberService.createSubscriber(
                    userId, postId, divisionId, buildingId,
                    dateBirth, cabinetNumber, internalPhoneNumber,
                    landlinePhoneNumber, mobilePhoneNumber
            );
            return new ResponseEntity<>(createdSubscriber, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<Subscriber> createSubscriber(@RequestBody Subscriber subscriber) {
        try {
            Subscriber savedSubscriber = subscriberService.save(subscriber);
            return new ResponseEntity<>(savedSubscriber, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Subscriber> updateSubscriberSimple(@PathVariable Long id, @RequestBody Subscriber subscriber) {
        if (!subscriberService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            Subscriber updatedSubscriber = subscriberService.update(id, subscriber);
            return new ResponseEntity<>(updatedSubscriber, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteSubscriberSimple(@PathVariable Long id) {
        if (!subscriberService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        subscriberService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscriber>> getSubscribersByUser(@PathVariable Long userId) {
        List<Subscriber> subscribers = subscriberService.findByUserId(userId);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDTO>> getAllContacts() {
        List<ContactDTO> contacts = subscriberService.findAllContacts();
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    /**
     * ПОЛНОТЕКСТОВЫЙ ПОИСК (возвращает полные данные)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Subscriber>> searchSubscribers(
            @RequestParam String query) {
        List<Subscriber> results = subscriberService.searchSubscribers(query);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * ПОИСК ПО ТЕЛЕФОНУ (возвращает полные данные)
     */
    @GetMapping("/search/phone")
    public ResponseEntity<List<Subscriber>> searchByPhone(
            @RequestParam String phone) {
        List<Subscriber> results = subscriberService.searchByPhone(phone);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * БЫСТРЫЙ ПОИСК ПО ВСЕМ ПОЛЯМ (возвращает ContactDTO)
     */
    @GetMapping("/contacts/search")
    public ResponseEntity<List<ContactDTO>> searchContacts(
            @RequestParam(required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllContacts();
        }
        List<ContactDTO> contacts = subscriberService.searchAllContacts(query);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    /**
     * БЫСТРЫЙ ПОИСК ПО ТЕЛЕФОНУ (возвращает ContactDTO)
     */
    @GetMapping("/contacts/search/phone")
    public ResponseEntity<List<ContactDTO>> searchContactsByPhone(
            @RequestParam String phone) {
        List<ContactDTO> contacts = subscriberService.searchContactsByPhone(phone);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    /**
     * автоопределение типа запроса (возвращает ContactDTO)
     * Если в запросе есть цифры - ищет по телефону, иначе по текстовым полям
     */
    @GetMapping("/contacts/smart-search")
    public ResponseEntity<List<ContactDTO>> smartSearch(
            @RequestParam(required = false) String query) {
        List<ContactDTO> contacts = subscriberService.smartSearch(query);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    /**
     * ПОИСК ПО ДОЛЖНОСТИ (возвращает ContactDTO)
     */
    @GetMapping("/contacts/post/{postId}")
    public ResponseEntity<List<ContactDTO>> getContactsByPost(
            @PathVariable Integer postId) {
        List<ContactDTO> contacts = subscriberService.findContactsByPostId(postId);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    @GetMapping("/contacts/division/{divisionId}")
    public ResponseEntity<List<ContactDTO>> getContactsByDivision(
            @PathVariable Integer divisionId) {
        List<ContactDTO> contacts = subscriberService.findContactsByDivisionId(divisionId);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    @GetMapping("/contacts/building/{buildingId}")
    public ResponseEntity<List<ContactDTO>> getContactsByBuilding(
            @PathVariable Integer buildingId) {
        List<ContactDTO> contacts = subscriberService.findContactsByBuildingId(buildingId);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }
}