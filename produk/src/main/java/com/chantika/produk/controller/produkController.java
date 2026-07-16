package com.chantika.produk.controller;

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

import com.chantika.produk.model.produk;
import com.chantika.produk.service.produkService;

@RestController
@RequestMapping("/api/produk")
public class produkController {
    @Autowired
    private produkService produkService;

    @GetMapping
    public List<produk> getAllProduk() {
        return produkService.getAllProduks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<produk> getProdukById(@PathVariable Long id) {
        produk produk = produkService.getProdukById(id);
        return produk != null ? ResponseEntity.ok(produk) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public produk createProduk(@RequestBody produk produk) {
        return produkService.createProduk(produk);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduk(@PathVariable Long id) {
        produkService.deleteProduk(id);
        return ResponseEntity.ok().build();
    }

}
