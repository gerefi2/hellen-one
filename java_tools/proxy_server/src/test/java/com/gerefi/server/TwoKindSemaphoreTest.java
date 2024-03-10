package com.gerefi.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TwoKindSemaphoreTest {
    @Test
    public void testTwoKindSemaphore() {
        TwoKindSemaphore twoKindSemaphore = new TwoKindSemaphore();

        assertFalse(twoKindSemaphore.isUsed());
        assertTrue(twoKindSemaphore.acquireForShortTermUsage());
        assertFalse(twoKindSemaphore.isUsed());
        twoKindSemaphore.releaseFromShortTermUsage();

        assertNull(twoKindSemaphore.getOwner());

        UserDetails userDetails = new UserDetails("xxx", 222);


        assertTrue(twoKindSemaphore.acquireForLongTermUsage(userDetails));
        assertNotNull(twoKindSemaphore.getOwner());
        assertTrue(twoKindSemaphore.isUsed());
        //
        assertFalse(twoKindSemaphore.acquireForLongTermUsage(userDetails, 1));
        assertFalse(twoKindSemaphore.acquireForShortTermUsage());


        twoKindSemaphore.releaseFromLongTermUsage();
        assertNull(twoKindSemaphore.getOwner());
    }
}
