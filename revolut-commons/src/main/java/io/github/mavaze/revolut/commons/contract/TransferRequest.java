package io.github.mavaze.revolut.commons.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest implements Serializable {

    private static final long serialVersionUID = -2056675742437734076L;

    private String sender;

    private String receiver;

    private String description;

    private Long amount;

    // Schedule feature is added here only for test demonstration.
    //private LocalDateTime scheduledAt = LocalDateTime.now();
}
