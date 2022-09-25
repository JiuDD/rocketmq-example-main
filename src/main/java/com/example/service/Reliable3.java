package com.example.service;

import com.example.domain.dto.AccountChangeMsg;
import com.example.domain.entity.AccountChangeRecord;
import com.example.mapper.AccountChangeRecordMapper;
import com.example.mapper.UserAccountMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发送事务消息来保证消息可靠
 * 参考：https://www.yuque.com/books/share/d9d6c020-67f9-457c-90da-6135ae1a2f5f/psgl53   密码：xbi8
 */
public class Reliable3 {
    @Autowired
    RocketMQTemplate rocketMQTemplate;
    @Autowired
    UserAccountMapper userAccountMapper;
    @Autowired
    AccountChangeRecordMapper accountChangeRecordMapper;

    /**
     * A给B转账 100元，转账后发送消息给B。
     * @param sUid
     * @param tUid
     * @param amount
     */
    public void transfer(Long sUid,Long tUid,int amount){
        //发送事务消息
        rocketMQTemplate.sendMessageInTransaction("tx_order","topic", MessageBuilder.withPayload(new AccountChangeMsg(sUid, tUid, amount)).build(),null);
    }

    @Transactional
    public void executeLocalTransaction(AccountChangeMsg accountChangeMsg, String transactionId) {
        //给A-100
        userAccountMapper.addAmount(accountChangeMsg.getSUid(),-accountChangeMsg.getMount());
        //给B+100
        userAccountMapper.addAmount(accountChangeMsg.getTUid(),accountChangeMsg.getMount());
        //生成一条转账记录
        AccountChangeRecord record = new AccountChangeRecord()
                .setSUid(accountChangeMsg.getSUid()).setTUid(accountChangeMsg.getTUid()).setMount(accountChangeMsg.getMount()).setTransactionId(transactionId);
        accountChangeRecordMapper.insert(record);
    }
}
