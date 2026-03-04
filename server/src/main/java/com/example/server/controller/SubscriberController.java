package com.example.server.controller;

import com.example.server.model.Subscriber;
import com.example.server.model.User;
import com.example.server.service.SubscriberService;
import com.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = {"http://localhost:53000", "http://localhost:*"}, allowCredentials = "true")
public class SubscriberController {

    private final SubscriberService subscriberService;
    private final UserService userService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService, UserService userService) {
        this.subscriberService = subscriberService;
        this.userService = userService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMySubscribers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User currentUser = userService.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Subscriber> subscribers = subscriberService.findAll();

        Long currentId = currentUser.getId();
        subscribers.sort((a, b) -> {
            boolean aIsMe = a.getIdUser() != null && a.getIdUser().getId().equals(currentId);
            boolean bIsMe = b.getIdUser() != null && b.getIdUser().getId().equals(currentId);
            if (aIsMe) return -1;
            if (bIsMe) return 1;
            return 0;
        });

        List<Map<String, Object>> result = subscribers.stream()
                .map(this::toContactMap)
                .toList();

        return ResponseEntity.ok(result);
    }

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
        map.put("department", sub.getIdDivision() != null ? sub.getIdDivision().getNameDivision() : "");
        map.put("building", sub.getIdBuilding() != null ? sub.getIdBuilding().getNameBuilding() : "");
        map.put("dateBirth", sub.getDateBirth());

        return map;
    }

    @GetMapping
    public ResponseEntity<List<Subscriber>> getAllSubscribers() {
        return ResponseEntity.ok(subscriberService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscriber> getSubscriberById(@PathVariable Long id) {
        return subscriberService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Subscriber> createSubscriber(@RequestBody Subscriber subscriber) {
        Subscriber saved = subscriberService.save(subscriber);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> updateSubscriber(@PathVariable Long id, @RequestBody Subscriber subscriber) {
        Subscriber updated = subscriberService.update(id, subscriber);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriber(@PathVariable Long id) {
        subscriberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}