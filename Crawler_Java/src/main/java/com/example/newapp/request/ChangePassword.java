package com.example.newapp.request;

import lombok.Data;

@Data
public class ChangePassword {
    String email;
    String new_pass;
}
