package ru.sav.amqbench;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;

public class JmsQueueSender {
    private JmsTemplate jmsTemplate;
    private Queue queue;

    public void send(final String message) {
        this.jmsTemplate.send(this.queue, session -> {
            return session.createTextMessage(message);
        });
    }

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}
