package tutorial;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.io.IOException;

/**
 * Created by dtkmn on 4/05/2017.
 */
public class BasicCamel {

    public static void main(String[] args) throws Exception {

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                onCompletion().process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("Completed! " + exchange.getIn().getMessageId());
                    }
                });

                errorHandler(deadLetterChannel("rabbitmq://localhost:5672/test-exchange-1?queue=error-handler&autoDelete=false&autoAck=false")
                    .onExceptionOccurred(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Inside error-handler-onExceptionOccurred-rpocess method ...");
                            System.out.println(exchange.getIn().getMessageId());
                            System.out.println("CamelRedeliveryCounter: " + exchange.getIn().getHeader("CamelRedeliveryCounter"));
                            System.out.println("CamelRedeliveryMaxCounter: " + exchange.getIn().getHeader("CamelRedeliveryMaxCounter"));
                        }
                    })
                    .onRedelivery(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Inside error-handler-ondelivery-process method ...");
                            System.out.println(exchange.getIn().getMessageId());
                            System.out.println("CamelRedeliveryCounter: " + exchange.getIn().getHeader("CamelRedeliveryCounter"));
                            System.out.println("CamelRedeliveryMaxCounter: " + exchange.getIn().getHeader("CamelRedeliveryMaxCounter"));
                        }
                    })
                    .log("Going to dead letter channel ...")
                    .useOriginalMessage().maximumRedeliveries(3).redeliveryDelay(5000));

                from("rabbitmq://localhost:5672/amq.topic?queue=test1&autoDelete=false&autoAck=false&exchangeType=topic&prefetchCount=1&bridgeEndpoint=true")
                    .process(exchange -> {
//                        exchange.getIn().removeHeaders("*");
                        exchange.getIn().setBody(String.format("Welcome %s", exchange.getIn().getBody()));

                        System.out.println("Inside from-process method ...");
                        System.out.println("Inside from-process method ...");
                        System.out.println(exchange.getIn().getMessageId());
                        System.out.println("CamelRedeliveryCounter: " + exchange.getIn().getHeader("CamelRedeliveryCounter"));
                        System.out.println("CamelRedeliveryMaxCounter: " + exchange.getIn().getHeader("CamelRedeliveryMaxCounter"));
                        throw new IOException("Testing exception here");

                    })
                    .to("rabbitmq://localhost:5672/amq.topic?queue=test-queue-1&autoDelete=false&autoAck=false&exchangeType=topic&bridgeEndpoint=true");



            }
        });
        camelContext.start();

    }

}
