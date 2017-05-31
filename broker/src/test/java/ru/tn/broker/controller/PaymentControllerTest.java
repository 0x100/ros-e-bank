package ru.tn.broker.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.tn.broker.PaymentBrokerApplication;
import ru.tn.broker.repository.PaymentRepository;
import ru.tn.broker.service.PaymentServicesActuator;
import ru.tn.gateway.publish.config.GatewayPublisherConfiguration;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaymentBrokerApplication.class)
@WebAppConfiguration
@Transactional
@ActiveProfiles("test")
public class PaymentControllerTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private MockMvc mockMvc;
    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private List<Payment> payments = new ArrayList<>();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PaymentServicesActuator actuator;

    @MockBean
    private GatewayPublisherConfiguration gatewayPublisher;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    private RestDocumentationResultHandler resultHandler;

    private final FieldDescriptor[] paymentFields = new FieldDescriptor[]{
            fieldWithPath("id").description("ID платежа"),
            fieldWithPath("clientName").description("Имя клиента"),
            fieldWithPath("accountNumber").description("Номер счета"),
            fieldWithPath("transferSum").description("Сумма платежа"),
            fieldWithPath("status").description("Статус платежа")
    };

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
        resultHandler = document("{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        mockMvc = webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(resultHandler)
                .build();

        paymentRepository.deleteAll();

        Payment payment = new Payment();
        payment.setAccountNumber("224477887777");
        payment.setClientName("R.Abramovich");
        payment.setTransferSum(BigDecimal.valueOf(112233445566.77));

        Payment storedPayment = paymentRepository.save(payment);
        payments.add(storedPayment);

        when(actuator.getPaymentClient(eq("7777"))).thenReturn(p -> new ResponseEntity<>(HttpStatus.CREATED));
    }

    @Test
    public void pay() throws Exception {
        Payment payment = new Payment();
        payment.setAccountNumber("112233447777");
        payment.setClientName("V.Pupkin");
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

        assertEquals("224477887777", payment.getAccountNumber());
        assertEquals("R.Abramovich", payment.getClientName());
        assertEquals(BigDecimal.valueOf(112233445566.77), payment.getTransferSum());

        mockMvc.perform(
                get("/broker/payment/" + payment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType))
                    .andExpect(jsonPath("$.id", is(payment.getId())))
                    .andExpect(jsonPath("$.accountNumber", is(payment.getAccountNumber())))
                    .andExpect(jsonPath("$.clientName", is(payment.getClientName())))
                    .andDo(resultHandler.document(responseFields(paymentFields)));
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