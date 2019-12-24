package com.revolut.hometask.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResponseStatusEnum {

    SUCCESS ("Success"),
    ERROR ("Error");

    @Getter
    private final String name;

}
