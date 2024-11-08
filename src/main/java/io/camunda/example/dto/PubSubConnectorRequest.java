package io.camunda.example.dto;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.PropertyType;
import jakarta.validation.constraints.NotEmpty;

public record PubSubConnectorRequest(
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String message,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String googleCloudUsername,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String googleCloudPassword,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String googleCloudProjectID,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String googleCloudPubTopicID,
    @NotEmpty @TemplateProperty(type = PropertyType.Text) String GoogleCloudSubID){}