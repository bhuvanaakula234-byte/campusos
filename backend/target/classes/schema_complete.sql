-- CampusOS Complete Schema (PostgreSQL)
-- Core entities: roles, users, departments, faculties, students, courses, subjects, timetables, attendance, marks, assignments, materials, events, placements, companies, notifications, library

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Roles
CREATE TABLE IF NOT EXISTS roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT
);

-- Users (all system users)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  phone VARCHAR(30),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- User roles (many-to-many for flexibility)
CREATE TABLE IF NOT EXISTS user_roles (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

-- Departments
CREATE TABLE IF NOT EXISTS departments (
  id SERIAL PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  hod_user_id UUID REFERENCES users(id)
);

-- Faculties
CREATE TABLE IF NOT EXISTS faculty (
  id SERIAL PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE UNIQUE,
  department_id INTEGER REFERENCES departments(id),
  designation VARCHAR(128),
  experience_years INTEGER,
  cabin VARCHAR(64)
);

-- Students
CREATE TABLE IF NOT EXISTS students (
  id SERIAL PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE UNIQUE,
  enrollment_number VARCHAR(100) UNIQUE,
  department_id INTEGER REFERENCES departments(id),
  branch VARCHAR(100),
  semester INTEGER,
  cgpa NUMERIC(4,2),
  hostel_id INTEGER,
  transport_id INTEGER
);

-- Courses and Subjects
CREATE TABLE IF NOT EXISTS courses (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) UNIQUE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  credits INTEGER
);

CREATE TABLE IF NOT EXISTS subjects (
  id SERIAL PRIMARY KEY,
  course_id INTEGER REFERENCES courses(id) ON DELETE SET NULL,
  code VARCHAR(50) UNIQUE,
  title VARCHAR(255) NOT NULL,
  semester INTEGER,
  syllabus TEXT
);

-- Subject assignments to faculty
CREATE TABLE IF NOT EXISTS subject_faculty (
  subject_id INTEGER REFERENCES subjects(id) ON DELETE CASCADE,
  faculty_id INTEGER REFERENCES faculty(id) ON DELETE SET NULL,
  PRIMARY KEY (subject_id, faculty_id)
);

-- Timetable entries
CREATE TABLE IF NOT EXISTS timetable (
  id SERIAL PRIMARY KEY,
  subject_id INTEGER REFERENCES subjects(id),
  day_of_week SMALLINT NOT NULL, -- 1=Mon .. 7=Sun
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  venue VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Attendance
CREATE TABLE IF NOT EXISTS attendance_sessions (
  id SERIAL PRIMARY KEY,
  subject_id INTEGER REFERENCES subjects(id),
  session_date DATE NOT NULL,
  faculty_id INTEGER REFERENCES faculty(id),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS attendance_records (
  id SERIAL PRIMARY KEY,
  attendance_session_id INTEGER REFERENCES attendance_sessions(id) ON DELETE CASCADE,
  student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
  status SMALLINT NOT NULL, -- 0=absent,1=present,2=late
  remarks TEXT
);

-- Marks / Internal assessments
CREATE TABLE IF NOT EXISTS assessments (
  id SERIAL PRIMARY KEY,
  subject_id INTEGER REFERENCES subjects(id),
  title VARCHAR(255),
  max_marks INTEGER,
  date DATE
);

CREATE TABLE IF NOT EXISTS marks (
  id SERIAL PRIMARY KEY,
  assessment_id INTEGER REFERENCES assessments(id) ON DELETE CASCADE,
  student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
  marks_obtained NUMERIC(6,2)
);

-- Assignments and study materials
CREATE TABLE IF NOT EXISTS assignments (
  id SERIAL PRIMARY KEY,
  subject_id INTEGER REFERENCES subjects(id),
  title VARCHAR(255),
  description TEXT,
  due_date TIMESTAMP,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS materials (
  id SERIAL PRIMARY KEY,
  subject_id INTEGER REFERENCES subjects(id),
  title VARCHAR(255),
  file_url TEXT,
  uploaded_by UUID REFERENCES users(id),
  uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Events
CREATE TABLE IF NOT EXISTS events (
  id SERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  event_type VARCHAR(50),
  start_time TIMESTAMP WITH TIME ZONE,
  end_time TIMESTAMP WITH TIME ZONE,
  created_by UUID REFERENCES users(id),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
  id SERIAL PRIMARY KEY,
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  title VARCHAR(255),
  body TEXT,
  read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Companies & Placements
CREATE TABLE IF NOT EXISTS companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  domain VARCHAR(128),
  website VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS placement_drives (
  id SERIAL PRIMARY KEY,
  company_id INTEGER REFERENCES companies(id),
  drive_date DATE,
  role VARCHAR(255),
  min_cgpa NUMERIC(3,2)
);

CREATE TABLE IF NOT EXISTS job_applications (
  id SERIAL PRIMARY KEY,
  placement_drive_id INTEGER REFERENCES placement_drives(id),
  student_id INTEGER REFERENCES students(id),
  status VARCHAR(50),
  applied_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Library
CREATE TABLE IF NOT EXISTS library_books (
  id SERIAL PRIMARY KEY,
  isbn VARCHAR(64),
  title VARCHAR(512) NOT NULL,
  author VARCHAR(255),
  copies INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS library_issues (
  id SERIAL PRIMARY KEY,
  book_id INTEGER REFERENCES library_books(id),
  student_id INTEGER REFERENCES students(id),
  issued_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  due_date DATE,
  returned_at TIMESTAMP WITH TIME ZONE
);

-- Hostel and Transport (basic)
CREATE TABLE IF NOT EXISTS hostels (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  capacity INTEGER
);

CREATE TABLE IF NOT EXISTS transports (
  id SERIAL PRIMARY KEY,
  route_name VARCHAR(255),
  bus_number VARCHAR(64)
);

-- Portfolio / Projects / Skills
CREATE TABLE IF NOT EXISTS projects (
  id SERIAL PRIMARY KEY,
  student_id INTEGER REFERENCES students(id),
  title VARCHAR(255),
  description TEXT,
  repo_url VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS skills (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128) UNIQUE
);

CREATE TABLE IF NOT EXISTS student_skills (
  student_id INTEGER REFERENCES students(id),
  skill_id INTEGER REFERENCES skills(id),
  PRIMARY KEY (student_id, skill_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_students_enroll ON students(enrollment_number);

-- Sample seed data
INSERT INTO roles (name, description) VALUES
  ('STUDENT','Student role') ON CONFLICT DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('FACULTY','Faculty role') ON CONFLICT DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('HOD','Head of Department') ON CONFLICT DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('PRINCIPAL','Principal') ON CONFLICT DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('PLACEMENT','Placement Cell') ON CONFLICT DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('ADMIN','Administrator') ON CONFLICT DO NOTHING;

-- End of schema
