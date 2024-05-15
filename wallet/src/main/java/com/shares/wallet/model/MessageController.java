package com.shares.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageController {

    String message;
    Boolean succeeded;
}
