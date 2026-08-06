package com.campusos.repository;

import com.campusos.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Integer> {
    List<AttendanceSession> findBySubjectId(Integer subjectId);
    List<AttendanceSession> findBySessionDate(LocalDate date);
}
