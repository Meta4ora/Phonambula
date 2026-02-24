package com.example.server.controller;

import com.example.server.model.Subscriber;
import com.example.server.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Subscriber> createSubscriber(@RequestBody Subscriber subscriber) {
        Subscriber savedSubscriber = subscriberService.save(subscriber);
        return new ResponseEntity<>(savedSubscriber, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> updateSubscriber(@PathVariable Long id, @RequestBody Subscriber subscriber) {
        if (!subscriberService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Subscriber updatedSubscriber = subscriberService.update(id, subscriber);
        return new ResponseEntity<>(updatedSubscriber, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriber(@PathVariable Long id) {
        if (!subscriberService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        subscriberService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<Subscriber>> getSubscribersByBuilding(@PathVariable Integer buildingId) {
        List<Subscriber> subscribers = subscriberService.findByBuildingId(buildingId);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @GetMapping("/division/{divisionId}")
    public ResponseEntity<List<Subscriber>> getSubscribersByDivision(@PathVariable Integer divisionId) {
        List<Subscriber> subscribers = subscriberService.findByDivisionId(divisionId);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Subscriber>> getSubscribersByPost(@PathVariable Integer postId) {
        List<Subscriber> subscribers = subscriberService.findByPostId(postId);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Subscriber>> searchSubscribers(@RequestParam(required = false) String q) {
        List<Subscriber> subscribers = subscriberService.search(q);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }
}