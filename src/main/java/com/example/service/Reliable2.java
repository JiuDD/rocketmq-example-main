package com.example.service;

import com.example.domain.dto.AccountChangeMsg;
import com.example.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 参考：https://www.yuque.com/books/share/d9d6c020-67f9-457c-90da-6135ae1a2f5f/psgl53   密码：xbi8
 * 消息入库投递，来保证消息可靠
 *  消息投递的可靠性之所以难保证。在于消息和数据库并非同源，无法保证事务性。我们是不是可以先让消息记录入库，然后再去用最终一致性来保证两者的事务。
 *  该方案已经覆盖了绝大多数的场景，所有入库的消息，最终发送完毕后都会被删除，我们还可以起个定时任务，定期的去发送那些失败的mq，实现最终─致性。
 */
public class Reliable2 {
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
        //事务结束后发送消息（MyTransactionManager里重写了commit方法，在commit成功之后发送message）
        mqProducer.sendAfterCommit("topic",new AccountChangeMsg(sUid, tUid, amount));
    }

}
