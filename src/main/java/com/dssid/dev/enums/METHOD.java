package com.dssid.dev.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum METHOD {
    POST("POST", 0),
    PUT("PUT", 1),
    GET("GET", 2),
    DELETE("DELETE", 3),
    PATCH("PATCH", 4);

    private String value;
    private int index;

}
