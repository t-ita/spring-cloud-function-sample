package com.myexample.serverless.functions;

import java.util.function.Function;

public class CharCounter implements Function<String, Integer> {

    @Override
    public Integer apply(String s) {
        return s.length();
    }

}
