package com.example.terminology_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tm2_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamasteCode {

    @Id
    private String id;

    @Field("tm2_code")
    @Indexed
    private String tm2Code;

    @Field("tm2_link")
    private String tm2Link;

    @Field("code")
    @Indexed
    private String code;

    @Field("tm2_title")
    @Indexed
    private String tm2Title;

    @Field("tm2_definition")
    private String tm2Definition;

    @Field("code_title")
    @Indexed
    private String codeTitle;

    @Field("code_description")
    private String codeDescription;

    @Field("confidence_score")
    private Double confidenceScore;

    @Field("type")
    @Indexed
    private String type;
}
