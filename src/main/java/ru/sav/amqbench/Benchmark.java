package ru.sav.amqbench;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;

@Component
public class Benchmark implements CommandLineRunner {
    private BenchmarkSettings benchmarkSettings;
    private PooledConnectionFactory connectionFactory;

    private final static Logger log = LoggerFactory.getLogger(Benchmark.class);

    public void run(String[] args) {
        List<Thread> pool = new ArrayList<>();
        ActiveMQQueue activeMQQueue = new ActiveMQQueue(benchmarkSettings.getQueue());

        Counter c = new Counter(benchmarkSettings.getN());

        log.info("START SEND");
        for (int i = 0; i < benchmarkSettings.getC(); i ++) {
            Thread t = new Thread(() -> {
                JmsQueueSender jmsQueueSender = new JmsQueueSender();
                jmsQueueSender.setQueue(activeMQQueue);
                jmsQueueSender.setConnectionFactory(connectionFactory);

//                for (int i1 = 0; i1 < benchmarkSettings.getN(); i1++) {
//                    jmsQueueSender.send(benchmarkSettings.getMessage());
//                }
                while (c.next()) {
                    jmsQueueSender.send(benchmarkSettings.getMessage());
                }
            });
            pool.add(t);
            t.start();
        }

        boolean alive = true;
        while (alive) {
            alive = false;
            for (Thread t: pool) {
                if (t.isAlive()) {
                    alive = true;
                }
            }
        }

        pool.clear();
        c.flush();

        log.info("START RECEIVE");
        for (int i = 0; i < benchmarkSettings.getC(); i ++) {
            Thread t = new Thread(() -> {
                JmsQueueReceiver jmsQueueReceiver = new JmsQueueReceiver();
                jmsQueueReceiver.setConnectionFactory(connectionFactory);
                jmsQueueReceiver.setQueue(activeMQQueue);
                String text = null;
                do {
                    text = jmsQueueReceiver.receive();
                    log.info(text);
                } while (text != null);
            });
            pool.add(t);
            t.start();
        }

        alive = true;
        while (alive) {
            alive = false;
            for (Thread t: pool) {
                if (t.isAlive()) {
                    alive = true;
                }
            }
        }

        log.info("END RECEIVE");
        pool.clear();
        connectionFactory.stop();
    }

    @Autowired
    public void setBenchmarkSettings(BenchmarkSettings benchmarkSettings) {
        this.benchmarkSettings = benchmarkSettings;
    }

    @Autowired
    public void setConnectionFactory(PooledConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private class Counter {
        private long i = 0;
        private long n = 0;

        Counter(long n) {
            this.n = n;
        }

        public synchronized boolean done() {
            return i >= n;
        }

        public synchronized boolean next() {
            if (i >= n) {
                return false;
            } else {
                i++;
                return true;
            }
        }

        public void setN(long n) {
            this.n = n;
        }

        public void flush() {
            i = 0;
        }
    }
}
