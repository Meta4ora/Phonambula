package com.example.server.controller;

import com.example.server.model.*;
import com.example.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AuditLogService auditLogService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService, 
                               UserService userService,
                               PostService postService,
                               DivisionService divisionService,
                               BuildingService buildingService,
                               AuditLogService auditLogService) {
        this.subscriberService = subscriberService;
        this.userService = userService;
        this.postService = postService;
        this.divisionService = divisionService;
        this.buildingService = buildingService;
        this.auditLogService = auditLogService;
    }

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

    // Обновление абонента
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscriber(@PathVariable Long id, @RequestBody Map<String, Object> updatedFields) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String login = auth.getName();

            User currentUser = userService.findByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Subscriber existing = subscriberService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Абонент не найден"));

            // Сохраняем состояние ДО изменений для аудита
            Map<String, Object> beforeMap = createSubscriberMap(existing);

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
            
            // Сохраняем состояние ПОСЛЕ изменений для аудита
            Map<String, Object> afterMap = createSubscriberMap(saved);
            
            // Записываем в аудит
            auditLogService.createAuditLog(
                currentUser,
                "UPDATE",
                "subscribers",
                saved.getId().intValue(),
                beforeMap,
                afterMap
            );

            return ResponseEntity.ok(toContactMap(saved));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Удаление абонента
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscriber(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String login = auth.getName();

            User currentUser = userService.findByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Subscriber existing = subscriberService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Абонент не найден"));
            
            // Сохраняем состояние ДО удаления для аудита
            Map<String, Object> beforeMap = createSubscriberMap(existing);
            
            subscriberService.deleteById(id);
            
            // Записываем в аудит
            auditLogService.createAuditLog(
                currentUser,
                "DELETE",
                "subscribers",
                id.intValue(),
                beforeMap,
                null
            );
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Вспомогательный метод для создания Map из Subscriber
    private Map<String, Object> createSubscriberMap(Subscriber sub) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", sub.getId());
        map.put("name", sub.getIdUser().getFullName());
        map.put("mobilePhone", sub.getMobilePhoneNumber());
        map.put("landlinePhone", sub.getLandlinePhoneNumber());
        map.put("internalPhone", sub.getInternalPhoneNumber());
        map.put("cabinet", sub.getCabinetNumber());
        map.put("position", sub.getIdPost() != null ? sub.getIdPost().getNamePost() : null);
        map.put("department", sub.getIdDivision() != null ? sub.getIdDivision().getNameDivision() : null);
        map.put("building", sub.getIdBuilding() != null ? sub.getIdBuilding().getNameBuilding() : null);
        return map;
    }
}