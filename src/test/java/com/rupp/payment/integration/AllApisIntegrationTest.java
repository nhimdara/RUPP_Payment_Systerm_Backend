package com.rupp.payment.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AllApisIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String adminRefreshToken;
    private String studentToken;
    private String financeToken;

    @BeforeEach
    void setUpTokens() throws Exception {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(context)
                    .apply(springSecurity())
                    .build();
        }
        if (adminToken == null) {
            String[] adminTokens = obtainTokens("admin", "admin123");
            adminToken = adminTokens[0];
            adminRefreshToken = adminTokens[1];

            String[] studentTokens = obtainTokens("student1", "student123");
            studentToken = studentTokens[0];

            String[] financeTokens = obtainTokens("finance", "finance123");
            financeToken = financeTokens[0];
        }
    }

    private String[] obtainTokens(String username, String password) throws Exception {
        String loginPayload = String.format("{\"usernameOrEmail\": \"%s\", \"password\": \"%s\"}", username, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return new String[]{
                root.path("data").path("accessToken").asText(),
                root.path("data").path("refreshToken").asText()
        };
    }

    // ==========================================
    // 1. AUTHENTICATION & CURRENT USER APIS
    // ==========================================

    @Test
    @DisplayName("API: POST /api/v1/auth/login - Admin Login Success")
    void testAuthLogin_Success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\": \"admin\", \"password\": \"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/login - Invalid Credentials 401")
    void testAuthLogin_InvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usernameOrEmail\": \"admin\", \"password\": \"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("API: GET /api/v1/auth/me - Authenticated User Profile")
    void testAuthGetMe_Success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/refresh - Refresh Access Token")
    void testAuthRefresh_Success() throws Exception {
        String payload = String.format("{\"refreshToken\": \"%s\"}", adminRefreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/register - Register Student and Conflict Detection (409)")
    void testAuthRegister_Flow() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "student_" + uniqueSuffix;
        String email = username + "@rupp.edu.kh";

        String registerPayload = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"secret123\", \"firstName\": \"Test\", \"lastName\": \"Student\"}",
                username, email
        );

        // 1. Success
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value(username));

        // 2. Conflict on duplicate username
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==========================================
    // 2. USER MANAGEMENT APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/users - List All Users")
    void testGetUsers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("API: GET /api/v1/users/{id} - Get User By ID")
    void testGetUserById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("API: POST /api/v1/users and PUT /api/v1/users/{id}")
    void testCreateAndUpdateUser_Success() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 6);
        String createPayload = String.format(
                "{\"username\": \"staff_%s\", \"email\": \"staff_%s@rupp.edu.kh\", \"password\": \"staff123\", \"role\": \"ROLE_FINANCE_STAFF\"}",
                unique, unique
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        long createdId = json.path("data").path("id").asLong();

        // Update user
        String updatePayload = "{\"email\": \"updated_" + unique + "@rupp.edu.kh\"}";
        mockMvc.perform(put("/api/v1/users/" + createdId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: DELETE /api/v1/users/{id} - Delete User")
    void testDeleteUser_Success() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 6);
        String createPayload = String.format(
                "{\"username\": \"del_%s\", \"email\": \"del_%s@rupp.edu.kh\", \"password\": \"pass123\", \"role\": \"ROLE_STUDENT\"}",
                unique, unique
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/v1/users/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 3. FACULTY & ACADEMIC STRUCTURE APIS
    // ==========================================

    @Test
    @DisplayName("API: GET and POST /api/v1/faculties")
    void testFacultyApis() throws Exception {
        mockMvc.perform(get("/api/v1/faculties")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        String unique = UUID.randomUUID().toString().substring(0, 6);
        String payload = "{\"facultyName\": \"Faculty " + unique + "\", \"description\": \"Test faculty description\"}";

        mockMvc.perform(post("/api/v1/faculties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: Complete Faculty CRUD (GET {id}, PUT {id}, DELETE {id})")
    void testFacultyCrud_Complete() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 6);
        MvcResult result = mockMvc.perform(post("/api/v1/faculties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facultyName\": \"Faculty " + unique + "\", \"description\": \"Desc\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/faculties/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facultyName").value("Faculty " + unique));

        mockMvc.perform(put("/api/v1/faculties/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facultyName\": \"Faculty " + unique + " Updated\", \"description\": \"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facultyName").value("Faculty " + unique + " Updated"));

        mockMvc.perform(delete("/api/v1/faculties/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: GET and POST /api/v1/academic-years")
    void testAcademicYearApis() throws Exception {
        mockMvc.perform(get("/api/v1/academic-years")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        String uniqueYear = "20" + (int) (Math.random() * 40 + 50) + "-20" + (int) (Math.random() * 40 + 51);
        String payload = String.format("{\"yearLabel\": \"%s\", \"startDate\": \"2050-10-01\", \"endDate\": \"2051-07-31\"}", uniqueYear);

        mockMvc.perform(post("/api/v1/academic-years")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: Complete Academic Year CRUD (GET {id}, PUT {id}, DELETE {id})")
    void testAcademicYearCrud_Complete() throws Exception {
        String uniqueYear = "20" + (int) (Math.random() * 40 + 50) + "-20" + (int) (Math.random() * 40 + 51);
        MvcResult result = mockMvc.perform(post("/api/v1/academic-years")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"yearLabel\": \"%s\", \"startDate\": \"2050-01-01\", \"endDate\": \"2050-12-31\"}", uniqueYear)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/academic-years/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.yearLabel").value(uniqueYear));

        mockMvc.perform(put("/api/v1/academic-years/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"yearLabel\": \"%s-U\", \"startDate\": \"2050-01-01\", \"endDate\": \"2050-12-31\"}", uniqueYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.yearLabel").value(uniqueYear + "-U"));

        mockMvc.perform(delete("/api/v1/academic-years/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: GET /api/v1/semesters and /active")
    void testSemesterApis() throws Exception {
        mockMvc.perform(get("/api/v1/semesters")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/semesters/active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: Complete Semester CRUD (POST, GET {id}, PUT {id}, DELETE {id})")
    void testSemesterCrud_Complete() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/semesters")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semesterName\": \"Test Semester\", \"semesterNumber\": 2, \"academicYearId\": 1, \"startDate\": \"2026-09-01\", \"endDate\": \"2027-01-31\", \"isActive\": false}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/semesters/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.semesterName").value("Test Semester"));

        mockMvc.perform(put("/api/v1/semesters/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semesterName\": \"Test Semester Updated\", \"academicYearId\": 1, \"startDate\": \"2026-09-01\", \"endDate\": \"2027-01-31\", \"isActive\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.semesterName").value("Test Semester Updated"));

        mockMvc.perform(delete("/api/v1/semesters/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: GET and POST /api/v1/majors")
    void testMajorApis() throws Exception {
        mockMvc.perform(get("/api/v1/majors")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String uniqueCode = "M" + (int) (Math.random() * 9000 + 1000);
        String payload = String.format("{\"majorName\": \"Software Development %s\", \"majorCode\": \"%s\", \"facultyId\": 1, \"description\": \"CS Major\"}", uniqueCode, uniqueCode);

        mockMvc.perform(post("/api/v1/majors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: Complete Major CRUD (POST, GET {id}, GET faculty/{id}, PUT {id}, DELETE {id})")
    void testMajorCrud_Complete() throws Exception {
        String uniqueCode = "MJ" + (int) (Math.random() * 90000 + 10000);
        MvcResult result = mockMvc.perform(post("/api/v1/majors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"majorName\": \"Program %s\", \"majorCode\": \"%s\", \"facultyId\": 1, \"description\": \"Major Desc\"}", uniqueCode, uniqueCode)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/majors/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.majorCode").value(uniqueCode));

        mockMvc.perform(get("/api/v1/majors?facultyId=1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(put("/api/v1/majors/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"majorName\": \"Program %s Updated\", \"majorCode\": \"%s\", \"facultyId\": 1, \"description\": \"Major Updated\"}", uniqueCode, uniqueCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.majorName").value("Program " + uniqueCode + " Updated"));

        mockMvc.perform(delete("/api/v1/majors/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 4. STUDENT APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/students and /api/v1/students/my-profile")
    void testStudentApis() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/students/my-profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").isString());
    }

    @Test
    @DisplayName("API: PUT /api/v1/students/my-academic-info and GET /api/v1/students/{id}")
    void testStudentAcademicInfoAndDetails() throws Exception {
        mockMvc.perform(put("/api/v1/students/my-academic-info")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facultyId\": 1, \"academicYearId\": 1, \"majorId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.faculty.id").value(1));

        mockMvc.perform(get("/api/v1/students/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ==========================================
    // 5. TUITION FEE STRUCTURE APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/tuition-fees")
    void testTuitionFeeApis() throws Exception {
        mockMvc.perform(get("/api/v1/tuition-fees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("API: Complete Tuition Fee CRUD (GET {id}, GET my-fee, POST, PUT, DELETE)")
    void testTuitionFeeCrud_Complete() throws Exception {
        mockMvc.perform(get("/api/v1/tuition-fees/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/api/v1/tuition-fees/my-fee")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        MvcResult result = mockMvc.perform(post("/api/v1/tuition-fees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facultyId\": 2, \"academicYearId\": 2, \"totalAmount\": 750.00, \"dueDate\": \"2030-12-31\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/tuition-fees/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"facultyId\": 2, \"academicYearId\": 2, \"totalAmount\": 800.00, \"dueDate\": \"2030-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(800.00));

        mockMvc.perform(delete("/api/v1/tuition-fees/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 6. PAYMENT & RECEIPT APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/payments/search and /api/v1/payments/{id}")
    void testPaymentGetApis() throws Exception {
        mockMvc.perform(get("/api/v1/payments/search")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/payments/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/api/v1/payments/my-payments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("API: POST /api/v1/payments - Student Makes Payment")
    void testMakePayment_Success() throws Exception {
        String uniqueTxn = "TXN-" + UUID.randomUUID().toString().substring(0, 10);
        String payload = String.format(
                "{\"feeStructureId\": 1, \"paymentMethod\": \"ABA\", \"transactionRef\": \"%s\", \"amount\": 600.00, \"remarks\": \"Tuition fee payment\"}",
                uniqueTxn
        );

        mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("API: Payment OTP Verification and Finance Rejection Flow")
    void testPaymentOtpAndRejectionFlow() throws Exception {
        // 1. Student creates payment
        String uniqueTxn = "TXN-" + UUID.randomUUID().toString().substring(0, 10);
        String createPayload = String.format(
                "{\"feeStructureId\": 1, \"paymentMethod\": \"ABA\", \"transactionRef\": \"%s\", \"amount\": 600.00, \"remarks\": \"Payment for OTP test\"}",
                uniqueTxn
        );

        MvcResult paymentRes = mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn();

        long paymentId = objectMapper.readTree(paymentRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. Student requests OTP
        MvcResult otpRes = mockMvc.perform(post("/api/v1/payments/" + paymentId + "/verification/otp")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.challengeId").isString())
                .andReturn();

        String challengeId = objectMapper.readTree(otpRes.getResponse().getContentAsString()).path("data").path("challengeId").asText();

        // 3. Student tries verifying with invalid OTP code (should fail with 400 Bad Request)
        mockMvc.perform(post("/api/v1/payments/verification/otp/" + challengeId + "/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"000000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 4. Finance staff rejects the payment with an Idempotency-Key
        String idempotencyKey = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/reject")
                        .header("Authorization", "Bearer " + financeToken)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Test rejection flow\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusName").value("REJECTED"));
    }

    @Test
    @DisplayName("API: GET /api/v1/receipts/{id} and by receipt number")
    void testReceiptApis() throws Exception {
        mockMvc.perform(get("/api/v1/receipts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/receipts/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiptNumber").value("RCP-20261019-0001"));

        mockMvc.perform(get("/api/v1/receipts/number/RCP-20261019-0001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiptNumber").value("RCP-20261019-0001"));

        mockMvc.perform(get("/api/v1/receipts/payment/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value(1));
    }

    // ==========================================
    // 7. FINANCIAL REPORTS & SUMMARY APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/reports/financial-summary, faculty-breakdown, and payment-methods")
    void testFinancialReportsApis() throws Exception {
        mockMvc.perform(get("/api/v1/reports/financial-summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").isNumber());

        mockMvc.perform(get("/api/v1/reports/faculty-breakdown")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reports/payment-methods")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("API: GET /api/v1/reports/summary and /api/v1/reports/payments")
    void testReportControllerApis() throws Exception {
        mockMvc.perform(get("/api/v1/reports/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/reports/payments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ==========================================
    // 8. SYSTEM MONITORING & AUDIT LOG APIS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/admin/monitoring/stats")
    void testSystemMonitoringApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/monitoring/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMemoryMb").isNumber())
                .andExpect(jsonPath("$.data.databaseStatus").value("UP"));
    }

    @Test
    @DisplayName("API: GET /api/v1/audit-logs and /api/v1/payments/{id}/audit-logs")
    void testAuditLogApis() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/payments/1/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // 9. TUITION CALCULATION, PDF RECEIPT & DASHBOARD METRICS
    // ==========================================

    @Test
    @DisplayName("API: GET /api/v1/tuition-fees/calculate and /calculate/{studentId}")
    void testTuitionCalculationApis() throws Exception {
        // Current student calculation
        mockMvc.perform(get("/api/v1/tuition-fees/calculate")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentName").isNotEmpty())
                .andExpect(jsonPath("$.data.totalTuitionFee").isNumber())
                .andExpect(jsonPath("$.data.remainingBalance").isNumber())
                .andExpect(jsonPath("$.data.paymentStatus").isNotEmpty());

        // Admin calculation for student 1
        mockMvc.perform(get("/api/v1/tuition-fees/calculate/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(1));
    }

    @Test
    @DisplayName("API: GET /api/v1/receipts/{id}/pdf, /receipts/payment/{id}/pdf, and /payments/{id}/receipt/pdf")
    void testPdfReceiptExportApis() throws Exception {
        // Receipt PDF by receipt ID
        mockMvc.perform(get("/api/v1/receipts/1/pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.springframework.http.MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().exists("Content-Disposition"));

        // Receipt PDF by payment ID via /api/v1/receipts/payment/1/pdf
        mockMvc.perform(get("/api/v1/receipts/payment/1/pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.springframework.http.MediaType.APPLICATION_PDF_VALUE));

        // Receipt PDF by payment ID via /api/v1/payments/1/receipt/pdf
        mockMvc.perform(get("/api/v1/payments/1/receipt/pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.springframework.http.MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @DisplayName("API: GET /api/v1/reports/daily-revenue and /monthly-revenue")
    void testDailyAndMonthlyRevenueApis() throws Exception {
        mockMvc.perform(get("/api/v1/reports/daily-revenue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reports/monthly-revenue?year=2024")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reports/financial-summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayRevenue").isNumber())
                .andExpect(jsonPath("$.data.currentMonthRevenue").isNumber())
                .andExpect(jsonPath("$.data.dailyBreakdown").isArray())
                .andExpect(jsonPath("$.data.monthlyBreakdown").isArray());
    }
}
