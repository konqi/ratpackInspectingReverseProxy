import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.form.Form
import ratpack.http.client.HttpClient
import ratpack.parse.Parse

import java.nio.charset.Charset

import static ratpack.groovy.Groovy.ratpack

final Logger log = LoggerFactory.getLogger(Ratpack.class)

ratpack
{
    bindings {
    }

    handlers {
        post('foo', { HttpClient httpClient ->
            request.body.flatMap({ body ->
                def promise = httpClient.requestStream(new URI('http://httpbin.org/post')) { spec ->
                    spec.post()
                    spec.body.buffer body.buffer.copy()
                }.left {
                    context.parse(body, Parse.of(Form.class))
                }

                log.info(promise.dump())
                promise
            }).flatMap { pair ->
                pair.right.getBody().toPromise().left { pair.left.get('a') }
            }.then {
                log.info(it.left)
                log.info(it.right.toString(Charset.defaultCharset()))
                response.headers.set('a', it.left)
                response.send(it.right.toString(Charset.defaultCharset()))
            }
        })
        post('bar', { HttpClient httpClient ->
            request.body.flatMap({ body ->
                // put form data into request registry
                request.add(Form.class, context.parse(body, Parse.of(Form.class)))

                httpClient.requestStream(new URI('http://httpbin.org/post')) { spec ->
                    spec.post()
                    spec.body.buffer body.buffer.copy()
                }
            }).flatMap {
                it.getBody().toPromise()
            }.then {
                // retrieve form data from registry
                Form form = request.get(Form.class)
                response.headers.set('a', form.get('a'))
                response.send(it.toString(Charset.defaultCharset()))
            }
        })
    }
}
