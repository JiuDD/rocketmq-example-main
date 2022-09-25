package com.example.service;

import com.example.domain.dto.AccountChangeMsg;
import com.example.domain.entity.UserAccount;
import com.example.mapper.UserAccountMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * 参考：https://www.yuque.com/books/share/d9d6c020-67f9-457c-90da-6135ae1a2f5f/psgl53   密码：xbi8
 * 简单保证消息可靠投递。          发送消息, 跟MySQL的事务同步，效率较低，不能扛并发
 *
 *  将发送消息放在事务之后，如果消息发送失败，会抛出异常，事务自然就会回滚。看起来特别简便的方法，也能实现我们想要的效果。但是你是否注意到这两个坑呢?
 *  1.如果同时想发送两条消息，第一条成功，第二条失败。就没法保证两条消息间的事务性了。数据库哪怕回滚了，也已经发出了一条消息。
 *  ⒉.事务尚未提交，消息就发送出去，万一事务提交的请求卡了500ms，此时消费者先消费了消息，去反查发现并没有这条转账记录，导致事故。
 *  该方案其实是生产中常用的消息投递方案，适用于简单场景。复杂场景下，必须解决上述的两个问题
 */
public class Reliable1 {
    @Autowired
    MQProducer mqProducer;
    @Autowired
    UserAccountMapper userAccountMapper;

    /**
     * A给B转账 100元，转账后发送消息给B。
     * @param sUid
     * @param tUid
     * @param amount
     */
    @Transactional
    public void transfer(Long sUid,Long tUid,int amount){
        //给A-100
        userAccountMapper.addAmount(sUid,-amount);
        //给B+100
        userAccountMapper.addAmount(tUid,amount);
        //发送消息, 跟MySQL的事务同步，效率较低，不能扛并发
        mqProducer.send("topic",new AccountChangeMsg(sUid, tUid, amount));
    }

}
