package com.kiwi.ApiServer.DTO;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

@Alias("refreshToken")
@Getter
@Setter
public class RefreshToken {
    private String refresh_token;
    private String user_id;
}
