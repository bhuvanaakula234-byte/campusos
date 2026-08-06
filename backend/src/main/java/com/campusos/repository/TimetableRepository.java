package com.campusos.repository;

import com.campusos.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableRepository extends JpaRepository<TimetableEntry, Integer> {
    List<TimetableEntry> findByDayOfWeek(Integer dayOfWeek);
}
