package com.grpc.demo.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
        doClientStreamingCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {

        // create a service client (blocking - synchronous)
        System.out.println("Creating stub");
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // create a proto buffer message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Steven")
                .setLastName("Xi")
                .build();

        // create a request
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call the RPC and get back a response
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {

        System.out.println("Creating stub");
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Steven"))
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doClientStreamingCall(ManagedChannel channel) {

        // create a client with asynchronous stub
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // get a response from the server, onNext called only once
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // get an error from the server
            }

            @Override
            public void onCompleted() {
                // the server is done, onCompleted called after onNext
                System.out.println("Server has completed sending requests");
                latch.countDown();
            }
        });

        // send requests
        System.out.println("Sending message 1");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                    .setFirstName("Steven")
                    .build())
                .build());

        System.out.println("Sending message 2");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Jane")
                        .build())
                .build());

        System.out.println("Sending message 3");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Woo")
                        .build())
                .build());

        // tell the server the client is done sending data
        requestStreamObserver.onCompleted();

        // use a count down latch for the asynchronous call to wait for the response
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
