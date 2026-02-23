package managers;

import filters.UserFilter;
import models.User;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> storage;

    public UserManager() {
        this.storage = new HashMap<>();
    }

    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (storage.containsKey(user.username())) {
            throw new IllegalArgumentException("Пользователь с именем '" + user.username() + "' уже существует");
        }
        storage.put(user.username(), user);
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;
        return storage.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(u -> u.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) return findAll();
        return storage.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Сортировка не может быть null");
        }
        return storage.values().stream()
                .filter(filter != null ? filter::test : u -> true)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        return storage.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        if (!storage.containsKey(username)) {
            throw new IllegalArgumentException("Пользователь '" + username + "' не найден");
        }
        User updatedUser = User.validate(username, newFullName, newEmail);
        storage.put(username, updatedUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(storage, that.storage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storage);
    }
}