package com.example.ai_expanse_tacker.udhaar.service;

import com.example.ai_expanse_tacker.ai.dto.AiIntentResponse;
import com.example.ai_expanse_tacker.udhaar.entity.Udhaar;
import com.example.ai_expanse_tacker.udhaar.repository.UdhaarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UdhaarService {

    private final UdhaarRepository repo;

    public UdhaarService(UdhaarRepository repo) {
        this.repo = repo;
    }

    public Udhaar saveGiven(AiIntentResponse ai, UUID userId) {
        Udhaar u = new Udhaar();
        u.setUserId(userId);
        u.setAmount(ai.getAmount());
        u.setPerson(ai.getPerson());
        u.setNote(ai.getNote());
        u.setGiven(true);
        u.setDate(LocalDate.now());
        return repo.save(u);
    }

    public Udhaar saveTaken(AiIntentResponse ai, UUID userId) {
        Udhaar u = new Udhaar();
        u.setUserId(userId);
        u.setAmount(ai.getAmount());
        u.setPerson(ai.getPerson());
        u.setNote(ai.getNote());
        u.setGiven(false);
        u.setDate(LocalDate.now());
        return repo.save(u);
    }

    public List<Udhaar> getUdhaarsByUser(UUID userId) {
        return repo.findByUserId(userId);
    }

    public String deleteUdhaarByKeyword(String keyword, UUID userId) {
        if (keyword == null || keyword.isEmpty())
            return "Please specify which person or note to delete.";
        List<Udhaar> udhaars = repo.findByUserId(userId);
        Udhaar toDelete = udhaars.stream()
                .filter(u -> {
                    String person = (u.getPerson() != null ? u.getPerson() : "").toLowerCase();
                    String note = (u.getNote() != null ? u.getNote() : "").toLowerCase();
                    String kw = keyword.toLowerCase();
                    return (person.contains(kw) || kw.contains(person)) && !person.isEmpty() ||
                            (note.contains(kw) || kw.contains(note)) && !note.isEmpty();
                })
                .findFirst().orElse(null);

        if (toDelete != null) {
            repo.delete(toDelete);
            return "Successfully deleted udhaar record for: "
                    + (toDelete.getPerson() != null ? toDelete.getPerson() : toDelete.getNote());
        }
        return "Could not find an udhaar matching '" + keyword + "' to delete.";
    }
}
