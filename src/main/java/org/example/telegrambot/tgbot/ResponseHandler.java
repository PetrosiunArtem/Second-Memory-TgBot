package org.example.telegrambot.tgbot;

import org.example.telegrambot.entity.UserEntity;
import org.example.telegrambot.repository.UsersRepository;
import org.example.telegrambot.service.FilesService;
import org.example.telegrambot.service.FollowersService;
import org.example.telegrambot.service.UsersService;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
  private final Map<Long, PaginationState> paginationStates;
  private final UsersService usersService;
  private final FilesService filesService;
  private final FollowersService followersService;
  private final UsersRepository usersRepository;
  private static final int SKIP_SIZE = 4;

  public ResponseHandler(
      SilentSender sender,
      DBContext db,
      Map<Long, PaginationState> paginationStates,
      UsersService usersService,
      FilesService filesService,
      FollowersService followersService,
      UsersRepository usersRepository) {
    this.sender = sender;
    this.chatStates = db.getMap(Constants.CHAT_STATES);
    this.paginationStates = paginationStates;
    this.usersService = usersService;
    this.filesService = filesService;
    this.followersService = followersService;
    this.usersRepository = usersRepository;
  }

  public void replyToStart(Long chatId) {
    SendMessage message = new SendMessage();
    followersService.save(chatId);
    message.setChatId(chatId.toString());
    message.setText(START_TEXT);
    sender.execute(message);
    paginationStates.put(chatId, PaginationState.DEFAULT);
    chatStates.put(chatId, AWAITING_NAME);
  }

  public void replyToButtons(Long chatId, Update update) {
    Message message = update.getMessage();
    if (message.getText().equalsIgnoreCase("/stop")) {
      stopChat(chatId);
    }
    if (!chatStates.containsKey(chatId)) {
      return;
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
    sendMessage.setText("Choose the user");
    if ("files".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      chatStates.put(chatId, ALL_USERS_SELECTION);
      paginationStates.put(chatId, PaginationState.ALL_USERS);
      getAllUsersWithPagination(chatId, 1);
    } else if ("subscribe".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      paginationStates.put(chatId, PaginationState.ALL_USERS_WITHOUT_CHAT_ID);
      chatStates.put(chatId, ALL_USERS_SELECTION_FROM_TOPICS);
      getAllUsersWithoutByChatIdWithPagination(chatId, 1);
    } else if ("unsubscribe".equalsIgnoreCase(text)) {
      sender.execute(sendMessage);
      paginationStates.put(chatId, PaginationState.ALL_USERS_WITH_CHAT_ID);
      chatStates.put(chatId, ALL_USERS_SELECTION_NOT_OF_THE_TOPICS);
      getAllUsersWithByChatIdWithPagination(chatId, 1);
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
    int page = 1;
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
        getAllUsersWithPagination(chatId, page);
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
      getAllUsersWithPagination(chatId, page);
    }
  }

  private void replyToAllUsersSelectionFromTopics(Long chatId, Message message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    List<Long> myUsers = usersService.getAllUsersWithoutChatId(chatId);
    int page = 1;
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
      getAllUsersWithoutByChatIdWithPagination(chatId, page);
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
    int page = 1;
    if (followers.isEmpty()) {
      sendMessage.setText("There are no people you can unsubscribe to");
      sendMessage.setReplyMarkup(KeyboardFactory.getActionSelection());
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
      getAllUsersWithByChatIdWithPagination(chatId, page);
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
        "Thank you for joining us. See you soon! Click /start to find the files in our service again.");
    chatStates.remove(chatId);
    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
    sender.execute(sendMessage);
  }

  private void getAllUsersWithPagination(long chatId, int page) {
    int count = Math.max(1, (usersRepository.countAllUsers() + SKIP_SIZE - 1) / SKIP_SIZE);
    SendMessage sendMessage = getSendMessage(chatId, getAllUsers(page), page, count);
    sender.execute(sendMessage);
  }

  private void getAllUsersWithoutByChatIdWithPagination(long chatId, int page) {
    int count =
        Math.max(
            1, (usersRepository.countAllUsersWithoutByChatId(chatId) + SKIP_SIZE - 1) / SKIP_SIZE);
    SendMessage sendMessage =
        getSendMessage(chatId, getAllUsersWithoutByChatId(chatId, page), page, count);
    sender.execute(sendMessage);
  }

  private void getAllUsersWithByChatIdWithPagination(long chatId, int page) {
    int count =
        Math.max(
            1, (usersRepository.countAllUsersWithByChatId(chatId) + SKIP_SIZE - 1) / SKIP_SIZE);
    SendMessage sendMessage =
        getSendMessage(chatId, getAllUsersWithByChatId(chatId, page), page, count);
    sender.execute(sendMessage);
  }

  private SendMessage getSendMessage(long chatId, String message, int page, int count) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText(message);
    sendMessage.setReplyMarkup(KeyboardFactory.getFirstInlineKeyboard(page, count));
    return sendMessage;
  }

  private String getAllUsers(int page) {
    List<String> names = usersRepository.findAllUsersWithPagination(page, SKIP_SIZE);

    return readNames(names);
  }

  private String getAllUsersWithByChatId(long chatId, int page) {
    List<String> names =
        usersRepository.findAllUsersWithByChatIdWithPagination(chatId, page, SKIP_SIZE);
    return readNames(names);
  }

  private String getAllUsersWithoutByChatId(long chatId, int page) {
    List<String> names =
        usersRepository.findAllUsersWithoutByChatIdWithPagination(chatId, page, SKIP_SIZE);
    return readNames(names);
  }

  private static String readNames(List<String> names) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String name : names) {
      stringBuilder.append(name);
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  public void goBackToActionSelection(Long chatId) {
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
        "Hello " + message.getText() + ". Choose actions",
        KeyboardFactory.getActionSelection(),
        TO_DO_SELECTION);
  }

  public boolean userIsActive(Long chatId) {
    return chatStates.containsKey(chatId);
  }
}
