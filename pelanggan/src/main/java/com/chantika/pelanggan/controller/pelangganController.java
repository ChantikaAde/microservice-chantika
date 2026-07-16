package com.chantika.pelanggan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chantika.pelanggan.model.pelanggan;
import com.chantika.pelanggan.service.pelangganService;

@RestController
@RequestMapping("/api/pelanggan")
public class pelangganController {

    @Autowired
    private pelangganService pelangganService;

    @GetMapping
    public List<pelanggan> getAllPelanggan() {
        return pelangganService.getAllPelanggan();
    }

    @GetMapping("/{id}")
    public ResponseEntity<pelanggan> getPelangganById(@PathVariable Long id) {
        pelanggan p = pelangganService.getPelangganById(id);
        return p != null ? ResponseEntity.ok(p) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<pelanggan> createPelanggan(@RequestBody pelanggan pelanggan) {
        pelanggan created = pelangganService.createPelanggan(pelanggan);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePelanggan(@PathVariable Long id) {
        pelangganService.deletePelanggan(id);
        return ResponseEntity.ok().build();
    }
}