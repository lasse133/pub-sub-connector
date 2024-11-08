package io.camunda.example;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.example.dto.PubSubConnectorRequest;
import io.camunda.example.dto.PubSubConnectorResult;
import io.grpc.netty.shaded.io.netty.handler.timeout.TimeoutException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

@OutboundConnector(
    name = "pub-sub-connector",
    inputVariables = {"message","googleCloudUsername","googleCloudPassword","googleCloudProjectID","googleCloudPubTopicID","GoogleCloudSubID"},
    type = "io.camunda:concatenation-api:1")

public class PubSubConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(PubSubConnectorFunction.class);

  @Override
  public Object execute(OutboundConnectorContext context) {
    final var connectorRequest = context.bindVariables(PubSubConnectorRequest.class);
    return executeConnector(connectorRequest);
  }

  private Object executeConnector(final PubSubConnectorRequest connectorRequest) {
    var result = connectorRequest.message();
    try {
      publishWithErrorHandlerExample(connectorRequest.googleCloudProjectID(), connectorRequest.googleCloudPubTopicID(), connectorRequest.message());
      subscribeAsyncExample(connectorRequest.googleCloudProjectID(),connectorRequest.GoogleCloudSubID());
    } catch (IOException | InterruptedException | java.util.concurrent.TimeoutException e) {
      LOGGER.error("Connector execution failed", e);
    }
    return result;
  }

  private void publishWithErrorHandlerExample(String projectId, String topicId, String message)
      throws IOException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);
    Publisher publisher = null;

    try {
      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).build();

        ByteString data = ByteString.copyFromUtf8(message);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

        // Once published, returns a server-assigned message id (unique within the topic)
        ApiFuture<String> future = publisher.publish(pubsubMessage);

        // Add an asynchronous callback to handle success / failure
        ApiFutures.addCallback(
            future,
            new ApiFutureCallback<String>() {

              @Override
              public void onFailure(Throwable throwable) {
                if (throwable instanceof ApiException) {
                  ApiException apiException = ((ApiException) throwable);
                  // details on the API exception
                  System.out.println(apiException.getStatusCode().getCode());
                  System.out.println(apiException.isRetryable());
                }
                System.out.println("Error publishing message : " + message);
              }

              @Override
              public void onSuccess(String messageId) {
                // Once published, returns server-assigned message ids (unique within the topic)
                System.out.println("Published message ID: " + messageId);
              }
            },
            MoreExecutors.directExecutor());
    } finally {
      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }
  private void subscribeAsyncExample(String projectId, String subscriptionId) throws java.util.concurrent.TimeoutException {
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(projectId, subscriptionId);

    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          System.out.println("Id: " + message.getMessageId());
          System.out.println("Data: " + message.getData().toStringUtf8());
          consumer.ack();
        };

    Subscriber subscriber = null;
    try {
      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      // Start the subscriber.
      subscriber.startAsync().awaitRunning();
      System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());
      // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
      subscriber.awaitTerminated(30, TimeUnit.SECONDS);
    } catch (TimeoutException timeoutException) {
      // Shut down the subscriber after 30s. Stop receiving messages.
      subscriber.stopAsync();
    }
  }
}