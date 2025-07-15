package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.ContactTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProfileRestControllerTest extends AbstractControllerTest {

    private static final String REST_URL = "/api/profile/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_MAIL = "user@gmail.com";
    private static final String ADMIN_MAIL = "admin@gmail.com";

    @Test
    @WithUserDetails(value = USER_MAIL)
    void getProfile() throws Exception {
        mockMvc.perform(get(REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(USER_MAIL));
    }

    @Test
    void getProfileUnauthorized() throws Exception {
        mockMvc.perform(get(REST_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfile() throws Exception {
        ProfileTo updatedProfile = new ProfileTo(null, Set.of("EMAIL_NOTIFICATIONS"), Set.of(new ContactTo("LINKEDIN", "new_linkedin_url")));

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProfile)))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get(REST_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mailNotifications[0]").value("EMAIL_NOTIFICATIONS"))
                .andExpect(jsonPath("$.contacts[0].code").value("LINKEDIN"))
                .andExpect(jsonPath("$.contacts[0].value").value("new_linkedin_url"));
    }

    @Test
    void updateProfileUnauthorized() throws Exception {
        ProfileTo updatedProfile = new ProfileTo(null, Set.of("EMAIL_NOTIFICATIONS"), Collections.emptySet());

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProfile)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileNullMailNotifications() throws Exception {
        ProfileTo invalidProfile = new ProfileTo(null, null, Collections.emptySet()); // mailNotifications = null

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfile)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileNullContacts() throws Exception {
        ProfileTo invalidProfile = new ProfileTo(null, Set.of("EMAIL_NOTIFICATIONS"), null); // contacts = null

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfile)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileBlankMailNotification() throws Exception {
        ProfileTo invalidProfile = new ProfileTo(null, Set.of(""), Collections.emptySet()); // Порожній рядок

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfile)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileInvalidContact() throws Exception {
        ProfileTo invalidProfile = new ProfileTo(null, Collections.emptySet(), Set.of(new ContactTo("", ""))); // Невалідний ContactTo

        mockMvc.perform(put(REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProfile)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
