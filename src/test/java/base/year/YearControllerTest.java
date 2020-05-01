package base.year;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc

public class YearControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser
    public void getCurYears() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/year/curUserYears").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError()).andExpect(content().string(equalTo("")));
    }

}
