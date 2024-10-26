package com.example.social_media_app_rtcomm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "tbl_message")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id1")
    private Long chatId1;
    @Column(name = "chat_id2")
    private Long chatId2;
    private Long groupChatId;
    private String message;
    private LocalDateTime createdAt;
    private Long senderId;
}
