package online.lucianmusat.Parastas;

import jakarta.servlet.RequestDispatcher;
import online.lucianmusat.Parastas.presentation.controllers.ErrController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class ErrControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(new ErrController())
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void testHandleError() throws Exception {
        mockMvc.perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
            .andExpect(status().isOk())
            .andExpect(view().name("error"));
    }

    @Test
    public void testHandleErrorWithNoStatus() throws Exception {
        mockMvc.perform(get("/error"))
            .andExpect(status().isOk())
            .andExpect(view().name("error"));
    }
}
