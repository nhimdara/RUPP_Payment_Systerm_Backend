package com.rupp.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
<<<<<<< HEAD
@Table(name = "tuition_fee_structures", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"faculty_id", "academic_year_id"})
})
=======
@Table(name = "tuition_fee_structures")
>>>>>>> origin/feature/search
public class TuitionFeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_structure_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

<<<<<<< HEAD
=======
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id")
    private Semester semester;

>>>>>>> origin/feature/search
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    public TuitionFeeStructure() {
    }

<<<<<<< HEAD
    public TuitionFeeStructure(Long id, Faculty faculty, AcademicYear academicYear, BigDecimal totalAmount, LocalDate dueDate) {
        this.id = id;
        this.faculty = faculty;
        this.academicYear = academicYear;
=======
    public TuitionFeeStructure(Faculty faculty, AcademicYear academicYear, Semester semester, BigDecimal totalAmount, LocalDate dueDate) {
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.semester = semester;
>>>>>>> origin/feature/search
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

<<<<<<< HEAD
    public TuitionFeeStructure(Faculty faculty, AcademicYear academicYear, BigDecimal totalAmount, LocalDate dueDate) {
        this.faculty = faculty;
        this.academicYear = academicYear;
=======
    public TuitionFeeStructure(Long id, Faculty faculty, AcademicYear academicYear, Semester semester, BigDecimal totalAmount, LocalDate dueDate) {
        this.id = id;
        this.faculty = faculty;
        this.academicYear = academicYear;
        this.semester = semester;
>>>>>>> origin/feature/search
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

<<<<<<< HEAD
=======
    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

>>>>>>> origin/feature/search
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
