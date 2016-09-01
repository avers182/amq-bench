package ru.sav.amqbench;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;

public class JmsQueueReceiver {
    private JmsTemplate jmsTemplate;
    private Queue queue;

    public String receive() {
        String text = null;
        Message message = this.jmsTemplate.receive(queue);
        if (message instanceof TextMessage) {
            try {
                text = ((TextMessage) message).getText();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
        this.jmsTemplate.setReceiveTimeout(1000);
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}
