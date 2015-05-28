/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.netty.protocol.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.channel.Connection;
import io.reactivex.netty.channel.ConnectionImpl;
import io.reactivex.netty.protocol.tcp.client.events.TcpClientEventPublisher;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class CookieTest {

    @Test(timeout = 60000)
    public void testGetCookie() throws Exception {
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        String cookie1Name = "PREF";
        String cookie1Value = "ID=a95756377b78e75e:FF=0:TM=1392709628:LM=1392709628:S=a5mOVvTB7DBkexgi";
        String cookie1Domain = ".google.com";
        String cookie1Path = "/";
        String cookie1Header = cookie1Name + '=' + cookie1Value
                               + "; expires=Thu, 18-Feb-2016 07:47:08 GMT; path=" + cookie1Path + "; domain=" + cookie1Domain;
        nettyRequest.headers().add(HttpHeaders.Names.COOKIE, cookie1Header);

        EmbeddedChannel channel = new EmbeddedChannel();
        HttpServerRequest<ByteBuf> request = new HttpServerRequestImpl<ByteBuf>(nettyRequest,channel);

        Map<String,Set<Cookie>> cookies = request.getCookies();
        Assert.assertEquals("Unexpected number of cookies.", 1, cookies.size());
        Set<Cookie> cookies1 = cookies.get(cookie1Name);
        Assert.assertNotNull("No cookie found with name: " + cookie1Name, cookies1);
        Assert.assertEquals("Unexpected number of cookies with name: " + cookie1Name, 1, cookies1.size() );
        Cookie cookie = cookies1.iterator().next();
        Assert.assertEquals("Unexpected cookie name.", cookie1Name, cookie.name());
        Assert.assertEquals("Unexpected cookie path.", cookie1Path, cookie.path());
    }

    @Test(timeout = 60000)
    public void testSetCookie() throws Exception {
        DefaultHttpResponse nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        EmbeddedChannel channel = new EmbeddedChannel();
        TcpClientEventPublisher eventPublisher = new TcpClientEventPublisher();
        Connection<ByteBuf, ByteBuf> connection = ConnectionImpl.create(channel, eventPublisher, eventPublisher);
        HttpServerResponse<ByteBuf> response = HttpServerResponseImpl.create(null, connection, nettyResponse);
        String cookieName = "name";
        String cookieValue = "value";
        response.addCookie(new DefaultCookie(cookieName, cookieValue));
        String cookieHeader = nettyResponse.headers().get(HttpHeaders.Names.SET_COOKIE);
        Assert.assertNotNull("Cookie header not found.", cookieHeader);
        Set<Cookie> decode = CookieDecoder.decode(cookieHeader);
        Assert.assertNotNull("Decoded cookie not found.", decode);
        Assert.assertEquals("Unexpected number of decoded cookie not found.", 1, decode.size());
        Cookie cookie = decode.iterator().next();
        Assert.assertEquals("Unexpected cookie name.", cookieName, cookie.name());
        Assert.assertEquals("Unexpected cookie value.", cookieValue, cookie.value());
    }
}
