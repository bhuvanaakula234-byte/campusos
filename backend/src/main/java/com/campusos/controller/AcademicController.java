package com.campusos.controller;

import com.campusos.model.*;
import com.campusos.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AcademicController {

    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public AcademicController(SubjectRepository subjectRepository, TimetableRepository timetableRepository,
                              AttendanceSessionRepository attendanceSessionRepository, AttendanceRecordRepository attendanceRecordRepository) {
        this.subjectRepository = subjectRepository;
        this.timetableRepository = timetableRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @GetMapping("/subjects")
    public List<Subject> listSubjects(@RequestParam(required = false) Integer semester) {
        if (semester != null) return subjectRepository.findBySemester(semester);
        return subjectRepository.findAll();
    }

    @GetMapping("/timetable")
    public List<TimetableEntry> getTimetable(@RequestParam(required = false) Integer day) {
        if (day != null) return timetableRepository.findByDayOfWeek(day);
        return timetableRepository.findAll();
    }

    @PostMapping("/attendance/session")
    @PreAuthorize("hasAuthority('FACULTY') or hasAuthority('ADMIN')")
    public AttendanceSession createSession(@RequestBody Map<String, Object> body) {
        Integer subjectId = (Integer) body.get("subjectId");
        String dateStr = (String) body.get("date");
        Integer facultyId = body.get("facultyId") instanceof Integer ? (Integer) body.get("facultyId") : null;
        Optional<Subject> s = subjectRepository.findById(subjectId);
        AttendanceSession sess = new AttendanceSession();
        s.ifPresent(sess::setSubject);
        sess.setSessionDate(LocalDate.parse(dateStr));
        sess.setFacultyId(facultyId);
        return attendanceSessionRepository.save(sess);
    }

    @PostMapping("/attendance/record")
    @PreAuthorize("hasAuthority('FACULTY') or hasAuthority('ADMIN')")
    public AttendanceRecord addRecord(@RequestBody Map<String, Object> body) {
        Integer sessionId = (Integer) body.get("sessionId");
        Integer studentId = (Integer) body.get("studentId");
        Integer status = (Integer) body.get("status");
        String remarks = (String) body.get("remarks");
        AttendanceRecord rec = new AttendanceRecord();
        attendanceSessionRepository.findById(sessionId).ifPresent(rec::setAttendanceSession);
        rec.setStudentId(studentId);
        rec.setStatus(status);
        rec.setRemarks(remarks);
        return attendanceRecordRepository.save(rec);
    }

    @GetMapping("/attendance/student/{studentId}/summary")
    public ResponseEntity<?> studentAttendanceSummary(@PathVariable Integer studentId) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudentId(studentId);
        if (records.isEmpty()) return ResponseEntity.ok(Map.of("present",0,"total",0,"percentage",0));
        long present = records.stream().filter(r-> r.getStatus()!=null && r.getStatus()==1).count();
        int total = records.size();
        double pct = total==0?0: (present * 100.0 / total);
        return ResponseEntity.ok(Map.of("present", present, "total", total, "percentage", pct));
    }
}
