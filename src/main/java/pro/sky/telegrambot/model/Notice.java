package pro.sky.telegrambot.model;

import liquibase.pro.packaged.S;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "chat_id")
    Long chatId;
    String text;
    String firstname;
    @Column(name = "notice_timestamp")
    LocalDateTime noticeTimestamp;

    public Notice() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public LocalDateTime getNoticeTimestamp() {
        return noticeTimestamp;
    }

    public void setNoticeTimestamp(LocalDateTime noticeTimestamp) {
        this.noticeTimestamp = noticeTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notice notice = (Notice) o;
        return Objects.equals(id, notice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notice{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", text='" + text + '\'' +
                ", firstname='" + firstname + '\'' +
                ", noticeTimestamp=" + noticeTimestamp +
                '}';
    }
}
