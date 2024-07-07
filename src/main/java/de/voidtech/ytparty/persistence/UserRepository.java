package main.java.de.voidtech.ytparty.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("FROM Users WHERE username = :username")
    User getUser(String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM Users WHERE username = :username")
    void deleteUser(String username);

}
