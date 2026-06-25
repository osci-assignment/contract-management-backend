package com.osci.contractmanagement.domain.model.company;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "company")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class Company {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    public static Company create(String name, ContractType contractType) {
        return Company.builder()
                .name(name)
                .contractType(contractType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .projects(new ArrayList<>())
                .build();
    }

    public void update(String name, ContractType contractType) {
        this.name = name;
        this.contractType = contractType;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.projects.forEach(Project::delete);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public Project registerProject(String title, LocalDate startDate, LocalDate endDate) {
        Project project = Project.create(this, title, startDate, endDate);
        this.projects.add(project);
        return project;
    }

    public void updateProject(Long projectId, String title, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Project project = findProjectById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다. id=" + projectId));
        project.update(title, startDate, endDate);
    }

    public Optional<Project> findProjectById(Long id) {
        return this.projects.stream()
                .filter(project -> project.getId().equals(id))
                .findFirst();
    }
}