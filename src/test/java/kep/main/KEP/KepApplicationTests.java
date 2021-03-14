package kep.main.KEP;

import kep.main.KEP.dto.UserDTO;
import kep.main.KEP.model.User;
import kep.main.KEP.service.UserManager;
import kep.main.KEP.utils.UserUtils;
import kep.main.KEP.web.UserController;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.security.RunAs;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class KepApplicationTests {
	@Mock
	UserManager userManager;

	private MockMvc mockMvc;

	@Before("")
	public void setUp() throws Exception {
		initMocks(this);
		UserController controller = new UserController(userManager, new UserUtils());
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void contextLoads() throws Exception {

		User user = new User();
		Mockito.when(userManager.save(user)).thenReturn(new UserDTO());
		mockMvc.perform(MockMvcRequestBuilders.post("/save", user))
				.andExpect(status().isOk());
	}

}
