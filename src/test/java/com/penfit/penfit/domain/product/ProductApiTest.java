package com.penfit.penfit.domain.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest {

    private static final String DETAIL_URL = "/api/v1/pension-products/%d";
    private static final String SAVED_URL = "/api/v1/users/me/saved-products";
    private static final String SAVED_ITEM_URL = SAVED_URL + "/%d";

    private static final String FUND_PRODUCT = "미래에셋 연금저축펀드";
    private static final String IRP_PRODUCT = "KB국민은행 개인형 IRP";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PensionProductRepository pensionProductRepository;

    @MockitoBean
    private KakaoOAuthClient kakaoOAuthClient;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAs("kakao-1", "이재원");
    }

    @Test
    @DisplayName("상품 상세는 요약과 특징, 주의사항을 함께 반환한다")
    void returnsProductDetail() throws Exception {
        long productId = productId(FUND_PRODUCT);

        mockMvc.perform(get(DETAIL_URL.formatted(productId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.providerName").value("미래에셋증권"))
                .andExpect(jsonPath("$.data.productName").value(FUND_PRODUCT))
                .andExpect(jsonPath("$.data.accountType.code").value("PENSION_SAVINGS_FUND"))
                .andExpect(jsonPath("$.data.accountType.displayName").value("연금저축펀드"))
                .andExpect(jsonPath("$.data.providerType.displayName").value("증권사"))
                .andExpect(jsonPath("$.data.features.length()").value(3))
                .andExpect(jsonPath("$.data.features[0]").value("ETF·펀드 2,300종 이상 선택 가능"))
                .andExpect(jsonPath("$.data.cautions[0]").value("일부 펀드는 판매수수료 별도"))
                .andExpect(jsonPath("$.data.investmentScope").value("ETF 300종 + 펀드 2,000종"));
    }

    @Test
    @DisplayName("추천을 받지 않았으면 추천 이유는 비어 있다")
    void recommendationReasonIsNullWithoutRecommendation() throws Exception {
        mockMvc.perform(get(DETAIL_URL.formatted(productId(FUND_PRODUCT)))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendationReason").doesNotExist())
                .andExpect(jsonPath("$.data.saved").value(false));
    }

    @Test
    @DisplayName("상품을 담으면 상세 조회의 담김 여부가 바뀐다")
    void savesProduct() throws Exception {
        long productId = productId(FUND_PRODUCT);

        mockMvc.perform(post(SAVED_ITEM_URL.formatted(productId))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(DETAIL_URL.formatted(productId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.saved").value(true));
    }

    @Test
    @DisplayName("담은 상품이 없으면 빈 목록을 반환한다")
    void returnsEmptySavedProducts() throws Exception {
        mockMvc.perform(get(SAVED_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("담은 상품 목록은 최근에 담은 순서로 반환한다")
    void returnsSavedProductsInRecentOrder() throws Exception {
        save(accessToken, productId(FUND_PRODUCT));
        save(accessToken, productId(IRP_PRODUCT));

        mockMvc.perform(get(SAVED_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].productName").value(IRP_PRODUCT))
                .andExpect(jsonPath("$.data[0].accountType.code").value("INDIVIDUAL_IRP"))
                .andExpect(jsonPath("$.data[1].productName").value(FUND_PRODUCT));
    }

    @Test
    @DisplayName("담아둔 상품을 취소하면 목록에서 사라진다")
    void deletesSavedProduct() throws Exception {
        long productId = productId(FUND_PRODUCT);
        save(accessToken, productId);

        mockMvc.perform(delete(SAVED_ITEM_URL.formatted(productId))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get(SAVED_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("없는 상품을 조회하면 PR4041 을 반환한다")
    void rejectsUnknownProductDetail() throws Exception {
        mockMvc.perform(get(DETAIL_URL.formatted(999_999L)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PR4041"));
    }

    @Test
    @DisplayName("없는 상품은 담을 수 없다")
    void rejectsSavingUnknownProduct() throws Exception {
        mockMvc.perform(post(SAVED_ITEM_URL.formatted(999_999L))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PR4041"));
    }

    @Test
    @DisplayName("이미 담은 상품을 다시 담으면 PR4091 을 반환한다")
    void rejectsDuplicateSave() throws Exception {
        long productId = productId(FUND_PRODUCT);
        save(accessToken, productId);

        mockMvc.perform(post(SAVED_ITEM_URL.formatted(productId))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PR4091"));
    }

    @Test
    @DisplayName("토큰이 없으면 상품 상세를 조회할 수 없다")
    void rejectsDetailWithoutToken() throws Exception {
        mockMvc.perform(get(DETAIL_URL.formatted(productId(FUND_PRODUCT))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰이 없으면 담은 상품 목록을 조회할 수 없다")
    void rejectsSavedListWithoutToken() throws Exception {
        mockMvc.perform(get(SAVED_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("담아두지 않은 상품을 취소해도 성공한다")
    void deleteIsIdempotent() throws Exception {
        mockMvc.perform(delete(SAVED_ITEM_URL.formatted(productId(FUND_PRODUCT)))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("다른 사용자가 담은 상품은 내 목록과 담김 여부에 영향을 주지 않는다")
    void savedProductsAreIsolatedPerUser() throws Exception {
        long productId = productId(FUND_PRODUCT);
        String otherToken = loginAs("kakao-2", "김하늘");
        save(otherToken, productId);

        mockMvc.perform(get(SAVED_URL).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get(DETAIL_URL.formatted(productId)).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.saved").value(false));
    }

    private long productId(String productName) {
        return pensionProductRepository.findAll().stream()
                .filter(product -> product.getProductName().equals(productName))
                .map(PensionProduct::getId)
                .findFirst()
                .orElseThrow();
    }

    private void save(String token, long productId) throws Exception {
        mockMvc.perform(post(SAVED_ITEM_URL.formatted(productId))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated());
    }

    private String loginAs(String kakaoId, String nickname) throws Exception {
        given(kakaoOAuthClient.fetchUserInfo(anyString()))
                .willReturn(new KakaoUserInfo(kakaoId, nickname));

        String body = mockMvc.perform(post("/api/v1/auth/kakao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"authorization-code\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
