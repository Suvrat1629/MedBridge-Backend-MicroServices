package com.example.fhir_service.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.example.fhir_service.client.TerminologyServiceClient;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TerminologyFhirServiceTest {

    private TerminologyFhirService svc;
    private TerminologyServiceClient fakeClient;
    private FhirContext fhirContext;
    private IParser jsonParser;

    @BeforeEach
    public void setup() throws Exception {
        fhirContext = FhirContext.forR4();
        jsonParser = fhirContext.newJsonParser();
        fakeClient = createClientProxy();
        svc = new TerminologyFhirService(fakeClient, fhirContext, jsonParser);
    }

    @Test
    public void testToJson_serializesResource() {
        var patient = new org.hl7.fhir.r4.model.Patient();
        patient.setId("p1");
        String json = svc.toJson(patient);
        assertNotNull(json);
        assertTrue(json.contains("\"resourceType\":\"Patient\""));
    }

    @Test
    public void testCreateSearchByCodeResult_noResults() {
        // fake client returns empty list by default
        Parameters p = svc.createSearchByCodeResult("NONEXISTENT");
        assertNotNull(p);

        var resultParam = p.getParameter().stream()
                .filter(pp -> "result".equals(pp.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(resultParam);
        assertTrue(resultParam.getValue() instanceof BooleanType);

        // service currently returns false when no matches; assert accordingly
        assertFalse(((BooleanType) resultParam.getValue()).booleanValue());
    }

    @Test
    public void testCreateSearchBySymptomsResult_groupingAndLimits() throws Exception {
        Class<?> ncCls = Class.forName("com.example.fhir_service.dto.NamasteCode");
        Constructor<?> ctor = ncCls.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object a = ctor.newInstance();
        Object b = ctor.newInstance();

        // set multiple variants to ensure DTO fields are populated regardless of naming
        setProp(a, "setTm2Code", String.class, "TM2.G1");
        setProp(a, "setIcd11Tm2Code", String.class, "TM2.G1");
        setProp(a, "setTm2Title", String.class, "Disease G1");
        setProp(a, "setTm2Definition", String.class, "Def G1");
        setProp(a, "setConfidenceScore", Double.class, 0.9);
        setProp(a, "setNamasteCode", String.class, "A1");
        setProp(a, "setNamasteName", String.class, "Map A1");

        setProp(b, "setTm2Code", String.class, "TM2.G1");
        setProp(b, "setTm2Title", String.class, "Disease G1");
        setProp(b, "setTm2Definition", String.class, "Def G1");
        setProp(b, "setConfidenceScore", Double.class, 0.8);
        setProp(b, "setNamasteCode", String.class, "B1");
        setProp(b, "setNamasteName", String.class, "Map B1");

        ((FakeClient) fakeClient).setSearchBySymptomsResponse(List.of(a, b));

        List<String> symptoms = List.of("fever", "headache");
        Parameters p = svc.createSearchBySymptomsResult(symptoms);
        assertNotNull(p);

        var resultParam = p.getParameter().stream().filter(pp -> "result".equals(pp.getName())).findFirst().orElse(null);
        assertNotNull(resultParam);
        assertTrue(((BooleanType) resultParam.getValue()).booleanValue());

        var totalGroupsParam = p.getParameter().stream().filter(pp -> "totalDiseaseGroups".equals(pp.getName())).findFirst().orElse(null);
        assertNotNull(totalGroupsParam);
        assertEquals(1, ((org.hl7.fhir.r4.model.IntegerType) totalGroupsParam.getValue()).getValue().intValue());

        boolean hasDiseaseGroup = p.getParameter().stream().anyMatch(pp -> "diseaseGroup".equals(pp.getName()));
        assertTrue(hasDiseaseGroup);
    }

    // --- helpers ---

    private TerminologyServiceClient createClientProxy() {
        return new FakeClient();
    }

    private static void setProp(Object target, String setterName, Class<?> paramType, Object value) {
        if (target == null) return;
        try {
            Method m = target.getClass().getMethod(setterName, paramType);
            m.invoke(target, value);
            return;
        } catch (Throwable ignored) { }
        try {
            // fallback: field name derived from setter (setXyz -> xyz)
            String fieldName = setterName.startsWith("set") && setterName.length() > 3
                    ? Character.toLowerCase(setterName.charAt(3)) + setterName.substring(4)
                    : setterName;
            java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Throwable ignored) { }
    }

    // FakeClient now extends the concrete TerminologyServiceClient class
    public static class FakeClient extends TerminologyServiceClient {
        private java.util.List<Object> searchByCodeResponse = List.of();
        private java.util.List<Object> searchBySymptomsResponse = List.of();

        public FakeClient() {
            super(null);
        }

        public void setSearchByCodeResponse(java.util.List<Object> resp) { this.searchByCodeResponse = resp; }
        public void setSearchBySymptomsResponse(java.util.List<Object> resp) { this.searchBySymptomsResponse = resp; }

        @Override
        public java.util.List searchByCode(String codeValue) {
            return searchByCodeResponse;
        }

        @Override
        public java.util.List searchBySymptoms(java.util.List symptoms) {
            return searchBySymptomsResponse;
        }
    }
}