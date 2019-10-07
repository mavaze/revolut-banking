package io.github.mavaze.revolut.commons.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TxMessage implements Routable<String>, Identifiable<Integer>, Serializable {

    private static final long serialVersionUID = 6114838538088187899L;

    private String sender;

    private Transaction transaction;

    @Override
    public String getReceiver() {
        return transaction.getInvolvedAccount();
    }

    @Override
    public Integer getId() {
        return transaction.getTransactionId();
    }
}
