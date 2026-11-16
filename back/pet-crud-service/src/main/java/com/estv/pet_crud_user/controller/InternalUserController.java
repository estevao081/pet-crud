package com.estv.pet_crud_user.controller;

import com.estv.pet_crud_user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;


    public InternalUserController(UserService userService) {
        this.userService = userService;
    }


}
