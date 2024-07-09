package main.java.de.voidtech.ytparty.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface TokenRepository extends JpaRepository<TokenContainer, Long> {

    @Query("FROM Tokens WHERE username = :username")
    TokenContainer getContainerByUsername(String username);

    @Query("FROM Tokens WHERE token = :token")
    TokenContainer getContainerByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM Tokens WHERE username = :username")
    void deleteToken(String username);

}
