package com.vky.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Table(name = "images")
@Entity
@EqualsAndHashCode(callSuper = false)
public class Image extends BaseEntity{
    private String name;
    private String path;
    private String type;
    @Lob
    private byte[] data;
}
