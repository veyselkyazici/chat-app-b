package com.vky.repository;

import com.vky.repository.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface IImageRepository extends JpaRepository<Image, UUID> {
}
