package org.example.telegrambot.tgbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.example.telegrambot.dto.MessageForPagination;
import org.example.telegrambot.repository.UsersRepository;
import org.example.telegrambot.service.FilesService;
import org.example.telegrambot.service.FollowersService;
import org.example.telegrambot.service.UsersService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class SecondMemoryBot extends AbilityBot {

  @Getter private final ResponseHandler responseHandler;
  private final ObjectMapper objectMapper;
  private final Map<Long, PaginationState> paginationStates;
  private final UsersRepository usersRepository;
  private static final int SKIP_SIZE = 4;

  @Value("${BOT_TOKEN}")
  private String token;

  public SecondMemoryBot(
      @Value("${BOT_TOKEN}") String token,
      UsersService usersService,
      FilesService filesService,
      FollowersService followersService,
      ObjectMapper objectMapper,
      UsersRepository usersRepository) {
    super(token, "Second Memory");
    this.paginationStates = db.getMap(Constants.PAGINATION_STATES);
    this.usersRepository = usersRepository;
    responseHandler =
        new ResponseHandler(
            silent,
            db,
            paginationStates,
            usersService,
            filesService,
            followersService,
            usersRepository);
    this.objectMapper = objectMapper;
  }

  public Ability startBot() {
    return Ability.builder()
        .name("start")
        .info(Constants.START_DESCRIPTION)
        .locality(USER)
        .privacy(PUBLIC)
        .action(ctx -> responseHandler.replyToStart(ctx.chatId()))
        .build();
  }

  public Reply replyToButtons() {
    BiConsumer<BaseAbilityBot, Update> action =
        (abilityBot, upd) -> responseHandler.replyToButtons(getChatId(upd), upd);
    return Reply.of(action, Flag.TEXT, upd -> responseHandler.userIsActive(getChatId(upd)));
  }

  //  @SneakyThrows
  @SneakyThrows
  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasCallbackQuery()) {
      String[] request = update.getCallbackQuery().getData().split("_");
      long chatId = update.getCallbackQuery().getMessage().getChatId();
      if (request[0].equals("unseen")) {
        DeleteMessage deleteMessage =
            DeleteMessage.builder()
                .chatId(chatId)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build();
        sender.execute(deleteMessage);
        responseHandler.goBackToActionSelection(chatId);
      } else if (request[0].contains("pagination")) {
        MessageForPagination message =
            objectMapper.readValue(request[0], MessageForPagination.class);
        int count = message.countPage();
        int page = message.numberPage();
        String text;
        if (!paginationStates.containsKey(chatId)) {
          paginationStates.put(chatId, PaginationState.DEFAULT);
        }
        switch (paginationStates.get(chatId)) {
          case ALL_USERS -> text = getAllUsers(page);
          case ALL_USERS_WITH_CHAT_ID -> text = getAllUsersWithByChatId(chatId, page);
          case ALL_USERS_WITHOUT_CHAT_ID -> text = getAllUsersWithoutByChatId(chatId, page);
          default -> text = "";
        }
        InlineKeyboardMarkup markup = KeyboardFactory.getFirstInlineKeyboard(page, count);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(markup);
        editMessageText.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        sender.execute(editMessageText);
      }
    } else if (update.hasMessage() && update.getMessage().hasText()) {
      long chatId = update.getMessage().getChatId();
      if (update.getMessage().getText().equals("/start")) {
        responseHandler.replyToStart(chatId);
      } else {
        responseHandler.replyToButtons(chatId, update);
      }
    }
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

  @Override
  public long creatorId() {
    return 1L;
  }

  @Override
  public String getBotToken() {
    return token;
  }
}
