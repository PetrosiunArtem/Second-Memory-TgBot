package org.example.telegrambot.tgbot;

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
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.BiConsumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class SecondMemoryBot extends AbilityBot {

  private final ResponseHandler responseHandler;

  @Value("${BOT_TOKEN}")
  private String token;

  public SecondMemoryBot(
      @Value("${BOT_TOKEN}") String token,
      UsersService usersService,
      FilesService filesService,
      FollowersService followersService) {
    super(token, "Second Memory");
    responseHandler = new ResponseHandler(silent, db, usersService, filesService, followersService);
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
        (abilityBot, upd) -> responseHandler.replyToButtons(getChatId(upd), upd.getMessage());
    return Reply.of(action, Flag.TEXT, upd -> responseHandler.userIsActive(getChatId(upd)));
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
