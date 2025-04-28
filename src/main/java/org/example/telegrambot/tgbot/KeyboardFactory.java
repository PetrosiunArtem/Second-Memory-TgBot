package org.example.telegrambot.tgbot;

import lombok.RequiredArgsConstructor;
import org.example.telegrambot.service.UsersService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@RequiredArgsConstructor
public class KeyboardFactory {
  private final UsersService usersService;

  public static ReplyKeyboard getActionSelection() {
    KeyboardRow row = new KeyboardRow();
    row.add("files");
    row.add("subscribe");
    row.add("unsubscribe");
    row.add("stop");
    return new ReplyKeyboardMarkup(List.of(row));
  }

  public ReplyKeyboard getAllUsersKeyboard() {
    KeyboardRow row = new KeyboardRow();
    List<String> usersNames = usersService.getAllUsersNames();
    row.addAll(usersNames);
    return new ReplyKeyboardMarkup(List.of(row));
  }
}
