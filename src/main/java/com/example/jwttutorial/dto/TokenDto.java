package com.example.jwttutorial.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TokenDto {

    private final String token;
}
