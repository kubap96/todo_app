package com.training.utils;

public class TestTodoUtils {

    public static final String TODOS_URL = "/todos";

    public static String todosUrlWithId(long id) {
        return TODOS_URL + "/" + id;
    }
}