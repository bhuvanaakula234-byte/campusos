package com.campusos.repository;

import com.campusos.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    List<AttendanceRecord> findByStudentId(Integer studentId);
    List<AttendanceRecord> findByAttendanceSessionId(Integer attendanceSessionId);
}
