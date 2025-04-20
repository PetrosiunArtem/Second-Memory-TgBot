package org.example.telegrambot.dto;

import java.util.List;

public record MessageToUsers(List<Long> chatIds, String message) {}
