import groovy.json.JsonSlurper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import spock.lang.Specification

import java.nio.charset.Charset

/**
 * Created by konqi on 16.06.17.
 */
class ReverseProxySpec extends Specification {
    def "reverse proxy using pairs"() {
        setup:
        def instance = new GroovyRatpackMainApplicationUnderTest()

        when:
        def response = instance.httpClient.request('foo') {
            it.post()
            it.headers.set('Content-Type', 'x-www-form-urlencoded')
            it.body.text('a=b&b=c')
        }

        def jsonBody = new JsonSlurper().parseText(response.body.getText(Charset.defaultCharset()))

        then:
        assert jsonBody.data == 'a=b&b=c'
        assert response.headers.get('a') == 'b'
    }

    def "reverse proxy using request registry"() {
        setup:
        def instance = new GroovyRatpackMainApplicationUnderTest()

        when:
        def response = instance.httpClient.request('bar') {
            it.post()
            it.headers.set('Content-Type', 'x-www-form-urlencoded')
            it.body.text('a=b&b=c')
        }

        def jsonBody = new JsonSlurper().parseText(response.body.getText(Charset.defaultCharset()))

        then:
        assert jsonBody.data == 'a=b&b=c'
        assert response.headers.get('a') == 'b'
    }
}
