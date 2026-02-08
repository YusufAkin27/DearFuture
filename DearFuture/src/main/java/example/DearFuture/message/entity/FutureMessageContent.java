package example.DearFuture.message.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "future_message_contents")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FutureMessageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    /** Metin içeriği. length ile VARCHAR eşlemesi; LOB/Clob stream hatası olmaz. */
    @Column(name = "text_content", length = 10485760)
    private String textContent;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "future_message_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_content_future_message")
    )
    private FutureMessage futureMessage;
}
