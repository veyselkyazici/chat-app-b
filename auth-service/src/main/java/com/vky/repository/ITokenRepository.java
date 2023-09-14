package com.vky.repository;

import com.vky.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITokenRepository extends JpaRepository<Token, Integer> {
    @Query(value = """
      select t from Token t inner join Auth a\s
      on t.auth.id = a.id\s
      where a.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(UUID id);
    Optional<Token> findByToken(String token);
    Optional<Token> findByAuthId(UUID id);
}
