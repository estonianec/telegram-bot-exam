package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Notice;
import pro.sky.telegrambot.repository.NoticeRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NoticeRepository noticeRepository;

    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String msg = update.message().text();
                if (msg.equals("/start")) {
                    String msgToSend = "Welcome to estonianec tg bot, " + update.message().from().firstName() +
                            "! \uD83D\uDC40\n\nЭтот бот умеет запоминать дела за Вас и напоминать Вам о них, когда " +
                            "необходимо. \nДля использования бота по назначению, отправьте ему сообщение вида " +
                            "\"01.01.2022 20:00 Сделать домашнюю работу\"";
                    SendMessage request = new SendMessage(chatId, msgToSend);
                    telegramBot.execute(request);
                    String imageFile = "https://c.tenor.com/ztDMimXwhsAAAAAi/hello.gif";
                    SendSticker sendSticker = new SendSticker(update.message().chat().id(), imageFile);
                    telegramBot.execute(sendSticker);
                } else {
                    Pattern dataPattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
                    Matcher dataMatcher = dataPattern.matcher(msg);
                    if (dataMatcher.matches()) {
                        String data = dataMatcher.group(1);
                        String item = dataMatcher.group(3);
                        String msgToSend = "Напоминание \"" + item + "\" создано и сработает " + data;
                        SendMessage request = new SendMessage(chatId, msgToSend)
                                .replyToMessageId(update.message().messageId());
                        telegramBot.execute(request);
                        Notice notice = new Notice();
                        notice.setChatId(chatId);
                        notice.setFirstname(update.message().from().firstName());
                        notice.setText(item);
                        LocalDateTime noticeTimeStamp = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        notice.setNoticeTimestamp(noticeTimeStamp);
                        noticeRepository.save(notice);
                    } else {
                        String msgToSend = "Извините, я вас не понял. \uD83D\uDE44Напишите, пожалуйста, напоминалку в формате " +
                                "\"01.01.2022 20:00 Сделать домашнюю работу\", либо отправьте сообщение /start.";
                        SendMessage request = new SendMessage(chatId, msgToSend)
                                .replyToMessageId(update.message().messageId());
                        telegramBot.execute(request);
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotices() {
        logger.info("Checking actual notices...");
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Notice> listOfNotices = noticeRepository.getNoticeByNoticeTimestampEquals(currentTime);
        long chatId;
        String item;
        String firstName;
        String msg;
        if (!listOfNotices.isEmpty()) {
            logger.info("Notices to send " + listOfNotices.size());
            for (Notice listOfNotice : listOfNotices) {
                chatId = listOfNotice.getChatId();
                item = listOfNotice.getText();
                firstName = listOfNotice.getFirstname();
                msg = "Доброго времени суток, " + firstName + "! Вы просили напомнить о событии \"" + item + "\".";
                SendMessage request = new SendMessage(chatId, msg);
                telegramBot.execute(request);
                logger.info("Notice was send to " + firstName);
            }
        }
    }
}
