package com.example.server.controller;

import com.example.server.model.Division;
import com.example.server.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
@CrossOrigin(origins = "*")
public class DivisionController {

    private final DivisionService divisionService;

    @Autowired
    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    @GetMapping
    public ResponseEntity<List<Division>> getAllDivisions() {
        List<Division> divisions = divisionService.findAll();
        return new ResponseEntity<>(divisions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Division> getDivisionById(@PathVariable Integer id) {
        return divisionService.findById(id)
                .map(division -> new ResponseEntity<>(division, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/create-simple")
    public ResponseEntity<Division> createDivisionSimple(@RequestParam String nameDivision) {
        if (nameDivision == null || nameDivision.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Division createdDivision = divisionService.createDivision(nameDivision);
        return new ResponseEntity<>(createdDivision, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Division> updateDivision(@PathVariable Integer id, @RequestBody Division division) {
        if (!divisionService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Division updatedDivision = divisionService.update(id, division);
        return new ResponseEntity<>(updatedDivision, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDivision(@PathVariable Integer id) {
        if (!divisionService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        divisionService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}