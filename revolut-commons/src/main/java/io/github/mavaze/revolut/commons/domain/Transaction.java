package io.github.mavaze.revolut.commons.domain;

import lombok.*;

import java.util.Date;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Comparable<Transaction>, Cloneable {

    private Integer transactionId;

    private String referenceId;

    private String involvedAccount;

    private Long amount;

    private TransactionStatus status;

    private String description;

    private Date transactionDate;

    @Override
    public int compareTo(@NonNull final Transaction tx) {
        return tx.transactionDate.compareTo(this.transactionDate);
    }

    @Override
    public Object clone() {
        Transaction tx = null;
        try {
            tx = (Transaction) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        return tx;
    }
}
