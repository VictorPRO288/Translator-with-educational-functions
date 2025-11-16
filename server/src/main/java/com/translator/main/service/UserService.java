package com.translator.main.service;

import com.translator.main.model.Translation;
import com.translator.main.model.User;
import com.translator.main.repository.TranslationRepository;
import com.translator.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TranslationRepository translationRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }

    public void addTranslationToUser(Integer userId, Integer translationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Translation translation = translationRepository.findById(translationId)
                .orElseThrow(() -> new RuntimeException("Перевод не найден"));

        user.getTranslations().add(translation);
        translation.getUsers().add(user);

        userRepository.save(user);
        translationRepository.save(translation);
    }

    public void removeTranslationFromUser(Integer userId, Integer translationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Translation translation = translationRepository.findById(translationId)
                .orElseThrow(() -> new RuntimeException("Перевод не найден"));

        user.getTranslations().remove(translation);
        translation.getUsers().remove(user);

        userRepository.save(user);
        translationRepository.save(translation);
    }
}
