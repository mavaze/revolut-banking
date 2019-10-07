package io.github.mavaze.revolut.corebank.exceptions;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ErrorMessage {
    @NonNull private String code;
    @NonNull private String message;
    private int status;
}
