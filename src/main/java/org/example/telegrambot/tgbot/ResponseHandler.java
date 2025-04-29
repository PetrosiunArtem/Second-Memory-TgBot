package org.example.telegrambot.tgbot;

import org.example.telegrambot.entity.UserEntity;
import org.example.telegrambot.service.FilesService;
import org.example.telegrambot.service.FollowersService;
import org.example.telegrambot.service.UsersService;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.telegrambot.tgbot.Constants.START_TEXT;
import static org.example.telegrambot.tgbot.UserState.ALL_USERS_SELECTION;
import static org.example.telegrambot.tgbot.UserState.ALL_USERS_SELECTION_FROM_TOPICS;
import static org.example.telegrambot.tgbot.UserState.ALL_USERS_SELECTION_NOT_OF_THE_TOPICS;
import static org.example.telegrambot.tgbot.UserState.AWAITING_NAME;
import static org.example.telegrambot.tgbot.UserState.TO_DO_SELECTION;

public class ResponseHandler {
  private final SilentSender sender;
  private final Map<Long, UserState> chatStates;
  private final UsersService usersService;
  private final FilesService filesService;
  private final FollowersService followersService;

  public ResponseHandler(
      SilentSender sender,
      DBContext db,
      UsersService usersService,
      FilesService filesService,
      FollowersService followersService) {
    this.sender = sender;
    this.chatStates = db.getMap(Constants.CHAT_STATES);
    this.usersService = usersService;
    this.filesService = filesService;
    this.followersService = followersService;
  }

  public void replyToStart(Long chatId) {
    SendMessage message = new SendMessage();
    followersService.save(chatId);
    message.setChatId(chatId.toString());
    message.setText(START_TEXT);
    sender.execute(message);
    chatStates.put(chatId, AWAITING_NAME);
  }

  public void replyToButtons(Long chatId, Message message) {
    if (message.getText().equalsIgnoreCase("/stop")) {
      stopChat(chatId);
    }

    switch (chatStates.get(chatId)) {
      case AWAITING_NAME -> replyToName(chatId, message);
      case TO_DO_SELECTION -> replyToDoSelection(chatId, message);
      case ALL_USERS_SELECTION -> replyToAllUsersSelection(chatId, message);
      case ALL_USERS_SELECTION_FROM_TOPICS -> replyToAllUsersSelectionFromTopics(chatId, message);
      case ALL_USERS_SELECTION_NOT_OF_THE_TOPICS ->
          replyToAllUsersSelectionNotOfTheTopics(chatId, message);
      default -> unexpectedMessage(chatId);
    }
  }

  private void replyToDoSelection(Long chatId, Message message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    String text = message.getText();
    sendMessage.setText("Chose the user");
    if ("files".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      chatStates.put(chatId, ALL_USERS_SELECTION);
    } else if ("subscribe".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      chatStates.put(chatId, ALL_USERS_SELECTION_FROM_TOPICS);
    } else if ("unsubscribe".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      chatStates.put(chatId, ALL_USERS_SELECTION_NOT_OF_THE_TOPICS);
    } else if ("stop".equalsIgnoreCase(text)) {
      stopChat(chatId);
    } else {
      sendMessage.setText("Please select files, subscribe, unsubscribe or stop");
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
      sender.execute(sendMessage);
    }
  }

  public void push(Long chatId, String message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(message);
    sender.execute(sendMessage);
  }

  private void replyToAllUsersSelection(Long chatId, Message message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    List<String> names = usersService.getAllUsersNames();
    if (names.isEmpty()) {
      sendMessage.setText("There are no people you can check files to");
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
      return;
    }
    if ("return".equalsIgnoreCase(message.getText())) {
      goBackToActionSelection(chatId);
    } else if (names.contains(message.getText())) {
      Optional<UserEntity> user = usersService.getUserByName(message.getText());
      StringBuilder stringBuilder = new StringBuilder();
      if (user.isEmpty()) {
        sendMessage.setText(
            "Please select one person from the list or write 'return' for go back to the chat");
        sender.execute(sendMessage);
        return;
      }
      List<String> filesNames = filesService.getAllFilesKeysWithOwnerId(user.get().getId());
      for (String name : filesNames) {
        stringBuilder.append(name);
        stringBuilder.append("\n");
      }
      sendMessage.setText(stringBuilder.toString());
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
    } else {
      sendMessage.setText(
          "Please select one person from the list or write 'return' for go back to the chat");
      sender.execute(sendMessage);
    }
  }

  private void replyToAllUsersSelectionFromTopics(Long chatId, Message message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    List<Long> myUsers = usersService.getAllUsersWithoutChatId(chatId);
    if (myUsers.isEmpty()) {
      sendMessage.setText("There are no people you can subscribe to");
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection()); // Допилить
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
      return;
    }
    if ("return".equalsIgnoreCase(message.getText())) {
      goBackToActionSelection(chatId);
      return;
    }

    Optional<UserEntity> user = usersService.getUserByName(message.getText());
    if (user.isEmpty() || !myUsers.contains(user.get().getId())) {
      sendMessage.setText(
          "Please select one person from the list or write 'return' for go back to the chat");
      sender.execute(sendMessage);
    } else {
      followersService.subscribeToUser(chatId, user.get().getId());
      sendMessage.setText("You have been subscribed to " + user.get().getName());
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
    }
  }

  private void replyToAllUsersSelectionNotOfTheTopics(Long chatId, Message message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    List<Long> followers = followersService.getAllUsersIdsWithChatId(chatId);
    if (followers.isEmpty()) {
      sendMessage.setText("There are no people you can unsubscribe to");
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection()); // Допилить
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
      return;
    }
    if ("return".equalsIgnoreCase(message.getText())) {
      goBackToActionSelection(chatId);
      return;
    }
    Optional<UserEntity> user = usersService.getUserByName(message.getText());
    if (user.isEmpty() || !followers.contains(user.get().getId())) {
      sendMessage.setText(
          "Please select one person from the list or write 'return' for go back to the chat");
      sender.execute(sendMessage);
    } else if (followers.contains(user.get().getId())) {
      followersService.unsubscribeFromUser(chatId, user.get().getId());
      sendMessage.setText("You have been unsubscribed to " + user.get().getName());
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
      sender.execute(sendMessage);
      chatStates.put(chatId, TO_DO_SELECTION);
    }
  }

  private void unexpectedMessage(Long chatId) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText("I did not expect that.");
    sender.execute(sendMessage);
  }

  private void stopChat(Long chatId) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(
        "Thank you for your questions. See you soon!\nPress /start to find files in our service again");
    chatStates.remove(chatId);
    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
    sender.execute(sendMessage);
  }

  private void goBackToActionSelection(Long chatId) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText("You was returned to the chat");
    sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
    sender.execute(sendMessage);
    chatStates.put(chatId, TO_DO_SELECTION);
  }

  private void promptWithKeyboardForState(
      Long chatId, String text, ReplyKeyboard keyBoardYesOrNo, UserState awaitingReorder) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    sendMessage.setText(text);
    sendMessage.setReplyMarkup(keyBoardYesOrNo);
    sender.execute(sendMessage);
    chatStates.put(chatId, awaitingReorder);
  }

  private void replyToName(long chatId, Message message) {
    promptWithKeyboardForState(
        chatId,
        "Hello " + message.getText() + ". What do yo do?",
        KeyboardFactory.getActionSelection(),
        TO_DO_SELECTION);
  }

  public boolean userIsActive(Long chatId) {
    return chatStates.containsKey(chatId);
  }
}
