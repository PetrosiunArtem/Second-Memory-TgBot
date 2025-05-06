package org.example.telegrambot.tgbot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
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

  public static InlineKeyboardMarkup getFirstInlineKeyboard(int page, int count) {
    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
    InlineKeyboardButton backPage = new InlineKeyboardButton();
    backPage.setText("Назад");
    String callback1 =
        String.format(
            "{\"method\":\"pagination\",\"numberPage\":\"%d\",\"countPage\":\"%d\"}",
            page - 1, count);
    backPage.setCallbackData(callback1);

    InlineKeyboardButton currentPage = new InlineKeyboardButton();
    currentPage.setText(String.format("%d/%d", page, count));
    currentPage.setCallbackData(" ");

    InlineKeyboardButton nextPage = new InlineKeyboardButton();
    nextPage.setText("Вперёд");
    String callback2 =
        String.format(
            "{\"method\":\"pagination\",\"numberPage\":\"%d\",\"countPage\":\"%d\"}",
            page + 1, count);
    nextPage.setCallbackData(callback2);

    if (page == 1 && count == 1) {
      rowInline1.add(currentPage);
    } else if (page == 1) {
      rowInline1.add(currentPage);
      rowInline1.add(nextPage);
    } else if (page == count) {
      rowInline1.add(backPage);
      rowInline1.add(currentPage);
    } else {
      rowInline1.add(backPage);
      rowInline1.add(currentPage);
      rowInline1.add(nextPage);
    }

    List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
    InlineKeyboardButton unseen = new InlineKeyboardButton();
    unseen.setText("Скрыть");
    unseen.setCallbackData("unseen");

    rowInline2.add(unseen);

    rowsInline.add(rowInline1);
    rowsInline.add(rowInline2);
    markupInline.setKeyboard(rowsInline);
    return markupInline;
  }

  private KeyboardFactory() {}
}
