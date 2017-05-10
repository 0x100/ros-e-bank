package ru.tn.exteranalpayment;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.tn.externalpayment.ExternalPaymentApplication;
import ru.tn.externalpayment.repository.ExternalPaymentRepository;
import ru.tn.model.Payment;

import java.math.BigDecimal;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = ExternalPaymentApplication.class)
@RunWith(SpringRunner.class)
public class ExternalPaymentDocumentation {
    private static final Gson gson = new Gson();
    private static final Payment payment = new Payment();

    static {
        payment.setClientName("Конор МакГрегор");
        payment.setTransferSum(BigDecimal.valueOf(1000500));
        payment.setAccountNumber("000000000000000000");
    }

    private MockMvc mockMvc;

    @Autowired
    private ExternalPaymentRepository paymentRepository;

    @Autowired
    private WebApplicationContext context;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Before
    public void setUp() {
        RestDocumentationResultHandler resultHandler = document("{methodName}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        mockMvc = webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(resultHandler)
                .build();
    }


    @Test
    public void createPayment() throws Exception {
        mockMvc.perform(
                post("/external-payments")
                        .content(gson.toJson(payment))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());
    }

    @Test
    public void findAllPayments() throws Exception {
        paymentRepository.save(payment);
        mockMvc.perform(
                get("/external-payments")
        ).andExpect(status().isOk());
    }
}
