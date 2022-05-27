package com.kiwi.ApiServer.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MultiResult<T> extends Result {

    @Getter
    @Setter
    private List<T> data;
}
