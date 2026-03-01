package com.example.server.controller;

import com.example.server.model.Subscriber;
import com.example.server.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = "*")
public class SubscriberController {

    private final SubscriberService subscriberService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

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

        // Валидация обязательных полей (все ID обязательны)
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

    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> updateSubscriber(@PathVariable Long id, @RequestBody Subscriber subscriber) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriber(@PathVariable Long id) {
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
}