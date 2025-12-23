package com.example.fhir_service.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FhirTerminologyControllerTest {

    private FhirTerminologyController controller;

    @BeforeEach
    public void setup() {
        // real FHIR parser used by the service for JSON serialization
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        // anonymous subclass overriding only the two methods used by controller endpoints
        var fakeService = new com.example.fhir_service.service.TerminologyFhirService(null, ctx, parser) {
            @Override
            public Parameters createSearchByCodeResult(String codeValue) {
                Parameters p = new Parameters();
                p.setId("search-by-code-" + codeValue);
                p.addParameter().setName("result").setValue(new BooleanType(true));
                p.addParameter().setName("totalMatches").setValue(new org.hl7.fhir.r4.model.IntegerType(1));
                // Add a 'match' group to mirror real output
                Parameters.ParametersParameterComponent match = p.addParameter();
                match.setName("match");
                match.addPart().setName("code").setValue(new org.hl7.fhir.r4.model.StringType(codeValue));
                match.addPart().setName("tm2_code").setValue(new org.hl7.fhir.r4.model.StringType("TM2.TEST"));
                return p;
            }

            @Override
            public Parameters createSearchBySymptomsResult(List<String> symptoms) {
                Parameters p = new Parameters();
                p.setId("search-by-symptoms");
                p.addParameter().setName("result").setValue(new BooleanType(true));
                p.addParameter().setName("totalDiseaseGroups").setValue(new org.hl7.fhir.r4.model.IntegerType(1));
                Parameters.ParametersParameterComponent group = p.addParameter();
                group.setName("diseaseGroup");
                group.addPart().setName("tm2_code").setValue(new org.hl7.fhir.r4.model.StringType("TM2.G1"));
                group.addPart().setName("tm2_title").setValue(new org.hl7.fhir.r4.model.StringType("Group 1"));
                return p;
            }
        };

        controller = new FhirTerminologyController(fakeService);
    }

    @Test
    public void testSearchByCode_returnsFhirJsonAndHeaders() {
        ResponseEntity<String> resp = controller.searchByCode("A01.1");
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getHeaders().getContentType());
        assertEquals("application/fhir+json;fhirVersion=4.0", resp.getHeaders().getContentType().toString());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("\"resourceType\":\"Parameters\""));
        assertTrue(resp.getBody().contains("search-by-code-A01.1"));
    }

    @Test
    public void testSearchByTm2Code_and_CodeOnly_return200() {
        ResponseEntity<String> r1 = controller.searchByTm2Code("TM2.TEST");
        ResponseEntity<String> r2 = controller.searchByCodeOnly("A01.1");
        assertEquals(200, r1.getStatusCodeValue());
        assertEquals(200, r2.getStatusCodeValue());
        assertTrue(r1.getBody().contains("\"resourceType\":\"Parameters\""));
        assertTrue(r2.getBody().contains("\"resourceType\":\"Parameters\""));
    }

    @Test
    public void testSearchBySymptoms_get_and_post() {
        ResponseEntity<String> getResp = controller.searchBySymptoms("fever,headache");
        assertEquals(200, getResp.getStatusCodeValue());
        assertTrue(getResp.getBody().contains("\"resourceType\":\"Parameters\""));
        // POST variant
        Map<String, List<String>> body = Map.of("symptoms", List.of("fever", "nausea"));
        ResponseEntity<String> postResp = controller.searchBySymptomsPost(body);
        assertEquals(200, postResp.getStatusCodeValue());
        assertTrue(postResp.getBody().contains("\"resourceType\":\"Parameters\""));
    }

    @Test
    public void testMetadata_and_Health() {
        ResponseEntity<String> meta = controller.getCapabilityStatement();
        assertEquals(200, meta.getStatusCodeValue());
        assertNotNull(meta.getBody());
        assertTrue(meta.getBody().contains("\"resourceType\":\"CapabilityStatement\""));

        ResponseEntity<java.util.Map<String, Object>> health = controller.health();
        assertEquals(200, health.getStatusCodeValue());
        assertNotNull(health.getBody());
        assertEquals("UP", health.getBody().get("status"));
        assertEquals("fhir-service", health.getBody().get("service"));
    }
}