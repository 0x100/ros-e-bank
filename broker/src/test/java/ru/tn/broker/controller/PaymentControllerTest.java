package ru.tn.broker.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.tn.broker.PaymentBrokerApplication;
import ru.tn.broker.repository.PaymentRepository;
import ru.tn.model.Payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentBrokerApplication.class)
@WebAppConfiguration
public class PaymentControllerTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private MockMvc mockMvc;
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private List<Payment> payments = new ArrayList<>();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        paymentRepository.deleteAll();

        Payment payment = new Payment();
        payment.setAccountNumber("22447788");
        payment.setClientName("R.Abramovich");
        payment.setTransferSum(BigDecimal.valueOf(112233445566.77));

        Payment storedPayment = paymentRepository.save(payment);
        payments.add(storedPayment);
    }

    @Test
    public void pay() throws Exception {
        Payment payment = new Payment();
        payment.setAccountNumber("11223344");
        payment.setClientName("1");
        payment.setTransferSum(BigDecimal.valueOf(1025.55));

        mockMvc.perform(
                post("/broker/payment")
                        .content(json(payment))
                        .contentType(contentType))
                    .andExpect(status().isCreated());
    }

    @Test
    public void getPayment() throws Exception {
        Payment payment = payments.get(0);

        assertEquals("22447788", payment.getAccountNumber());
        assertEquals("R.Abramovich", payment.getClientName());
        assertEquals(BigDecimal.valueOf(112233445566.77), payment.getTransferSum());

        mockMvc.perform(
                get("/broker/payment/" + payment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andExpect(jsonPath("$.id", is(payment.getId())))
                    .andExpect(jsonPath("$.accountNumber", is(payment.getAccountNumber())))
                    .andExpect(jsonPath("$.clientName", is(payment.getClientName())));
    }

    @Test
    public void paymentNotFound() throws Exception {
        mockMvc.perform(get("/broker/payment/-1"))
                .andExpect(status().isNotFound());
    }

    @SuppressWarnings("unchecked")
    private String json(Object obj) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(obj, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}