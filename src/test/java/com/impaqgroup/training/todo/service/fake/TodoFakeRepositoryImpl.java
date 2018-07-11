//package com.impaqgroup.training.todo.service.fake;
//
//import com.impaqgroup.training.todo.domain.Todo;
//import com.impaqgroup.training.todo.persistance.TodoJpaRepository;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class TodoFakeRepositoryImpl implements TodoJpaRepository {
//
//    private final Map<Long, Todo> todoMap = new HashMap<>();
//    private static Long lastId = 0L;
//
//    @Override
//    public List<Todo> findByName(String name) {
//        return todoMap.values().stream()
//                .filter( t -> name.equals(t.getName()))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Todo> findByPriority(String priority) {
//        return null;
//    }
//
//    @Override
//    public List<Todo> findByNameAndPriority(String name, String priority) {
//        return null;
//    }
//
//    @Override
//    public Todo save(Todo todo) {
//        lastId++;
//        todo.setId(lastId);
//        todoMap.put(lastId, todo);
//        return todo;
//    }
//
//    @Override
//    public <T extends Todo> Iterable<T> save(Iterable<T> todos) {
//        return null;
//    }
//
//    @Override
//    public Todo findOne(Long aLong) {
//        return null;
//    }
//
//    @Override
//    public boolean exists(Long aLong) {
//        return false;
//    }
//
//    @Override
//    public Iterable<Todo> findAll() {
//        return todoMap.values();
//    }
//
//    @Override
//    public Iterable<Todo> findAll(Iterable<Long> iterable) {
//        return null;
//    }
//
//    @Override
//    public long count() {
//        return 0;
//    }
//
//    @Override
//    public void delete(Long aLong) {
//
//    }
//
//    @Override
//    public void delete(Todo todo) {
//
//    }
//
//    @Override
//    public void delete(Iterable<? extends Todo> iterable) {
//
//    }
//
//    @Override
//    public void deleteAll() {
//
//    }
//}
