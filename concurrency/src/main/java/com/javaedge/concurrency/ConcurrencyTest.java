package com.javaedge.concurrency;

import com.javaedge.concurrency.annoations.NotThreadSafe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author JavaEdge
 */
@Slf4j
@NotThreadSafe
public class ConcurrencyTest {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程数
    public static int threadTotal = 200;

    public static int count = 0;

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (Exception e) {
//                    log.error("exception", e);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        executorService.shutdown();
//        log.info("count:{}", count);
    }

    private static void add() {
        count++;
    }

    /**
     * @author JavaEdge
     */
    @SpringBootApplication
    public static class ConcurrencyApplication extends WebMvcConfigurerAdapter {

        public static void main(String[] args) {
            SpringApplication.run(ConcurrencyApplication.class, args);
        }

        @Bean
        public FilterRegistrationBean httpFilter() {
            FilterRegistrationBean registrationBean = new FilterRegistrationBean();
            registrationBean.setFilter(new HttpFilter());
            registrationBean.addUrlPatterns("/threadLocal/*");
            return registrationBean;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new HttpInterceptor()).addPathPatterns("/**");
        }
    }
}
