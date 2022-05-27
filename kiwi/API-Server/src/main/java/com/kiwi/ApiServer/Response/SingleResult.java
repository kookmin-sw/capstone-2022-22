package com.kiwi.ApiServer.Response;

import lombok.Getter;
import lombok.Setter;

public class SingleResult<T> extends Result {
    @Getter
    @Setter
    private T data;
}
