package com.chantika.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long idPelanggan;
    private Long idProduk;
    private int jumlah;
    private double harga;
    private double totalHarga;

    // --- LOGIKA OTOMATIS ---
    @PrePersist
    @PreUpdate
    private void calculateTotalHarga() {
        this.totalHarga = this.harga * this.jumlah;
    }

    // --- GETTER MANUAL (SOLUSI ERROR "CANNOT FIND SYMBOL") ---
    // Tambahkan ini agar OrderService tidak merah lagi
    
    public Long getId() {
        return id;
    }

    public Long getIdProduk() {
        return idProduk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public double getHarga() {
        return harga;
    }

    public double getTotalHarga() {
        return totalHarga;
    }
    
    public Long getIdPelanggan() {
        return idPelanggan;
    }
}