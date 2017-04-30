package sample.camel.http;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.dispatch.Mapper;

public class HttpConsumer extends UntypedConsumerActor {

  private ActorRef producer;

  public HttpConsumer(ActorRef producer) {
    this.producer = producer;
  }

  public String getEndpointUri() {
//    return "jetty:http://0.0.0.0:8875/";
    return "rabbitmq://localhost:5672/test-exchange-1?routingKey=test&queue=test-queue-1&autoDelete=false&autoAck=false&publisherAcknowledgements=true&publisherAcknowledgementsTimeout=1000";
  }

  public void onReceive(Object message) {
//    producer.forward(message, getContext());
      if (message instanceof CamelMessage) {
          CamelMessage camelMessage = (CamelMessage) message;
          CamelMessage replacedMessage = camelMessage.mapBody(new Mapper<Object, String>() {
              @Override
              public String apply(Object body) {
                  String text = new String((byte[]) body);
                  System.out.println(text);
//                  return text.replaceAll("Akka ", "AKKA ");
                  return text;
              }
          });
//          getSender().tell(replacedMessage, getSelf());
      } else if (message instanceof Status.Failure) {
          System.out.println(message.toString());
//          getSender().tell(message, getSelf());
      }
//      else
//          unhandled(message);
  }
}
