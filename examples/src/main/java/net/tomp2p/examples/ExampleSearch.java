/*
 * Copyright 2009 Thomas Bocek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.tomp2p.examples;

import java.io.IOException;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;

/**
 * Example how to search.
 * 
 * @author Thomas Bocek
 * 
 */
public final class ExampleSearch {
    private static final String TERM = "Communication Systems Group";

    /**
     * Empty constructor.
     */
    private ExampleSearch() {
    }

    /**
     * Start the examples.
     * 
     * @param args
     *            Empty
     * @throws Exception .
     */
    public static void main(final String[] args) throws Exception {
        final int peerNr = 100;
        final int port = 4001;
        PeerDHT[] peers = null;
        try {
            peers = ExampleUtils.createAndAttachPeersDHT(peerNr, port);
            ExampleUtils.bootstrap(peers);
            exampleSearch(peers);
            exampleKeywordSearch(peers);
        } finally {
            // 0 is the master
            if (peers != null && peers[0] != null) {
                peers[0].shutdown();
            }
        }
    }

    /**
     * Search for term. This also stores the term under its hash.
     * 
     * @param peers
     *            All the peers
     * @throws IOException .
     * @throws ClassNotFoundException .
     */
    private static void exampleSearch(final PeerDHT[] peers) throws IOException, ClassNotFoundException {
        final int peer30 = 30;
        final int peer60 = 60;

        Number160 key = Number160.createHash(TERM);

        FuturePut futurePut = peers[peer60].put(key).object(TERM).start();
        futurePut.awaitUninterruptibly();

        FutureGet futureGet = peers[peer30].get(key).start();
        futureGet.awaitUninterruptibly();

        System.out.println("got: " + key + " = " + futureGet.data().object());

    }

    /**
     * Search for a keyword and find a term.
     * 
     * @param peers
     *            All the peers.
     * @throws IOException .
     * @throws ClassNotFoundException .
     */
    private static void exampleKeywordSearch(final PeerDHT[] peers) throws IOException, ClassNotFoundException {
        final int peer10 = 10;
        final int peer20 = 20;

        Number160 keyTerm = Number160.createHash(TERM);
        String[] keywords = TERM.split(" ");

        // store a keyword
        for (String keyword : keywords) {
            Number160 keyKeyword = Number160.createHash(keyword);
            peers[peer10].put(keyKeyword).object(keyTerm).start().awaitUninterruptibly();
        }

        // search for a keyword
        Number160 termKey = findReference(peers[peer20], "Communication");
        // this will return a reference to the term stored in the method exampleSearch(), next, we have to search for
        // that.
        FutureGet futureGet = peers[peer10].get(termKey).start();
        futureGet.awaitUninterruptibly();
        System.out.println("searched for [Communication], found " + futureGet.data().object());
    }

    /**
     * Finds a reference and returns it.
     * 
     * @param peer
     *            The peer that searches for the reference.
     * @param keyword
     *            The keyword to search.
     * @return The reference to the keyword or null.
     * @throws ClassNotFoundException .
     * @throws IOException .
     */
    private static Number160 findReference(final PeerDHT peer, final String keyword) throws ClassNotFoundException,
            IOException {
        Number160 keyKeyword = Number160.createHash(keyword);
        FutureGet futureGet = peer.get(keyKeyword).start();
        futureGet.awaitUninterruptibly();
        Number160 termKey = (Number160) futureGet.data().object();
        return termKey;
    }

}
