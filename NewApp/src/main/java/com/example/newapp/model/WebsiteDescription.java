package com.example.newapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class WebsiteDescription {
    private String id;
    private String title;
    private String description;
    private String sdt;
    private String website_id;
}
