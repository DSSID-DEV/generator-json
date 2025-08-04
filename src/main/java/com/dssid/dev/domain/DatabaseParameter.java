package com.dssid.dev.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class DatabaseParameter {
    private String dbType;
    private String url;
    private String username;
    private String password;
}
