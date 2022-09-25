package com.example.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AccountChangeRecord {
    //发送方
    private Long sUid;
    //收款人
    private Long tUid;
    //收款金额
    private int mount;
    //事务id
    private String transactionId;
}
