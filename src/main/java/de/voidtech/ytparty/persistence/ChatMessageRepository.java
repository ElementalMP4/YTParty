package main.java.de.voidtech.ytparty.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("FROM Messages WHERE partyID = :partyID")
    List<ChatMessage> getMessageHistory(String partyID);

    @Modifying
    @Transactional
    @Query("DELETE FROM Messages WHERE partyID = :partyID")
    void clearMessageHistory(String partyID);

    @Modifying
    @Transactional
    @Query("DELETE FROM Messages")
    void clean();

}
