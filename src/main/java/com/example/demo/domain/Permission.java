// package com.example.demo.domain;

// import java.util.List;

// import org.hibernate.annotations.UuidGenerator;

// import com.fasterxml.jackson.annotation.JsonIgnore;

// import jakarta.persistence.Entity;
// import jakarta.persistence.Id;
// import jakarta.persistence.ManyToMany;
// import jakarta.persistence.Table;
// import jakarta.validation.constraints.NotBlank;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Getter
// @Setter
// @Entity
// @Table(name = "permissions")
// @NoArgsConstructor
// public class Permission {

// public Permission(String name, String apiPath, String method, String module)
// {
// this.name = name;
// this.apiPath = apiPath;
// this.method = method;
// this.module = module;
// }

// @Id
// @UuidGenerator
// private String id;

// @NotBlank(message = "Name không được để trống")
// private String name;

// @NotBlank(message = "ApiPath không được để trống")
// private String apiPath;

// @NotBlank(message = "Method không được để trống")
// private String method;

// @NotBlank(message = "Module không được để trống")
// private String module;

// @ManyToMany(mappedBy = "permissions")
// @JsonIgnore
// private List<Role> roles;

// }
