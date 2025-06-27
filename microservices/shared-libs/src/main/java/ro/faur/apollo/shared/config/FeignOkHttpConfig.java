package ro.faur.apollo.shared.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.faur.apollo.shared.security.FeignClientInterceptor;
import ro.faur.apollo.shared.security.FeignErrorDecoder;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignOkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        // 200 idle connections, 5-minute keep-alive
        ConnectionPool pool = new ConnectionPool(200, 5, TimeUnit.MINUTES);
        return new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor feignClientInterceptor() {
        return new FeignClientInterceptor();
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                5000,  // connectTimeout
                TimeUnit.MILLISECONDS,
                10000, // readTimeout
                TimeUnit.MILLISECONDS,
                true   // followRedirects
        );
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
} 