package com.example.config;

import com.example.service.MQProducer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;

/**
 * Description: 消息入库投递，重写事务管理器的commit方法，在commit成功之后，发送消息
 *  我们已经将所有待发送的消息都存入了数据库，并在线程threadLoacl中存入消息id，接下来，需要实现我们自己的
 *  事务管理器，做到事务提交后统一取出threadLocal的消息id，进行发送。
 */
@Component
public class MyTransactionManager extends DataSourceTransactionManager {
    @Autowired
    MQProducer mqProducer;

    public MyTransactionManager(DataSource dataSource){
        super(dataSource);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        try {
            super.doCommit(status);
        }finally {
            //事务提交后
            sendMQ();
        }
    }

    //MySQL事务commit之后调用该方法发送本地ThreadLocal的message
    private void sendMQ() {
        try {
            //发送ThreadLocal内所有待发送的mq
            mqProducer.sendAllThreadLocalMsg();
        } catch (Exception e) {
            //如果发送消息异常，可自定义重试机制
            e.printStackTrace();
        }
    }
}
