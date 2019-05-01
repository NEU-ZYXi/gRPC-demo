package com.grpc.demo.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);


        // Unary
//        SumRequest sumRequest = SumRequest.newBuilder()
//                .setFirstNumber(10)
//                .setSecondNumber(20)
//                .build();
//
//        SumResponse sumResponse = calculatorClient.sum(sumRequest);
//
//        System.out.println(sumRequest.getFirstNumber() + " + " + sumRequest.getSecondNumber() + " = " + sumResponse.getSumResult());


        // Server Streaming
        Long number = 5678908123123123137L;
        PrimeNumberDecompositionRequest request = PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number)
                .build();
        calculatorClient.primeNumberDecomposition(request)
                .forEachRemaining(primeNumberDecompositionResponse -> {
                    System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
                });


        channel.shutdown();
    }
}
