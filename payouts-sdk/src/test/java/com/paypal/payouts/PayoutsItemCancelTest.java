package com.paypal.payouts;

import com.paypal.TestHarness;
import com.paypal.http.HttpResponse;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class PayoutsItemCancelTest extends TestHarness {

    @Test
    public void testPayoutsItemCancelRequest() throws IOException, InterruptedException {
        HttpResponse<CreatePayoutResponse> createResponse = PayoutsPostTest.createPayouts(client());
        assertEquals(createResponse.statusCode(), 201);
        assertNotNull(createResponse.result());

        int i = 0;
        do {
            Thread.sleep(2000);
            HttpResponse<PayoutBatch> getResponse = PayoutsGetTest.getPayouts(client(), createResponse.result().batchHeader().payoutBatchId());
            assertEquals(getResponse.statusCode(), 200);
            assertNotNull(getResponse.result());
            if (getResponse.result().batchHeader().batchStatus().equals("SUCCESS")) {
                PayoutsItemCancelRequest request = new PayoutsItemCancelRequest(getResponse.result().items().get(0).payoutItemId());

                HttpResponse<PayoutItemResponse> response = client().execute(request);
                assertEquals(response.statusCode(), 200);
                assertNotNull(response.result());

                PayoutItemResponse responseBody = response.result();

                assertNotNull(responseBody.payoutItemId());
                assertNotNull(responseBody.transactionId());
                assertNotNull(responseBody.transactionStatus());
                assertEquals(responseBody.transactionStatus(), "RETURNED");
                assertEquals(responseBody.payoutItem().amount().value(), "1.00");
                assertEquals(responseBody.payoutItem().amount().currency(), "USD");
                assertEquals(responseBody.payoutItem().senderItemId(), "Test_txn_1");
                assertEquals(responseBody.payoutItem().receiver(), "payout-sdk-1@paypal.com");
                break;
            }
            i++;
        } while (i < 5);

        assertNotEquals(i, 5, "Payouts batch has not processed all the payments yet");

    }
}
