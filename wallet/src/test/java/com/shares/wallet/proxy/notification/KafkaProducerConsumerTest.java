package com.shares.wallet.proxy.notification;

import com.shares.wallet.Proxy.notification.kafka.KafKaConsumerService;
import com.shares.wallet.Proxy.notification.kafka.KafkaProducerService;
import com.shares.wallet.model.History;
import com.shares.wallet.model.Users;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class KafkaProducerConsumerTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @DynamicPropertySource
    public static void initKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaProducerService publisher;

    @Autowired
    private KafKaConsumerService consumer;

    @MockBean
    private RestTemplate restTemplate;
    private String topic = "transaction-notification";
    private History historyTest;

    @BeforeEach
    public void init(){

        Users userTest = Users
                .builder()
                .username("userTest")
                .email("userTest@gmail.com")
                .build();

        historyTest = History
                .builder()
                .user(userTest)
                .action("buy")
                .quant(new BigDecimal("100.50"))
                .name("AAPL")
                .date(new Date())
                .price(new BigDecimal("150.75"))
                .build();

    }

    @Test
    public void testSendEventsToTopic() throws InterruptedException {
        //Arrange
        ResponseEntity<Boolean> responseEntity =
                new ResponseEntity<>(true, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(responseEntity);

        //act
        publisher.send("transaction-notification", historyTest);
        await().pollInterval(Duration.ofSeconds(10)).atMost(12, SECONDS).untilAsserted(() -> {
                Assertions.assertThat(consumer.getPayload().getName()).isEqualTo(historyTest.getName());
        });

    }

    @TestConfiguration
    static class KafkaTestContainersConfiguration {
        private final static Logger logger = LoggerFactory.getLogger(KafkaProducerConsumerTest.class);
        String bootstrapServers = kafka.getBootstrapServers();

        @Bean
        public KafkaAdmin kafkaAdmin() {
            logger.info("Configuring KafkaAdmin with bootstrap servers: {}", bootstrapServers);
            Map<String, Object> configs = new HashMap<>();
            configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            return new KafkaAdmin(configs);
        }
        @Bean
        public ProducerFactory<String, History> producerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
            return new DefaultKafkaProducerFactory<>(config);
        }
        @Bean
        public NewTopic taskTopic() {
            return TopicBuilder.name("transaction-notification")
                    .partitions(1)
                    .replicas(1)
                    .build();
        }
        @Bean
        public KafkaTemplate<String, History> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public ConsumerFactory<String, History> consumerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
            config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            return new DefaultKafkaConsumerFactory<>(config);
        }
        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, History> kafkaListenerContainerFactory(
                ConsumerFactory<String, History> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, History> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }
    }

}