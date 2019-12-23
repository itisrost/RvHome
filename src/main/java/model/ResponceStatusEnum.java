package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResponceStatusEnum {

    SUCCESS ("Success"),
    ERROR ("Error");

    @Getter
    private final String name;

}
