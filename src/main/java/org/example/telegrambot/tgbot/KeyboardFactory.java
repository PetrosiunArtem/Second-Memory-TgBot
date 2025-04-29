package org.example.telegrambot.tgbot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.List;

public class KeyboardFactory {

  public static ReplyKeyboard getActionSelection() {
    KeyboardRow row = new KeyboardRow();
    row.add("files");
    row.add("subscribe");
    row.add("unsubscribe");
    row.add("stop");
    return new ReplyKeyboardMarkup(List.of(row));
  }

  private KeyboardFactory() {}
}
