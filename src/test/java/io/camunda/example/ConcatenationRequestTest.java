package io.camunda.example;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.example.dto.PubConnectorRequest;
import org.junit.jupiter.api.Test;

public class ConcatenationRequestTest {

  ObjectMapper objectMapper = new ObjectMapper();
  String input1, input2;

  @Test
  void shouldFailWhenValidate_NoInput1() throws JsonProcessingException {
    // given
    var input = new PubConnectorRequest(input1);

    var context = OutboundConnectorContextBuilder
      .create()
      .variables(input).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(PubConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("input1");
  }

  @Test
  void shouldFailWhenValidate_NoInput2() throws JsonProcessingException {
    // given
    var input = new PubConnectorRequest(input1, input1, input1, input1, input1, input1);

    var context = OutboundConnectorContextBuilder
      .create()
      .variables(input).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(PubConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("input2");
  }
}