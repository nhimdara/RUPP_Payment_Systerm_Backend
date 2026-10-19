-- ==========================================================
-- RUPP PAYMENT SYSTEM - SEED DATA
-- 100% Exact Match to the ERD Diagram & Entities
-- Realistic RUPP Seed Data including all 6 team members
-- Passwords:
--   admin / admin123
--   finance / finance123
--   student accounts / student123
-- ==========================================================

-- 1. Seed Faculties
INSERT INTO faculties (faculty_name, department) VALUES
('Computer Science', 'Faculty of Science'),
('Telecommunication Engineering', 'Faculty of Engineering'),
('Information Technology Engineering', 'Faculty of Engineering'),
('Mathematics', 'Faculty of Science'),
('Department of English', 'Institute of Foreign Languages')
ON CONFLICT (faculty_name) DO NOTHING;

-- 2. Seed Majors
INSERT INTO majors (major_name, major_code, faculty_id, description)
SELECT 'Computer Science', 'CS-01', f.faculty_id, 'Bachelor of Science in Computer Science'
FROM faculties f WHERE f.faculty_name = 'Computer Science'
ON CONFLICT (major_code) DO NOTHING;

INSERT INTO majors (major_name, major_code, faculty_id, description)
SELECT 'Data Science & AI', 'DS-02', f.faculty_id, 'Bachelor of Science in Data Science and AI'
FROM faculties f WHERE f.faculty_name = 'Computer Science'
ON CONFLICT (major_code) DO NOTHING;

INSERT INTO majors (major_name, major_code, faculty_id, description)
SELECT 'Telecommunication Engineering', 'TE-01', f.faculty_id, 'Bachelor of Engineering in Telecommunications'
FROM faculties f WHERE f.faculty_name = 'Telecommunication Engineering'
ON CONFLICT (major_code) DO NOTHING;

INSERT INTO majors (major_name, major_code, faculty_id, description)
SELECT 'Information Technology Engineering', 'ITE-01', f.faculty_id, 'Bachelor of Engineering in Information Technology'
FROM faculties f WHERE f.faculty_name = 'Information Technology Engineering'
ON CONFLICT (major_code) DO NOTHING;

-- 3. Seed Academic Years
INSERT INTO academic_years (year_label, start_date, end_date) VALUES
('2023-2024', '2023-10-01', '2024-07-31'),
('2024-2025', '2024-10-01', '2025-07-31'),
('2025-2026', '2025-10-01', '2026-07-31')
ON CONFLICT (year_label) DO NOTHING;

-- 4. Seed Semesters
INSERT INTO semesters (semester_name, academic_year_id)
SELECT 'Semester 1', academic_year_id FROM academic_years WHERE year_label = '2024-2025'
ON CONFLICT DO NOTHING;

INSERT INTO semesters (semester_name, academic_year_id)
SELECT 'Semester 2', academic_year_id FROM academic_years WHERE year_label = '2024-2025'
ON CONFLICT DO NOTHING;

-- 5. Seed Payment Statuses
INSERT INTO payment_statuses (status_name) VALUES
('PENDING'),
('VERIFIED'),
('CONFIRMED'),
('COMPLETED'),
('REJECTED'),
('FAILED'),
('REFUNDED')
ON CONFLICT (status_name) DO NOTHING;

