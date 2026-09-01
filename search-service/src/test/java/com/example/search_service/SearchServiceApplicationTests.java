package com.example.search_service;

import com.example.search_service.repository.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SearchServiceApplicationTests {

    @MockBean
    private ProductSearchRepository productSearchRepository;

	@Test
	void contextLoads() {
	}

}