-- 6. Seed Users
-- Admin
INSERT INTO users (username, email, password, role) VALUES
('admin', 'admin@rupp.edu.kh', '$2a$10$T646AseblBZNHNcqclDJFeyAj4JhgScqWjGZHdj.QPlsu2H4yhoAS', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Finance Staff
INSERT INTO users (username, email, password, role) VALUES
('finance', 'finance@rupp.edu.kh', '$2a$10$9I/abXwpmJ99ZduAgodove8FAE2oyNqhLzzC0An2QMF.OXLoC8M3q', 'ROLE_FINANCE_STAFF')
ON CONFLICT (username) DO NOTHING;

-- Students (All 6 Team Members)
INSERT INTO users (username, email, password, role) VALUES
('dara_nhim', 'dara@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT'),
('leangkhai', 'leangkhai@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT'),
('ayutaya', 'ayutaya@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT'),
('chhuntheang', 'chhuntheang@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT'),
('chihong', 'chihong@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT'),
('vannsonleng', 'vannsonleng@rupp.edu.kh', '$2a$10$u25s8wBkbhvFRglbLQpnKexVy.lraAk07zHbEoB08sGrNrpEJrLcG', 'ROLE_STUDENT')
ON CONFLICT (username) DO NOTHING;

-- 7. Seed Students Table
INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Dara', 'Nhim', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'dara_nhim' AND f.faculty_name = 'Computer Science' AND a.year_label = '2024-2025' AND m.major_code = 'CS-01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Leangkhai', 'Taing', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'leangkhai' AND f.faculty_name = 'Telecommunication Engineering' AND a.year_label = '2024-2025' AND m.major_code = 'TE-01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Ayutaya', 'Tab', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'ayutaya' AND f.faculty_name = 'Computer Science' AND a.year_label = '2024-2025' AND m.major_code = 'CS-01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Chhuntheang', 'Thorn', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'chhuntheang' AND f.faculty_name = 'Information Technology Engineering' AND a.year_label = '2024-2025' AND m.major_code = 'ITE-01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Chihong', 'Tang', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'chihong' AND f.faculty_name = 'Computer Science' AND a.year_label = '2024-2025' AND m.major_code = 'CS-01'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO students (user_id, first_name, last_name, faculty_id, academic_year_id, major_id)
SELECT u.user_id, 'Vannsonleng', 'Nuon', f.faculty_id, a.academic_year_id, m.major_id
FROM users u, faculties f, academic_years a, majors m
WHERE u.username = 'vannsonleng' AND f.faculty_name = 'Telecommunication Engineering' AND a.year_label = '2024-2025' AND m.major_code = 'TE-01'
ON CONFLICT (user_id) DO NOTHING;

-- 8. Seed Tuition Fee Structures
INSERT INTO tuition_fee_structures (faculty_id, academic_year_id, total_amount, due_date)
SELECT f.faculty_id, a.academic_year_id, 600.00, '2024-11-30'
FROM faculties f, academic_years a
WHERE f.faculty_name = 'Computer Science' AND a.year_label = '2024-2025'
ON CONFLICT DO NOTHING;

INSERT INTO tuition_fee_structures (faculty_id, academic_year_id, total_amount, due_date)
SELECT f.faculty_id, a.academic_year_id, 700.00, '2024-11-30'
FROM faculties f, academic_years a
WHERE f.faculty_name = 'Telecommunication Engineering' AND a.year_label = '2024-2025'
ON CONFLICT DO NOTHING;

INSERT INTO tuition_fee_structures (faculty_id, academic_year_id, total_amount, due_date)
SELECT f.faculty_id, a.academic_year_id, 650.00, '2024-11-30'
FROM faculties f, academic_years a
WHERE f.faculty_name = 'Information Technology Engineering' AND a.year_label = '2024-2025'
ON CONFLICT DO NOTHING;

-- 9. Seed Sample Payments
INSERT INTO payments (student_id, fee_structure_id, payment_status, payment_status_id, payment_date, total_paid, remarks)
SELECT s.student_id, tfs.fee_structure_id, 'CONFIRMED', ps.payment_status_id, '2024-10-15 10:30:00', 600.00, 'Tuition fee full payment'
FROM students s
JOIN users u ON s.user_id = u.user_id
JOIN tuition_fee_structures tfs ON tfs.faculty_id = s.faculty_id
JOIN payment_statuses ps ON ps.status_name = 'COMPLETED'
WHERE u.username = 'dara_nhim'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO payments (student_id, fee_structure_id, payment_status, payment_status_id, payment_date, total_paid, remarks)
SELECT s.student_id, tfs.fee_structure_id, 'CONFIRMED', ps.payment_status_id, '2024-10-16 14:15:00', 600.00, 'Tuition fee full payment'
FROM students s
JOIN users u ON s.user_id = u.user_id
JOIN tuition_fee_structures tfs ON tfs.faculty_id = s.faculty_id
JOIN payment_statuses ps ON ps.status_name = 'COMPLETED'
WHERE u.username = 'ayutaya'
LIMIT 1
ON CONFLICT DO NOTHING;

-- 10. Seed Payment Details
INSERT INTO payment_details (payment_id, method, transaction_ref, amount)
SELECT p.payment_id, 'KHQR_BAKONG', 'TXN-KHQR-20241015-001', 600.00
FROM payments p
JOIN students s ON p.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
WHERE u.username = 'dara_nhim'
LIMIT 1
ON CONFLICT (transaction_ref) DO NOTHING;

INSERT INTO payment_details (payment_id, method, transaction_ref, amount)
SELECT p.payment_id, 'ABA_PAYWAY', 'TXN-ABA-20241016-002', 600.00
FROM payments p
JOIN students s ON p.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
WHERE u.username = 'ayutaya'
LIMIT 1
ON CONFLICT (transaction_ref) DO NOTHING;

-- 11. Seed Receipts
INSERT INTO receipts (payment_id, receipt_number, issue_date, total_amount)
SELECT p.payment_id, 'RUPP-REC-2024-001', '2024-10-15 10:35:00', 600.00
FROM payments p
JOIN students s ON p.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
WHERE u.username = 'dara_nhim'
LIMIT 1
ON CONFLICT (payment_id) DO NOTHING;

INSERT INTO receipts (payment_id, receipt_number, issue_date, total_amount)
SELECT p.payment_id, 'RUPP-REC-2024-002', '2024-10-16 14:20:00', 600.00
FROM payments p
JOIN students s ON p.student_id = s.student_id
JOIN users u ON s.user_id = u.user_id
WHERE u.username = 'ayutaya'
LIMIT 1
ON CONFLICT (payment_id) DO NOTHING;
